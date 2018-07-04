/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.config.scheduler.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.baborg.Azienda;
import it.bologna.ausl.entities.baborg.QAzienda;
import it.bologna.ausl.entities.gipi.Iter;
import it.bologna.ausl.entities.gipi.QEventoIter;
import it.bologna.ausl.entities.gipi.QIter;
import it.bologna.ausl.entities.gipi.Servizio;
import it.bologna.ausl.entities.gipi.Stato;
import it.bologna.ausl.entities.repository.AziendaRepository;
import it.bologna.ausl.entities.repository.IterRepository;
import it.bologna.ausl.gipi.config.scheduler.BaseScheduledJob;
import it.bologna.ausl.gipi.config.scheduler.ServiceKey;
import it.bologna.ausl.gipi.config.scheduler.ServiceManager;
import it.bologna.ausl.gipi.frullinotemp.utils.NotifyScadenzaSospensioneTask;
import static it.bologna.ausl.gipi.frullinotemp.utils.NotifyScadenzaSospensioneTask.JSON;
import static it.bologna.ausl.gipi.frullinotemp.utils.NotifyScadenzaSospensioneTask.MESSAGGIO;
import it.bologna.ausl.gipi.utils.GetBaseUrls;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
import org.springframework.stereotype.Component;

/**
 *
 * @author Salo
 */
@Component
public class JobInviaNotificheTerminiSospensioneIterScaduti implements BaseScheduledJob {

    private static final Logger log = LoggerFactory.getLogger(JobInviaNotificheTerminiSospensioneIterScaduti.class);
    private static JSONArray megaJsonArray;

    @Value("${inviaNotificheWebApi}")
    private String inviaNotificheWebApiPath;

    @PersistenceContext
    EntityManager em;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    IterRepository iterRepository;

    @Autowired
    ServiceManager serviceManager;

    @Autowired
    AziendaRepository aziendaRepository;

    QAzienda qAzienda = QAzienda.azienda;
    QIter qIter = QIter.iter;
    QEventoIter qEventoIter = QEventoIter.eventoIter;

    @Override
    public String getJobName() {
        return "invia_notifiche_termini_sospensione_iter_scaduti";
    }

