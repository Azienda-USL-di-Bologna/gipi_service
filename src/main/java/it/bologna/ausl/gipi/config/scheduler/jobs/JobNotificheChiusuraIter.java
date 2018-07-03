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
import it.bologna.ausl.gipi.utils.GetBaseUrls;
import it.bologna.ausl.gipi.utils.QueryPronte;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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
@RequestMapping(value = "${custom.mapping.url.root}" + "/notifiche")
public class JobNotificheChiusuraIter {
    
    @PersistenceContext
    EntityManager em;
    
    @Autowired
    ObjectMapper objectMapper;
    
    @Autowired
    QueryPronte queryPronte;
    
    long diffInMillies;
    long giorniTrascorsi;
    
    private static final Logger log = LoggerFactory.getLogger(JobNotificheChiusuraIter.class);
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    @RequestMapping(value = "notifyChiusure", method = RequestMethod.GET)
    public ResponseEntity notifyChiusure() throws IOException {
        
        log.info("-= Notify Chiusura Iter =-");
        log.info("Carico la lista delle aziende...");
        List<Azienda> aziende = queryPronte.getListaAziende();
        
        log.info("Carico la lista degli iter...");
        List<Iter> iters = queryPronte.getIterInCorso();
        
        HashMap<String, JSONArray> mappaAziende = new HashMap<String, JSONArray>();
        for (Azienda az: aziende){
            mappaAziende.put(az.getId().toString(), new JSONArray());
        }
        
        JSONArray tempObj;
        log.info("Inizio a ciclare gli iter...");        
        Date dataOdierna = new Date(); 
        for (Iter i: iters){
            JSONObject obj = new JSONObject();
            diffInMillies = Math.abs(dataOdierna.getTime() - i.getDataAvvio().getTime()); // - i.getGiorniSospensioneTrascorsi());
            giorniTrascorsi = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            log.debug("Giorni trascorsi = " + giorniTrascorsi);
            //Date chiusuraPrevista = i.getDataChiusuraPrevista();
            Date chiusuraPrevista = new Date();
            int durataMassimaProcedimento = i.getIdProcedimento().getIdAziendaTipoProcedimento().getDurataMassimaProcedimento(); // aggiungere giorni sospensione?
            Calendar cal = Calendar.getInstance();
            cal.setTime(i.getDataAvvio());
            cal.add(Calendar.DATE, durataMassimaProcedimento); // aggiungo la durata massima del procedimento.
            chiusuraPrevista = cal.getTime();
            diffInMillies = Math.abs(chiusuraPrevista.getTime() - i.getDataAvvio().getTime());
            long daysExpected = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);    // giorni previsti 
            EventoIter evIniz = queryPronte.getEventoInizialeIter(i.getId());
            List<String> utentiList = new ArrayList<>();
            Set<String> utentiSet = new HashSet<>();
            utentiSet.add(evIniz.getAutore().getIdPersona().getCodiceFiscale());
            utentiSet.add(i.getProcedimentoCache().getIdResponsabileProcedimento().getIdPersona().getCodiceFiscale());
            utentiSet.add(i.getProcedimentoCache().getIdResponsabileAdozioneAttoFinale().getIdPersona().getCodiceFiscale());
            obj.put("idIter", i.getId());
            obj.put("descrizioneNotifica", "notificaChiusura");
            if (giorniTrascorsi > daysExpected) {   // L'iter ha superato i giorni previsti per la chiusura
                utentiSet.add(i.getProcedimentoCache().getIdTitolarePotereSostitutivo().getIdPersona().getCodiceFiscale());
                utentiList.addAll(utentiSet);
                obj.put("cfUtenti", utentiList);
                obj.put("messaggio", "L'iter " +
                    i.getNumero() + "/" + i.getAnno() + " - " + i.getProcedimentoCache().getNomeTipoProcedimento() +
                    " ha esaurito i tempi previsti per la sua esecuzione.");
                tempObj = mappaAziende.get(i.getIdProcedimento().getIdAziendaTipoProcedimento().getIdAzienda().getId().toString());
                tempObj.add(obj);
            } else {
                int j = 1;
                long temp = 0;
                do {
                    temp += Math.round((1 / Math.pow(2, j)) * daysExpected); 
                    if (giorniTrascorsi == temp) {
                        utentiList.addAll(utentiSet);
                        obj.put("cfUtenti", utentiList);
                        obj.put("messaggio", "Mancano " + 
                            (daysExpected - giorniTrascorsi) + " giorni alla chiusura dell'iter " +
                            i.getNumero() + "/" + i.getAnno() + " - " + i.getProcedimentoCache().getNomeTipoProcedimento() + ".");
                        tempObj = mappaAziende.get(i.getIdProcedimento().getIdAziendaTipoProcedimento().getIdAzienda().getId().toString());
                        tempObj.add(obj);
                    }
                    j++;
                } while (temp < daysExpected && temp < giorniTrascorsi);
            }
        }
        log.info("Iter elaborati. Effettuo le chiamate alle varie applicazioni...");
        String urlChiamata;
        JSONObject responseObj = new JSONObject();
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
                    responseObj.put(key, "Inviate N° " + ja.size() + " notifiche.");
                } else {
                    log.error("Non va la chiamata a Babel." );
                }
            } catch (NullPointerException ex) {
                log.error("Errore, l'azienda non ha parametri: " + ex);
            } finally {
                responseg.body().close();
            }
        }
        log.info("Fine procedura.");
        return new ResponseEntity(responseObj.toString(),HttpStatus.OK);
    }  
}
