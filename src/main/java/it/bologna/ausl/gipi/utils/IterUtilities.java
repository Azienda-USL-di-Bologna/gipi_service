/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import com.google.gson.JsonObject;
import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.baborg.Struttura;
import it.bologna.ausl.entities.baborg.Utente;
import it.bologna.ausl.entities.cache.cachableobject.UtenteCachable;
import it.bologna.ausl.entities.gipi.DocumentoIter;
import it.bologna.ausl.entities.gipi.Evento;
import it.bologna.ausl.entities.gipi.EventoIter;
import it.bologna.ausl.entities.gipi.FaseIter;
import it.bologna.ausl.entities.gipi.Iter;
import it.bologna.ausl.entities.gipi.MotivoPrecedente;
import it.bologna.ausl.entities.gipi.ProcedimentoCache;
import it.bologna.ausl.entities.gipi.QEventoIter;
import it.bologna.ausl.entities.gipi.QFaseIter;
import it.bologna.ausl.entities.gipi.QIter;
import it.bologna.ausl.entities.gipi.QMotivoPrecedente;
import it.bologna.ausl.entities.gipi.QRegistroTipoProcedimento;
import it.bologna.ausl.entities.gipi.Registro;
import it.bologna.ausl.entities.gipi.RegistroIter;
import it.bologna.ausl.entities.gipi.RegistroTipoProcedimento;
import it.bologna.ausl.entities.gipi.utilities.EntitiesCachableUtilities;
import it.bologna.ausl.gipi.exceptions.GipiPubblicazioneException;
import it.bologna.ausl.gipi.jwt.utils.TokenGenerator;
import it.bologna.ausl.gipi.utils.classes.GestioneStatiParams;
import it.bologna.ausl.ioda.iodaobjectlibrary.Document;
import it.bologna.ausl.ioda.iodaobjectlibrary.Fascicolazione;
import it.bologna.ausl.ioda.iodaobjectlibrary.GdDoc;
import it.bologna.ausl.ioda.iodaobjectlibrary.IodaRequestDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import javax.persistence.EntityManager;
import okhttp3.MultipartBody;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import static it.bologna.ausl.gipi.process.CreaIter.JSON;
import it.bologna.ausl.gipi.pubblicazioni.RegistroAccessi;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import it.bologna.ausl.gipi.pubblicazioni.Marshaller;
import java.util.Date;
import java.util.EnumMap;
import javax.persistence.Query;
import org.json.simple.JSONObject;

/**
 *
 * @author Giuseppe Russo <g.russo@nsi.it>
 */
@Component
public class IterUtilities {

    @Autowired
    EntityManager em;

    @Autowired
    ObjectMapper objectMapper;
    
    @Autowired
    IterUtilities iterUtilities;
    
    @Autowired
    private EntitiesCachableUtilities entitiesCachableUtilities;

    @Autowired
    TokenGenerator tokenGenerator;

    @Value("${updateGdDoc}")
    private String updateGdDocPath;

    @Value("${gipi.api.registro-accessi}")
    private String registroAccessiPath;

    QRegistroTipoProcedimento qRegistroTipoProcedimento = QRegistroTipoProcedimento.registroTipoProcedimento;
    QEventoIter qEventoIter = QEventoIter.eventoIter;

    public enum Esiti {
        ACCOLTO, RIFIUTO_TOTALE, RIFIUTO_PARZIALE, RITIRATO
    }

    private static final Logger log = LoggerFactory.getLogger(IterUtilities.class);

