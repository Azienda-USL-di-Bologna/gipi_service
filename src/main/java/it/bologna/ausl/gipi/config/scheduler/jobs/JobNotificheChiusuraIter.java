/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.config.scheduler.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.bologna.ausl.entities.baborg.Azienda;
import it.bologna.ausl.entities.gipi.EventoIter;
import it.bologna.ausl.entities.gipi.Iter;
import it.bologna.ausl.entities.gipi.Servizio;
import it.bologna.ausl.entities.gipi.Stato;
import it.bologna.ausl.gipi.config.scheduler.BaseScheduledJob;
import it.bologna.ausl.gipi.config.scheduler.ServiceKey;
import it.bologna.ausl.gipi.config.scheduler.ServiceManager;
import it.bologna.ausl.gipi.utils.GetBaseUrls;
import it.bologna.ausl.gipi.utils.Queries;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author Giuseppe Russo <g.russo@nsi.it>
 */

@Component
public class JobNotificheChiusuraIter implements BaseScheduledJob {
    
    @PersistenceContext
    EntityManager em;
    
    @Autowired
    ObjectMapper objectMapper;
    
    @Autowired
    Queries queryList;
    
    @Autowired
    ServiceManager serviceManager;
    
    long diffInMillies;
    long giorniTrascorsi;
    
    private static final Logger log = LoggerFactory.getLogger(JobNotificheChiusuraIter.class);
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    @Override
    public String getJobName() {
        return "notifiche_chiusura_iter";
    }
    
