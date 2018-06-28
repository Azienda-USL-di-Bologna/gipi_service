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


// @Component
// @RequestMapping(value = "${custom.mapping.url.root}" + "/notifiche")
public class NotifyScadenzaSospensioneTaskProva {
    
    @Value("${getIdUtentiMap}")
    private String getIdUtentiMapPath;
    
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
    
    // Mesasggio fisso agli utenti a cui vanno aggiunte le stringhe: numero-anno dell'iter, nome_del_procedimento
    public static final String MESSAGGIO = "L'iter %s - %s, in stato Sospeso,  ha esaurito i tempi previsti per la sua sospensione.";
    
    private static final String logContextName = "NotifyScadenzaSospensioneTask.";
    
    // questo è il super JSONArray che contiene JSONArray
    private JSONArray megaJsonArray = new JSONArray();
    
    
    public void componiAndIviaNotifiche() {
        
    }
    
    
    public JSONObject callBabel(int idAzienda, JSONObject oggettoPerBabel) throws Exception {
        String functionName = "callBabel";
        try {
            String urlChiamata = GetBaseUrl.getBaseUrl(idAzienda, em, objectMapper) + getIdUtentiMapPath;
            System.out.println(functionName + " --> urlChiamata => " + urlChiamata);
            System.out.println(functionName + " --> oggettoPerBabel => " + oggettoPerBabel.toString());

            okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, oggettoPerBabel.toString().getBytes("UTF-8"));
            log.info(functionName + " -> url aziendale " + urlChiamata);
            OkHttpClient client = new OkHttpClient();
            Request requestg = new Request.Builder()
                    .url(urlChiamata)
                    .addHeader("X-HTTP-Method-Override", "getIdUtentiMap")
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
            JSONObject risultato = (JSONObject) parser.parse((String) json.get("risultato"));
            System.out.println(risultato.toString());

            log.info(functionName + " -> ritorno il risultato");
            return risultato;
        }catch(Exception e){
            log.error(functionName + " ERRORE " + e);
            throw new Exception(e);
        }
    }
    
    
    // chiama la webapi GetIdUtentiMap sull'azienda specificata mandando un jsonArray di codici fiscali 
    // restuisce una mappa con chiva:valore => cf:id_utente
    public JSONObject callBabelAndGetIdUtente(int idAzienda, JSONArray cfJsonArray) throws IOException, Exception {
        String functionName = "callBabelAndGetIdUtente";
        try {
            JSONObject oggettoPerBabel = new JSONObject();

            // ora metto il jsonArray stringhificato in un json da mandare a Babel, di modo che InDe possa leggerlo
             return callBabel(idAzienda, (JSONObject) oggettoPerBabel.put("listaCF", cfJsonArray.toString()));
           
            
        }catch(Exception e){
            log.error(functionName + " ERRORE " + e);
            throw new Exception(e);
        }
    }  
    
    
    // presi come parametri un iter e un json array, aggiunge i codici fiscali dei responsabili dell'iter non ancora presenti nel jsonarray.
    public void putCodiciFiscaliResponsabiliInArrayList(Iter iter, JSONArray jaCfUtentiAzienda) {
        String functionName = "putCodiciFiscaliResponsabiliInArrayList";      
        if(!jaCfUtentiAzienda.contains(iter.getIdResponsabileProcedimento().getIdPersona().getCodiceFiscale())){
            jaCfUtentiAzienda.add(iter.getIdResponsabileProcedimento().getIdPersona().getCodiceFiscale());
            log.info(functionName + " aggiunto cf del responsabile di procedimento " + iter.getIdResponsabileProcedimento().getIdPersona().getCodiceFiscale());
        }
               
        if(!jaCfUtentiAzienda.contains(iter.getIdProcedimento().getIdResponsabileAdozioneAttoFinale().getIdPersona().getCodiceFiscale())){
            jaCfUtentiAzienda.add(iter.getIdProcedimento().getIdResponsabileAdozioneAttoFinale().getIdPersona().getCodiceFiscale());
            log.info(functionName + " aggiunto cf del responsabile adozione atto finale " + iter.getIdProcedimento().getIdResponsabileAdozioneAttoFinale().getIdPersona().getCodiceFiscale());
        }    
        
        if(!jaCfUtentiAzienda.contains(iter.getIdProcedimento().getIdTitolarePotereSostitutivo().getIdPersona().getCodiceFiscale())){
            jaCfUtentiAzienda.add(iter.getIdProcedimento().getIdTitolarePotereSostitutivo().getIdPersona().getCodiceFiscale());
            log.info(functionName + " aggiunto cf del titolare del potere sostitutivo " + iter.getIdProcedimento().getIdTitolarePotereSostitutivo().getIdPersona().getCodiceFiscale());
        }
    }
    
    // a partire dall'iter crea il json con i dati salienti che ci serviranno per inviare le notifiche
    public JSONObject getJsonObjectFromIter(Iter iter) {
        String functionName = "getJsonObjectFromIter";
        log.info(functionName + " mi creo il json per l'iter con id " + iter.getId().toString());
        JSONObject json = new JSONObject();
        json.put("idIter", iter.getId());
        json.put("numeroAnno", iter.getNumero() + "/" + iter.getAnno().toString());
        json.put("oggetto", iter.getOggetto());
        json.put("reponsabileProcedimento", iter.getIdResponsabileProcedimento().getIdPersona().getCodiceFiscale());
        json.put("reponsabileAdozione", iter.getIdProcedimento().getIdResponsabileAdozioneAttoFinale().getIdPersona().getCodiceFiscale());
        json.put("titolare", iter.getIdProcedimento().getIdTitolarePotereSostitutivo().getIdPersona().getCodiceFiscale());
        
        System.out.println(json);
        return json;
    }
    
    
    // prende come parametri la lista degli iter e il idAzienda, cicla gli iter, ne ricava i dati per la notifica e il json degli idUtente tipo Babel
    public JSONObject ciclamiListaAndRidammiJSONObject(List<Iter> lista, int idAzienda){
        JSONObject o = new JSONObject();
        JSONArray cfJsonArray = new JSONArray();
        JSONArray datiIterJsonArray = new JSONArray();
        for(Iter iter : lista ) {          
            int giorniSospensioneTrascorsi = iter.getGiorniSospensioneTrascorsi() != null ? iter.getGiorniSospensioneTrascorsi() : 0;
            int derogaSospensione = iter.getDerogaSospensione() != null ? iter.getDerogaSospensione() : 0;
            int durataMassimaSospensione = iter.getIdProcedimento().getIdAziendaTipoProcedimento().getDurataMassimaSospensione() != null ? iter.getIdProcedimento().getIdAziendaTipoProcedimento().getDurataMassimaSospensione() : 0;
            
            if(giorniSospensioneTrascorsi > derogaSospensione + durataMassimaSospensione){
                datiIterJsonArray.add(getJsonObjectFromIter(iter));
                putCodiciFiscaliResponsabiliInArrayList(iter, cfJsonArray);
            }
        }
        System.out.println(datiIterJsonArray.toString());
        System.out.println(cfJsonArray.toString());

        try {
            // ora, io ho il JSONArray con i cf degli utenti di un'azienda: chiamo  l'azienda e gli chiedo l'itUtente
            System.out.println("OK MO SPACCO TUTTO!!");
            JSONObject cfIdUtenteJson = callBabelAndGetIdUtente(idAzienda, cfJsonArray);
            o.put("datiIterJsonArray", datiIterJsonArray);
            o.put("cdIdUtentiJson", cfIdUtenteJson);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(NotifyScadenzaSospensioneTaskProva.class.getName()).log(Level.SEVERE, null, ex);
        }
        return o;
    }
    
    /** Carico gli iter sospesi, ma solo quelli che hanno finito il tempo massimo di sospensione*/
    @RequestMapping(value = "getIterSospesiOutOfTime", method = RequestMethod.GET)
    public List<Iter> getIterSospesiOutOfTime(int idAzienda) {
        String functionName = "getIterSospesiOutOfTime";
        log.info(functionName + " --> compongo la query");
        List<Iter> lista = new ArrayList<>();
        JPQLQuery<Iter> query = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);      
        lista = query.select(this.qIter)
                .from(this.qIter)
                .where(
                        this.qIter.idStato.codice.eq(Stato.CodiciStato.SOSPESO.toString())
                        .and(this.qIter.giorniSospensioneTrascorsi.isNotNull())
                        .and(this.qIter.idProcedimento.idAziendaTipoProcedimento.idAzienda.id.eq(idAzienda))
                )
                .fetch();    
        lista.forEach(item->System.out.println(functionName + "-> " + item.getId().toString() 
                + "\t" + item.getIdStato().getDescrizione()
                + "\t" + item.getNumero() + "/" + item.getAnno().toString() + " - " + item.getOggetto()
        ));
        return lista;
    }
    
    
    // se ci sono degli iter sospesi per l'azienda popola il megaJsonArray con i json dei dati necessari per mandare le notifiche
    public void recuperaDatiIterSospesiDiQuestaAzienda(Azienda azienda) throws Exception {
        int idAzienda = azienda.getId();
        List<Iter> lista = getIterSospesiOutOfTime(idAzienda);
        if(lista.size() > 0){
            JSONObject jsonAziendale = new JSONObject();
            jsonAziendale.put(idAzienda, ciclamiListaAndRidammiJSONObject(lista, idAzienda));
            megaJsonArray.add(jsonAziendale);
        }
        else{
            log.info("Non ci sono iter sospesi per " + azienda.getDescrizione());
        }
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
            try {
                recuperaDatiIterSospesiDiQuestaAzienda(item);
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(NotifyScadenzaSospensioneTaskProva.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        // ORA DOVREI AVERE IL MEGAJSONARRAY POPOLATO: ciclare e chiamare Babel per ogni azienda
        System.out.println("*****************************************************************");
        System.out.println("*****************************************************************");
        System.out.println("*****************************************************************");
        megaJsonArray.forEach(item->System.out.println(item));
        System.out.println("*****************************************************************");
        System.out.println("*****************************************************************");
        System.out.println("*****************************************************************");
        
        
        // mando a Babel il JSONArray con i cf de
        
        
        // ciclo gli iter:  
        //      per ogni responsabile, cerco l'id_utente nella mappa
        //      gli invio la notifica
        
        
    }
}