    /* Fascicolo il documento */
    public Response inserisciFascicolazione(Iter i, GestioneStatiParams gestioneStatiParams, String cfUtenteFascicolatore) throws IOException {

        String baseUrl = GetBaseUrls.getBabelSuiteBdsToolsUrl(i.getIdProcedimento().getIdAziendaTipoProcedimento().getIdAzienda().getId(), em, objectMapper);

        // baseUrl = "http://localhost:8084/bds_tools/ioda/api/document/update";
        String urlChiamata = baseUrl + updateGdDocPath;

        GdDoc g = new GdDoc((String) gestioneStatiParams.getIdOggettoOrigine(), gestioneStatiParams.getTipoOggettoOrigine(), null, null, null, null, null, (String) gestioneStatiParams.getCodiceRegistroDocumento(), null, null, null, null, null, null, null, null, null, null);
        Fascicolazione fascicolazione = new Fascicolazione(i.getIdFascicolo(), null, null, null, DateTime.now(), Document.DocumentOperationType.INSERT);
        fascicolazione.setCfUtenteFascicolatore(cfUtenteFascicolatore);
        ArrayList a = new ArrayList();
        a.add(fascicolazione);
        g.setFascicolazioni(a);
        IodaRequestDescriptor irdg = new IodaRequestDescriptor("gipi", "gipi", g);
        okhttp3.RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("request_descriptor", null, okhttp3.RequestBody.create(JSON, irdg.getJSONString().getBytes("UTF-8")))
                .build();
        Request requestg = new Request.Builder()
                .url(urlChiamata)
                .post(body)
                .build();
        OkHttpClient client = new OkHttpClient();
        System.out.println("La richiesta --> " + requestg.toString());
        Response responseg = client.newCall(requestg).execute();
//        if (!responseg.isSuccessful()) {
//            System.out.println("La risposta --> " + responseg.toString());
//            throw new IOException("La fascicolazione non è andata a buon fine.");
//        }

        return responseg;
    }

    public JsonObject pubblicaIter(Iter i, DocumentoIter doc, List<RegistroTipoProcedimento> registriTipoProc) throws IOException, GipiPubblicazioneException {

        JsonObject statoPubblicazioni = new JsonObject();

        if (registriTipoProc.isEmpty()) {
            JPQLQuery<RegistroTipoProcedimento> query = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
            registriTipoProc = query
                    .from(qRegistroTipoProcedimento)
                    .where(qRegistroTipoProcedimento.idTipoProcedimento.id.eq(i.getIdProcedimento()
                            .getIdAziendaTipoProcedimento().getIdTipoProcedimento().getId()))
                    .fetch();
        }

        okhttp3.RequestBody body = null;
        log.info("Inizio pubblicazione dell'iter con id " + i.getId() + " sugli opportuni registri...");
        String baseUrl = GetBaseUrls.getShalboApiUrl(i.getIdProcedimento().getIdAziendaTipoProcedimento().getIdAzienda().getId(), em, objectMapper);
        //String urlChiamata = "http://localhost:10011/shalbo-api/registroaccessi";
        String urlChiamata;
        String token = tokenGenerator.getToken(i.getIdProcedimento().getIdAziendaTipoProcedimento().getIdAzienda());
        Registro registro;
        for (RegistroTipoProcedimento reg : registriTipoProc) {
            urlChiamata = baseUrl;
            registro = reg.getIdRegistro();
            switch (registro.getCodice()) {
                case "REGISTRO_ACCESSI":
                    log.info("Pubblico sul registro degli accessi...");
                    urlChiamata += registroAccessiPath;
                    log.info("URL Chiamata = " + urlChiamata);
                    RegistroAccessi iterAlbo = buildaRegistroAccessi(i, doc);
                    body = okhttp3.RequestBody.create(JSON, iterAlbo.getJSONString().getBytes("UTF-8"));
                    break;
                default:
                    log.info("Codice di registro non valido");
                    continue;
            }
            Request requestg = new Request.Builder()
                    .url(urlChiamata).addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .post(body)
                    .build();
            OkHttpClient client = new OkHttpClient();

            RegistroIter registroIter = new RegistroIter();
            registroIter.setIdIter(i);
            registroIter.setIdRegistro(registro);
            log.info("Effettuo la chiamata a shalbo...");
            try {
                Response responseg = client.newCall(requestg).execute();
                log.info("RISPOSTA = " + responseg.toString());
                if (responseg.isSuccessful()) {
                    log.info("Pubblicazione avvenuta con successo.");
                    log.info("Aggiorno il database...");
                    RegistroAccessi registroAccessi = Marshaller.parse(responseg.body().string(), RegistroAccessi.class);
                    registroIter.setNumeroPubblicazione(registroAccessi.getNumeroPubblicazione());
                    registroIter.setAnnoPubblicazione(registroAccessi.getAnnoPubblicazione());

                    statoPubblicazioni.addProperty(registro.getCodice(), "OK - N.Pubb " + registroAccessi.getNumeroPubblicazione());
                } else {
                    statoPubblicazioni.addProperty(registro.getCodice(), "ERROR");
                    throw new GipiPubblicazioneException("La pubblicazione non è andata a buon fine.");
                }
            } catch (IOException | GipiPubblicazioneException ex) {
                log.error("Errore: " + ex);
            } finally {
                log.info("Salvo nella registri iter..");
                em.persist(registroIter);
                em.flush();
                log.info("Salvataggio effettuato.");
            }
        }
        return statoPubblicazioni;
    }