    public void callBabel(int idAzienda, JSONArray ja) throws ParseException {
        String functionName = "callBabel";
        if (ja.size() > 0) {
            try {
                String urlChiamata = GetBaseUrls.getBabelSuiteWebApiUrl(idAzienda, em, objectMapper) + inviaNotificheWebApiPath;
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
                System.out.println(risultato);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(NotifyScadenzaSospensioneTask.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public JSONObject getJsonOfThisIter(Iter iter) {
        JSONObject o = new JSONObject();
        List<String> utenti = new ArrayList<String>();
        try {
            utenti.add(iter.getIdResponsabileProcedimento().getIdPersona().getCodiceFiscale());
        } catch (Error e) {
            log.error("Non è stato trovato il responsabile del procedimento dell'iter " + iter.getId().toString());
        }
        try {
            if (iter.getIdProcedimento().getIdResponsabileAdozioneAttoFinale() != null && !utenti.contains(iter.getIdProcedimento().getIdResponsabileAdozioneAttoFinale().getIdPersona().getCodiceFiscale())) {
                utenti.add(iter.getIdProcedimento().getIdResponsabileAdozioneAttoFinale().getIdPersona().getCodiceFiscale());
            }
        } catch (Error e) {
            log.error("Non è stato trovato il responsabile adozione dell'atto finale dell'iter " + iter.getId().toString()
                    + " dal procedimento " + iter.getIdProcedimento().getId().toString());
        }
        try {
            if (iter.getIdProcedimento().getIdTitolarePotereSostitutivo() != null && !utenti.contains(iter.getIdProcedimento().getIdTitolarePotereSostitutivo().getIdPersona().getCodiceFiscale())) {
                utenti.add(iter.getIdProcedimento().getIdTitolarePotereSostitutivo().getIdPersona().getCodiceFiscale());
            }
        } catch (Error e) {
            log.error("Non è stato trovato il titolare del potere esecutivo dell'iter " + iter.getId().toString()
                    + " dal procedimento " + iter.getIdProcedimento().getId().toString());
        }
        o.put("cfUtenti", utenti);
        o.put("messaggio", String.format(MESSAGGIO, iter.getNumero() + "/" + iter.getAnno().toString(), iter.getIdProcedimento().getIdAziendaTipoProcedimento().getIdTipoProcedimento().getNome()));
        o.put("idIter", iter.getId());
        o.put("descrizioneNotifica", "EsaurimentoTempiSospensione");
        return o;
    }

    public void puttaQuestoIterNelGiustoJsonAziendale(Iter iter) {
        String functionName = "puttaQuestoIterNelGiustoJsonAziendale";
        megaJsonArray.forEach(item -> {
            JSONObject o = (JSONObject) item;
            int idazienda = (int) o.get("idAzienda");

            if (idazienda == iter.getIdProcedimento().getIdAziendaTipoProcedimento().getIdAzienda().getId()) {
                JSONArray ja = (JSONArray) o.get("datiDaMandare");
                ja.add(getJsonOfThisIter(iter));
            }
        });
    }

    public void caricaGliIterSospesiAndPopolaIlMegaJson() {
        String functionName = "caricaGliIterSospesiAndPopolaIlMegaJson";
        log.info(functionName + " query ricerca iter sospesi");
        List<Iter> lista = new ArrayList<>();
        JPQLQuery<Iter> query = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);
        lista = query.select(this.qIter)
                .from(this.qIter)
                .where(
                        this.qIter.idStato.codice.eq(Stato.CodiciStato.SOSPESO.toString())
                                .and(this.qIter.giorniSospensioneTrascorsi.isNotNull())
                )
                .fetch();
        log.info(functionName + " iter sospesi totali " + lista.size());
        lista.forEach(iter -> {
            int giorniSospensioneTrascorsi = iter.getGiorniSospensioneTrascorsi() != null ? iter.getGiorniSospensioneTrascorsi() : 0;
            int derogaSospensione = iter.getDerogaSospensione() != null ? iter.getDerogaSospensione() : 0;
            int durataMassimaSospensione = iter.getIdProcedimento().getIdAziendaTipoProcedimento().getDurataMassimaSospensione() != null ? iter.getIdProcedimento().getIdAziendaTipoProcedimento().getDurataMassimaSospensione() : 0;
            if (giorniSospensioneTrascorsi > derogaSospensione + durataMassimaSospensione) {
                log.info(functionName + " da notificare l'iter con id = " + iter.getId());
                puttaQuestoIterNelGiustoJsonAziendale(iter);
            }
        });
    }

    public JSONObject gimmeJsonAziendale(int idAzienda) {
        JSONObject o = new JSONObject();
        o.put("idAzienda", idAzienda);
        o.put("datiDaMandare", new JSONArray());
        System.out.println("mi torno --> " + o.toString());
        return o;
    }

    @Override
    public void run() {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        String functionName = "notifyMain";
        ServiceKey serviceKey = new ServiceKey(getJobName(), null);
        Servizio service = serviceManager.getService(serviceKey);
        if (service != null && service.getActive()) {
            serviceManager.setDataInizioRun(serviceKey);
            log.info(functionName + "--> sono entrato nel main del servizio di notifica per Iter Sospesi");
            List<Azienda> aziende = new ArrayList<>();
            megaJsonArray = new JSONArray();
            JPQLQuery<Iter> query = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);
            log.info(functionName + " eseguo query per il recupero delle aziende");
            aziende = query.select(this.qAzienda).from(this.qAzienda).fetch();
            log.info(functionName + " lista aziende: ");
            aziende.forEach(item -> log.info(functionName + "> " + item.getId() + "\t" + item.getDescrizione()));

            log.info(functionName + " preparazione del megaJsonArray ");
            // per ogni azienda mi prendo gli iter ed i loro dati per le notifiche mettendoli nel megaJsonArray
            aziende.forEach(item -> {
                megaJsonArray.add(gimmeJsonAziendale(item.getId()));
            });

            caricaGliIterSospesiAndPopolaIlMegaJson();

            // ORA DOVREI AVERE IL MEGAJSONARRAY POPOLATO: ciclare e chiamare Babel per ogni azienda
            System.out.println("*****************************************************************");
            System.out.println("*****************************************************************");
            log.info(functionName + "--> ITER DA NOTIFICARE TROVATI:");
            megaJsonArray.forEach(item -> log.info(item.toString()));
            System.out.println("*****************************************************************");
            System.out.println("********************* LET'S CALL BABEL **************************");
            megaJsonArray.forEach(item -> {
                JSONObject o = (JSONObject) item;
                int idazienda = (int) o.get("idAzienda");
                JSONArray ja = (JSONArray) o.get("datiDaMandare");
                if (ja.size() > 0) {
                    try {
                        callBabel(idazienda, ja);
                    } catch (ParseException ex) {
                        java.util.logging.Logger.getLogger(JobInviaNotificheTerminiSospensioneIterScaduti.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            serviceManager.setDataFineRun(serviceKey);
        } else {
            log.info(getJobName() + ": servizio non attivo");
        }
    } // fine del run
}// fine classe
