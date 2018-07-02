/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.geiar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.baborg.Azienda;
import it.bologna.ausl.entities.baborg.QAzienda;
import it.bologna.ausl.entities.gipi.EventoIter;
import it.bologna.ausl.entities.gipi.Iter;
import it.bologna.ausl.entities.gipi.QEventoIter;
import it.bologna.ausl.entities.gipi.QIter;
import it.bologna.ausl.entities.gipi.Stato;
import it.bologna.ausl.gipi.utils.GetBaseUrl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class NotifyChiusuraIter {
    
    @PersistenceContext
    EntityManager em;
    
    @Autowired
    ObjectMapper objectMapper;
    
    QAzienda qAzienda = QAzienda.azienda;
    QIter qIter = QIter.iter;
    
    long diffInMillies;
    long giorniTrascorsi;
    
    private static final Logger log = LoggerFactory.getLogger(NotifyChiusuraIter.class);
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    @RequestMapping(value = "notifyChiusure", method = RequestMethod.GET)
    public ResponseEntity notifyChiusure() throws IOException {
        
        log.info("-= Notify Chiusura Iter =-");
        log.info("Carico la lista delle aziende...");
        JPQLQuery<Azienda> query = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);
        List<Azienda> aziende = query.from(qAzienda).fetch();
        
        log.info("Carico la lista degli iter...");
        JPQLQuery<Iter> queryIter = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
        List<Iter> iters = queryIter.from(qIter)
            .where(qIter.idStato.codice.eq(Stato.CodiciStato.IN_CORSO.toString()))
            .fetch();
        
        HashMap<String, JSONArray> mappaAziende = new HashMap<String, JSONArray>();
        for (Azienda az: aziende){
            mappaAziende.put(az.getId().toString(), new JSONArray());
        }
        
        JSONArray tempObj;
                
        Date dataOdierna = new Date();
        Boolean sendNotify;    
        for (Iter i: iters){
            JSONObject obj = new JSONObject();
            sendNotify = false;
            diffInMillies = Math.abs(dataOdierna.getTime() - i.getDataAvvio().getTime());
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
            long daysExpected = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            QEventoIter qEventoIter = QEventoIter.eventoIter;
            JPQLQuery<EventoIter> queryEventiIter = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
            EventoIter evIniz = queryEventiIter
                .from(qEventoIter)
                .where(qEventoIter.idIter.id.eq(i.getId()).and(qEventoIter.idEvento.id.eq(1))) // 1 evento apertura
                .fetchOne();
            List<String> utentiList = new ArrayList<>();
            utentiList.add(evIniz.getAutore().getIdPersona().getCodiceFiscale()); // Creatore Iter
            utentiList.add(i.getProcedimentoCache().getIdResponsabileProcedimento().getIdPersona().getCodiceFiscale());
            utentiList.add(i.getProcedimentoCache().getIdResponsabileAdozioneAttoFinale().getIdPersona().getCodiceFiscale());
            obj.put("idIter", i.getId());
            obj.put("descrizioneNotifica", "notificaChiusura");
            if (giorniTrascorsi > daysExpected) {
                utentiList.add(i.getProcedimentoCache().getIdTitolarePotereSostitutivo().getIdPersona().getCodiceFiscale());
                obj.put("cfUtenti", utentiList);
                obj.put("messaggio", "L'iter " +
                    i.getNumero() + "/" + i.getAnno() + " - " + i.getProcedimentoCache().getNomeTipoProcedimento() +
                    " ha esaurito i tempi previsti per la sua esecuzione.");
                sendNotify = true;
            } else {
                int j = 1;
                long temp = 0;
                do {
                    temp += Math.round((1 / Math.pow(2, j)) * daysExpected); 
                    if (giorniTrascorsi == temp) {
                        obj.put("cfUtenti", utentiList);
                        obj.put("messaggio", "Mancano " + 
                            (daysExpected - giorniTrascorsi) + " giorni alla chiusura dell'iter " +
                            i.getNumero() + "/" + i.getAnno() + " - " + i.getProcedimentoCache().getNomeTipoProcedimento() + ".");
                        sendNotify = true;
                    }
                    j++;
                } while (temp < daysExpected && temp <= giorniTrascorsi);
            }
            
            if (sendNotify) {
                tempObj = mappaAziende.get(i.getIdProcedimento().getIdAziendaTipoProcedimento().getIdAzienda().getId().toString());
                tempObj.add(obj);
            }
        }
        String urlChiamata;
        JSONObject responseObj = new JSONObject();
        Response responseg = null;
        for (Map.Entry<String, JSONArray> entry : mappaAziende.entrySet()) {
            String key = entry.getKey();
            JSONArray ja = entry.getValue();
            JSONObject o = new JSONObject();
            o.put("ja", ja.toString());
            try {
                urlChiamata = GetBaseUrl.getBaseUrl(Integer.parseInt(key), em, objectMapper);
                urlChiamata += "/Babel/InviaNotificheIter";

                okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, o.toString().getBytes("UTF-8"));
                log.info("Url Chiamata = " + urlChiamata );
                Request requestg = new Request.Builder()
                        .url(urlChiamata)
                        .addHeader("X-HTTP-Method-Override", "inviaNotifiche")
                        .post(body)
                        .build();

                OkHttpClient client = new OkHttpClient();
                responseg = client.newCall(requestg).execute();
                if (responseg.isSuccessful()) {
                    responseObj.put(key, "Inviate N° " + ja.size() + " notifiche. ");
                } else {
                    log.info("Non va la chiamata a Babel." );
                }
            } catch (NullPointerException ex) {
                log.error("Errore, l'azienda non ha parametri: " + ex);
            } finally {
                responseg.body().close();
            }
        }
        log.info("Fine procedura");
        return new ResponseEntity(responseObj.toString(),HttpStatus.OK);
    }
}