    private RegistroAccessi buildaRegistroAccessi(Iter i, DocumentoIter doc) {
        JPQLQuery<EventoIter> queryEventiIter = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);

        // Trovo il documento che ha creato l'iter
        EventoIter evIniz = queryEventiIter
                .from(qEventoIter)
                .where(qEventoIter.idIter.id.eq(i.getId()).and(qEventoIter.idEvento.id.eq(1)))
                .fetchOne();
        // Trovo il documento che ha chiuso l'iter
//        EventoIter evChius = queryEventiIter
//                .from(qEventoIter)
//                .where(qEventoIter.idIter.id.eq(i.getId()).and(qEventoIter.idEvento.id.eq(2)))
//                .fetchOne();

        EnumMap<Esiti, String> esitiMap = new EnumMap(Esiti.class);
        esitiMap.put(Esiti.ACCOLTO, "Accolto");
        esitiMap.put(Esiti.RIFIUTO_TOTALE, "Rifiuto totale");
        esitiMap.put(Esiti.RIFIUTO_PARZIALE, "Rifiuto parziale");
        esitiMap.put(Esiti.RITIRATO, "Ritirato");
        RegistroAccessi iterAlbo = new RegistroAccessi();

        iterAlbo.setOggetto(i.getOggetto());
        iterAlbo.setTipoProcedimento(i.getIdProcedimento().getIdAziendaTipoProcedimento().getIdTipoProcedimento().getNome());
        // Segnaposto per TipoProcedimento Precedente, per quando sarà inserito
        iterAlbo.setModalitaCollegamento("Undefined"); // Campo ancora da definire
        iterAlbo.setUoProcedente(i.getIdProcedimento().getIdStruttura().getNome());
        iterAlbo.setResponsabileProcedimento(i.getIdResponsabileProcedimento().getIdPersona().getDescrizione());
        iterAlbo.setCodiceRegistroIniziativa(evIniz.getIdDocumentoIter().getRegistro());
        iterAlbo.setRegistroIniziativa(evIniz.getIdDocumentoIter().getRegistro());
        iterAlbo.setNumeroRegistroIniziativa(evIniz.getIdDocumentoIter().getNumeroRegistro());
        iterAlbo.setAnnoRegistroIniziativa(evIniz.getIdDocumentoIter().getAnno());
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        iterAlbo.setDataIniziativa(formatter.format(i.getDataAvvio()));
        Boolean presenzaControinteressati = i.getPresenzaControinteressati();
        iterAlbo.setControinteressati(presenzaControinteressati == null? false: presenzaControinteressati);
        iterAlbo.setEsito(esitiMap.get(Esiti.valueOf(i.getEsito())));
        iterAlbo.setCodiceRegistroChiusura(doc.getRegistro());
        iterAlbo.setRegistroChiusura(doc.getRegistro());
        iterAlbo.setNumeroRegistroChiusura(doc.getNumeroRegistro());
        iterAlbo.setAnnoRegistroChiusura(doc.getAnno());
        iterAlbo.setDataChiusura(formatter.format(i.getDataChiusura()));
        iterAlbo.setSintesiMotivazioneRifuto(i.getEsitoMotivazione());

