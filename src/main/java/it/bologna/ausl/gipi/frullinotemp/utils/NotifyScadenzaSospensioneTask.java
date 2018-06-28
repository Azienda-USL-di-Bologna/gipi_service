/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.frullinotemp.utils;

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
import it.bologna.ausl.entities.gipi.utilities.EntitiesCachableUtilities;
import it.bologna.ausl.entities.repository.AziendaRepository;
import static it.bologna.ausl.gipi.frullinotemp.utils.NotifyScadenzaSospensioneTaskProva.JSON;
import it.bologna.ausl.gipi.utils.GetBaseUrl;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author l.salomone
 */


@Component
@RequestMapping(value = "${custom.mapping.url.root}" + "/notifiche")
public class NotifyScadenzaSospensioneTask {
    @Value("${inviaNotificheWebApi}")
    private String inviaNotificheWebApiPath;
    @Autowired
    private EntitiesCachableUtilities entitiesCachableUtilities;
    @PersistenceContext
    EntityManager em;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    AziendaRepository aziendaRepository;
    QAzienda qAzienda = QAzienda.azienda;
    QIter qIter = QIter.iter;
    QEventoIter qEventoIter = QEventoIter.eventoIter;
    private static final Logger log = LoggerFactory.getLogger(NotifyScadenzaSospensioneTaskProva.class);
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String MESSAGGIO = "L'iter %s - %s, in stato Sospeso, ha esaurito i tempi previsti per la sua sospensione."; // Mesasggio fisso agli utenti a cui vanno aggiunte le stringhe: numero-anno dell'iter, nome_del_procedimento
    private static final String logContextName = "NotifyScadenzaSospensioneTask.";
    private JSONArray megaJsonArray = new JSONArray(); // questo è il super JSONArray che contiene JSONArray
    
    
    public void callBabel(int idAzienda, JSONArray ja) throws ParseException {
        String functionName = "callBabel";
        if(ja.size() > 0){
            System.out.println("PORCO GIUDA FUNZIONA!!!" + "\n" + ja.toString());
            try {
                String urlChiamata = GetBaseUrl.getBaseUrl(idAzienda, em, objectMapper) + inviaNotificheWebApiPath;
                urlChiamata = "http://localhost:8080/Babel/InviaNotifiche";
                JSONObject jo = new JSONObject();
                jo.put("ja", ja.toString());
                okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, jo.toString().getBytes("UTF-8"));
                log.info(functionName + " -> url aziendale " + urlChiamata);
                OkHttpClient client = new OkHttpClient();
                Request requestg = new Request.Builder()
                        .url(urlChiamata)
                        .addHeader("X-HTTP-Method-Override", "inviaNotifiche")
                        .post(body)
                        .build();
                log.info(functionName + " -> chiamo babel");

                Response responseg = client.newCall(requestg).execute();

                if (!responseg.isSuccessful()) {
                    log.error(functionName + " ERRORE -> la response non è successful");
                    throw new IOException("La chiamata a Babel non è andata a buon fine.");
                }
                log.info(functionName + " -> parso la risposta");
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(responseg.body().string());
                System.out.println(json.toString());

                log.info(functionName + " -> parso il risultato della risposta...");
                String risultato = (String) json.get("risultato");
                log.info(functionName + " risposta -> " + risultato);
                System.out.println(risultato.toString());
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(NotifyScadenzaSospensioneTask.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public JSONObject getJsonOfThisIter(Iter iter){
        JSONObject o = new JSONObject();
        JSONArray ja = new JSONArray();
        ja.add(iter.getIdResponsabileProcedimento().getIdPersona().getCodiceFiscale());
        if(!ja.contains(iter.getIdProcedimento().getIdResponsabileAdozioneAttoFinale().getIdPersona().getCodiceFiscale()))
            ja.add(iter.getIdProcedimento().getIdResponsabileAdozioneAttoFinale().getIdPersona().getCodiceFiscale());
        if(!ja.contains(iter.getIdProcedimento().getIdTitolarePotereSostitutivo().getIdPersona().getCodiceFiscale()))
            ja.add(iter.getIdProcedimento().getIdTitolarePotereSostitutivo().getIdPersona().getCodiceFiscale());
        o.put("cfUtenti", ja.toString());
        o.put("messaggio", String.format(MESSAGGIO, iter.getNumero() + "/" + iter.getAnno().toString(), iter.getIdProcedimento().getIdAziendaTipoProcedimento().getIdTipoProcedimento().getNome()));
        o.put("idIter", iter.getId());
        return o;
    }
    
    public void puttaQuestoIterNelGiustoJsonAziendale(Iter iter) {
        megaJsonArray.forEach(item->{
            JSONObject o = (JSONObject) item;
            int idazienda = (int) o.get("idAzienda");
            
            if(idazienda == iter.getIdProcedimento().getIdAziendaTipoProcedimento().getIdAzienda().getId()){
                JSONArray ja = (JSONArray) o.get("datiDaMandare");
                ja.add(getJsonOfThisIter(iter));
            }
                
        });
            
    }
    
    public void caricaGliIterSospesiAndPopolaIlMegaJson() {
        List<Iter> lista = new ArrayList<>();
        JPQLQuery<Iter> query = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);      
        lista = query.select(this.qIter)
                .from(this.qIter)
                .where(
                        this.qIter.idStato.codice.eq(Stato.CodiciStato.SOSPESO.toString())
                        .and(this.qIter.giorniSospensioneTrascorsi.isNotNull())
                )
                .fetch();
        lista.forEach(iter->{
            int giorniSospensioneTrascorsi = iter.getGiorniSospensioneTrascorsi() != null ? iter.getGiorniSospensioneTrascorsi() : 0;
            int derogaSospensione = iter.getDerogaSospensione() != null ? iter.getDerogaSospensione() : 0;
            int durataMassimaSospensione = iter.getIdProcedimento().getIdAziendaTipoProcedimento().getDurataMassimaSospensione() != null ? iter.getIdProcedimento().getIdAziendaTipoProcedimento().getDurataMassimaSospensione() : 0;
            if(giorniSospensioneTrascorsi > derogaSospensione + durataMassimaSospensione)
                puttaQuestoIterNelGiustoJsonAziendale(iter);
        });
    }
    
    
    public JSONObject gimmeJsonAziendale(int idAzienda) {
        JSONObject o = new JSONObject();
        o.put("idAzienda", idAzienda);
        o.put("datiDaMandare", new JSONArray());
        System.out.println("mi torno --> " + o.toString());
        return o;
    }
    
    /** Questo metodo è quello chiamato dal frullino: cicla gli iter sospesi in prossimità di scadenza e invia ai reposnabili la notifica */
    @RequestMapping(value = "notifyMain", method = RequestMethod.GET)
    public void notifyMain() {
        String functionName = "notifyMain";
        log.info(functionName + "--> sono entrato nel main del servizio di notifica per Iter Sospesi");        
        List<Azienda> aziende = new ArrayList<>();
        JPQLQuery<Iter> query = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);
        aziende = query.select(this.qAzienda).from(this.qAzienda).fetch();
        aziende.forEach(item->System.out.println(item.getId() + "\t" + item.getDescrizione()));
        
        // per ogni azienda mi prendo gli iter ed i loro dati per le notifiche mettendoli nel megaJsonArray
        aziende.forEach(item->{
            megaJsonArray.add(gimmeJsonAziendale(item.getId()));
        });
        
        caricaGliIterSospesiAndPopolaIlMegaJson();
        
        // ORA DOVREI AVERE IL MEGAJSONARRAY POPOLATO: ciclare e chiamare Babel per ogni azienda
        System.out.println("*****************************************************************");
        System.out.println("*****************************************************************");
        megaJsonArray.forEach(item->System.out.println(item.toString()));
        System.out.println("*****************************************************************");
        System.out.println("********************* LET'S CALL BABEL **************************");
        megaJsonArray.forEach(item->{
            try {
                JSONObject o = (JSONObject) item;
                int idazienda = (int) o.get("idAzienda");
                JSONArray ja = (JSONArray) o.get("datiDaMandare");
                if (ja.size() > 0)
                    callBabel(idazienda, ja);
            } catch (ParseException ex) {
                java.util.logging.Logger.getLogger(NotifyScadenzaSospensioneTask.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
}