    @Override
    public void run() {
        ServiceKey serviceKey = new ServiceKey(getJobName(), null);
        Servizio service = serviceManager.getService(serviceKey);
        
        String s = "-";
        String delimiter = IntStream.range(0, 30).mapToObj(i -> s).collect(Collectors.joining(""));

        if (service != null && service.getActive()) {
            log.info(delimiter + "START: " + getJobName() + delimiter);
            serviceManager.setDataInizioRun(serviceKey);
        
            log.info("-= Notify Chiusura Iter =-");
            log.info("Carico la lista delle aziende...");
            List<Azienda> aziende = queryList.getListaAziende();

            log.info("Carico la lista degli iter...");
            List<Iter> iters = queryList.getIterByStatus(Stato.CodiciStato.IN_CORSO);

            log.info("Creo la mappa delle aziende...");
            HashMap<String, JSONArray> mappaAziende = new HashMap<String, JSONArray>();
            for (Azienda az: aziende){
                mappaAziende.put(az.getId().toString(), new JSONArray());
            }

            JSONArray tempAzienda;  // Variabile per memorizzare un'azienda della mappa
            log.info("Inizio a ciclare gli iter...");        
            Date dataOdierna = new Date(); 
            for (Iter i: iters){
                try {
                    JSONObject obj = new JSONObject();
                    int giorniSospensioneTrascorsi = i.getGiorniSospensioneTrascorsi() == null ? 0 : i.getGiorniSospensioneTrascorsi();
                    diffInMillies = Math.abs(dataOdierna.getTime() - i.getDataAvvio().getTime());
                    giorniTrascorsi = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) - giorniSospensioneTrascorsi;
                    // log.debug("Giorni trascorsi = " + giorniTrascorsi);
                    //Date chiusuraPrevista = i.getDataChiusuraPrevista();
                    /* Questa parte va tolta quando il campo DataChiusuraPrevista sul DB sarà aggiornato automaticamente - START */
                    Date chiusuraPrevista = new Date();
                    int giorniDerogaIter = i.getDerogaDurata() == null ? 0 : i.getDerogaDurata();
                    int durataMassimaProcedimento = i.getIdProcedimento().getIdAziendaTipoProcedimento().getDurataMassimaProcedimento() + giorniDerogaIter;
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(i.getDataAvvio());
                    cal.add(Calendar.DATE, durataMassimaProcedimento); // aggiungo la durata massima del procedimento.
                    chiusuraPrevista = cal.getTime();
                    /* END  */
                    diffInMillies = Math.abs(chiusuraPrevista.getTime() - i.getDataAvvio().getTime());
                    long giorniPrevisti = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                    EventoIter evIniz = queryList.getEventoInizialeIter(i.getId());
                    List<String> utentiList = new ArrayList<>();
                    Set<String> utentiSet = new HashSet<>();        // Utilizzo il set per evitare utenti duplicati nella lista
                    /* Aggiungo gli utenti a cui inviare la notifica che saranno sempre presenti, sia per la notifica di scadenza che quelli già scaduti */
                    utentiSet.add(evIniz.getAutore().getIdPersona().getCodiceFiscale());
                    utentiSet.add(i.getProcedimentoCache().getIdResponsabileProcedimento().getIdPersona().getCodiceFiscale());
                    utentiSet.add(i.getProcedimentoCache().getIdResponsabileAdozioneAttoFinale().getIdPersona().getCodiceFiscale());
                    obj.put("idIter", i.getId());
                    obj.put("descrizioneNotifica", "Scadenza");
                    if (giorniTrascorsi > giorniPrevisti) {   // L'iter ha superato i giorni previsti per la chiusura
                        log.debug("Iter n° " + i.getNumero() + " scaduto. Notifica da inviare.");
                        utentiSet.add(i.getProcedimentoCache().getIdTitolarePotereSostitutivo().getIdPersona().getCodiceFiscale());
                        utentiList.addAll(utentiSet);
                        obj.put("cfUtenti", utentiList);
                        obj.put("messaggio", "Iter " +
                            i.getNumero() + "/" + i.getAnno() + ": " + i.getProcedimentoCache().getNomeTipoProcedimento() +
                            " - ha esaurito i tempi previsti per la sua esecuzione.");
                        tempAzienda = mappaAziende.get(i.getIdProcedimento().getIdAziendaTipoProcedimento().getIdAzienda().getId().toString());
                        tempAzienda.add(obj);
                    } else {        // Iter non è ancora scaduto, calcolo se inviare la notifica in base ai giorni restanti
                        int j = 1;
                        long temp = 0;
                        do {
                            temp += Math.round((1 / Math.pow(2, j)) * giorniPrevisti); 
                            if (giorniTrascorsi == temp) {
                                log.debug("Iter n° " + i.getNumero() + " in scadenza. Mancano "+ (giorniPrevisti - giorniTrascorsi) + " giorni. Notifica da inviare.");
                                utentiList.addAll(utentiSet);
                                obj.put("cfUtenti", utentiList);
                                obj.put("messaggio", "Iter " + i.getNumero() + "/" + i.getAnno() + ": " +
                                    i.getProcedimentoCache().getNomeTipoProcedimento() + " - mancano " +
                                    (giorniPrevisti - giorniTrascorsi) + " giorni alla chiusura dell'iter.");
                                tempAzienda = mappaAziende.get(i.getIdProcedimento().getIdAziendaTipoProcedimento().getIdAzienda().getId().toString());
                                tempAzienda.add(obj);
                            }
                            j++;
                        } while (temp < giorniPrevisti && temp < giorniTrascorsi);
                    }
                } catch (NullPointerException ex) {
                    log.error("Dato nullo = " + ex );
                }
            }
            log.info("Iter elaborati. Effettuo le chiamate alle varie applicazioni...");
            String urlChiamata;
            ArrayList<String> resultListMessage = new ArrayList<>();
            Response responseg = null;
            for (Map.Entry<String, JSONArray> entry : mappaAziende.entrySet()) {
                String key = entry.getKey();
                JSONArray ja = entry.getValue();
                JSONObject o = new JSONObject();
                o.put("ja", ja.toString());
                try {
                    urlChiamata = GetBaseUrls.getBabelSuiteWebApiUrl(Integer.parseInt(key), em, objectMapper);
                    urlChiamata += "/Babel/InviaNotificheIter";

                    okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, o.toString().getBytes("UTF-8"));
                    log.debug("Url Chiamata = " + urlChiamata );
                    Request requestg = new Request.Builder()
                            .url(urlChiamata)
                            .addHeader("X-HTTP-Method-Override", "inviaNotifiche")
                            .post(body)
                            .build();

                    OkHttpClient client = new OkHttpClient();
                    responseg = client.newCall(requestg).execute();
                    if (responseg.isSuccessful()) {
                        resultListMessage.add("Azienda " + key + " inviate n° " + ja.size() + " notifiche.");
                    } else {
                        log.error("Non va la chiamata a Babel." );
                    }
                } catch (NullPointerException | IOException ex) {
                    log.error("Errore -> " + ex);
                } finally {
                    responseg.body().close();
                }
            }
            serviceManager.setDataFineRun(serviceKey);
            resultListMessage.forEach((mess) -> {log.info(mess);});
            log.info(delimiter + "STOP: " + getJobName() + delimiter);
        } else {
            log.info(getJobName() + ": servizio non attivo");
        }
    }  
}