        return iterAlbo;
    }
    
    public void aggiornaProcCacheEloggaEvento(int idIter, 
        int idUtenteResponsabile, int idStrutturaResponsabile, 
        Utente utenteLoggato, EntitiesCachableUtilities entitiesCachableUtilities) {
        Iter i = GetEntityById.getIter(idIter, em);
        ProcedimentoCache pc = i.getProcedimentoCache();
        Utente nuovoResponsabile = GetEntityById.getUtente(idUtenteResponsabile, em);
        Struttura nuovaStrutturaResp = GetEntityById.getStruttura(idStrutturaResponsabile, em);
        Evento eventoModifica = entitiesCachableUtilities.loadEventoByCodice("modifica_iter");
        EventoIter ei = new EventoIter();
        FaseIter fi = getFaseIter(i);
        ei.setNote("L'utente " + utenteLoggato.getIdPersona().getDescrizione()
                + " ha cambiato il responsabile del procedimento da "
                + i.getIdResponsabileProcedimento().getIdPersona().getDescrizione() + " ("
                + i.getIdStrutturaResponsabileProcedimento().getNome() + ")"
                + " a " + nuovoResponsabile.getIdPersona().getDescrizione() + " ("
                + nuovaStrutturaResp.getNome() + ").");
        ei.setIdEvento(eventoModifica);
        ei.setIdIter(i);
        ei.setAutore(utenteLoggato);
        ei.setDataOraEvento(new Date());
        ei.setIdFaseIter(fi);
        em.persist(ei);
        i.setIdResponsabileProcedimento(nuovoResponsabile);
        i.setIdStrutturaResponsabileProcedimento(nuovaStrutturaResp);
        em.merge(i);
    }

    public FaseIter getFaseIter(Iter i) {
        QFaseIter qFaseIter = QFaseIter.faseIter;
        JPQLQuery<FaseIter> q = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);
        System.out.println("Funzione getFaseIter(Iter i)");
        System.out.println("Iter i: " + i.toString());
        System.out.println("Carico la FaseIter");
        FaseIter fi = q
                .from(qFaseIter)
                .where(qFaseIter.idIter.id.eq(i.getId())
                        .and(qFaseIter.idFase.id.eq(i.getIdFaseCorrente().getId())))
                .orderBy(qFaseIter.dataInizioFase.desc())
                .fetchFirst();
        System.out.println("Ritorno la FaseIter " + fi.toString());
        return fi;
    }
    
    public Iter getIterById(int idIter) {
        JPQLQuery<Iter> queryIter = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);
        Iter iter = queryIter
                .from(QIter.iter)
                .where(QIter.iter.id.eq(idIter))
                .fetchOne();
        return iter;
    }
    
    public MotivoPrecedente getMotivoPrecedenteByCodice(String codiceMotivoPrecedente){
        JPQLQuery<MotivoPrecedente> queryMotivoPrecedente = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);
        MotivoPrecedente mp = queryMotivoPrecedente
                .from(QMotivoPrecedente.motivoPrecedente)
                .where(QMotivoPrecedente.motivoPrecedente.codice.eq(codiceMotivoPrecedente))
                .fetchOne();
        return mp;
    }
    
    public void eventoIterCambioOggetto(Iter iNew, Iter iOld, EntityManager entityManager, UtenteCachable utente) {
        Utente autore = GetEntityById.getUtente((int) utente.get(UtenteCachable.KEYS.ID), em);
        
        //JPQLQuery<Iter> queryIter = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);
        //Iter iterOld = getIterById(iNew.getId());
        EventoIter ei = new EventoIter();
        ei.setIdFaseIter(iterUtilities.getFaseIter(iNew));
        Evento e = entitiesCachableUtilities.loadEventoByCodice("modifica_iter");
        ei.setIdEvento(e);
        ei.setIdIter(iNew);
        ei.setDataOraEvento(new Date());
        ei.setAutore(autore);
        ei.setNote("L'utente " + autore.getIdPersona().getDescrizione()
                + " ha cambiato l'oggetto dell'iter da:\n\""
                + iOld.getOggetto() + "\" a:\n\""
                + iNew.getOggetto() + "\".");
        entityManager.persist(ei);
    }
    
    public String setIdCatenaAndPrecedenza(Integer idIter, Integer catena, Integer precedente){
        String q = "select * from gipi.update_catena(?, ?, ?)";
        Query query = em.createNativeQuery(q);
        query.setParameter(1, idIter);
        query.setParameter(2, catena);
        query.setParameter(3, precedente);
        log.info("Query che lancio", query.toString());
        JSONObject jo = new JSONObject();
        return jo.put("risultato", query.getSingleResult()).toString();
    }
}