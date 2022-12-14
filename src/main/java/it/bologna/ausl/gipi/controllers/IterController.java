package it.bologna.ausl.gipi.controllers;

import it.bologna.ausl.gipi.utils.classes.GestioneStatiParams;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.gipi.Fase;
import it.bologna.ausl.entities.gipi.Iter;
import it.bologna.ausl.entities.gipi.MotivoPrecedente;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import it.bologna.ausl.gipi.process.ProcessSteponParams;
import org.springframework.beans.factory.annotation.Autowired;
import it.bologna.ausl.gipi.process.Process;
import javax.persistence.EntityManager;
import org.springframework.web.bind.annotation.RequestParam;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;
import it.bologna.ausl.entities.baborg.Azienda;
import it.bologna.ausl.entities.baborg.QStruttura;
import it.bologna.ausl.entities.baborg.Struttura;
import it.bologna.ausl.entities.baborg.Utente;
import it.bologna.ausl.entities.cache.cachableobject.AziendaCachable;
import it.bologna.ausl.entities.cache.cachableobject.UtenteCachable;
import it.bologna.ausl.entities.gipi.DocumentoIter;
import it.bologna.ausl.entities.gipi.Evento;
import it.bologna.ausl.entities.gipi.EventoIter;
import it.bologna.ausl.entities.gipi.FaseIter;
import it.bologna.ausl.entities.gipi.ProcedimentoCache;
import it.bologna.ausl.entities.gipi.QDocumentoIter;
import it.bologna.ausl.entities.gipi.QEvento;
import it.bologna.ausl.entities.gipi.QEventoIter;
import it.bologna.ausl.entities.gipi.QFaseIter;
import it.bologna.ausl.entities.gipi.QIter;
import it.bologna.ausl.entities.gipi.QRegistroTipoProcedimento;
import it.bologna.ausl.entities.gipi.RegistroTipoProcedimento;
import it.bologna.ausl.entities.gipi.SpettanzaAnnullamento;
import it.bologna.ausl.entities.gipi.Stato;
import it.bologna.ausl.entities.gipi.utilities.EntitiesCachableUtilities;
import it.bologna.ausl.entities.repository.AziendaRepository;
import it.bologna.ausl.entities.repository.IterRepository;
import it.bologna.ausl.entities.utilities.response.controller.ControllerHandledExceptions;
import it.bologna.ausl.entities.utilities.response.exceptions.ForbiddenResponseException;
import it.bologna.ausl.entities.utilities.response.exceptions.InternalServerErrorResponseException;
import it.bologna.ausl.gipi.config.scheduler.jobs.JobAggiornaCampiIter;
import it.bologna.ausl.gipi.exceptions.GipiDatabaseException;
import it.bologna.ausl.gipi.exceptions.GipiPubblicazioneException;
import it.bologna.ausl.gipi.exceptions.GipiRequestParamsException;
import it.bologna.ausl.gipi.frullinotemp.utils.NotifyScadenzaSospensioneTask;
import static it.bologna.ausl.gipi.frullinotemp.utils.NotifyScadenzaSospensioneTask.JSON;
import static it.bologna.ausl.gipi.frullinotemp.utils.NotifyScadenzaSospensioneTask.MESSAGGIO;
import it.bologna.ausl.gipi.process.CreaIter;
import static it.bologna.ausl.gipi.process.CreaIter.JSON;
import it.bologna.ausl.gipi.process.CreaIter.OperazioniFascicolo;
import it.bologna.ausl.gipi.utils.GetBaseUrls;
import it.bologna.ausl.gipi.utils.GetEntityById;
import it.bologna.ausl.gipi.utils.GipiUtilityFunctions;
import it.bologna.ausl.gipi.utils.IterUtilities;
import it.bologna.ausl.ioda.iodaobjectlibrary.Fascicoli;
import it.bologna.ausl.ioda.iodaobjectlibrary.Fascicolo;
import it.bologna.ausl.ioda.iodaobjectlibrary.IodaRequestDescriptor;
import it.bologna.ausl.ioda.iodaobjectlibrary.Researcher;
import it.bologna.ausl.primuscommanderclient.PrimusCommandParams;
import it.bologna.ausl.primuscommanderclient.RefreshBoxDatiDiArchivioCommandParams;
import it.nextsw.olingo.interceptor.exception.OlingoRequestRollbackException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.servlet.http.HttpServletRequest;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.client.HttpResponseException;
import org.joda.time.Days;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 * @author f.gusella
 */
@RestController
@RequestMapping(value = "${custom.mapping.url.root}" + "/iter")
@PropertySource("classpath:query.properties")
public class IterController extends ControllerHandledExceptions {

    private final Integer FASCICOLAZIONE_ERROR = 0;

    @Autowired
    Process process;

    @Autowired
    CreaIter creaIter;

    @Autowired
    EntityManager em;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    IterUtilities iterUtilities;

    @Autowired
    IterRepository iterRepository;

    @Autowired
    JobAggiornaCampiIter aggiornaCampiIter;

    @Autowired
    @Qualifier("GipiUtilityFunctions")
    GipiUtilityFunctions utilityFunctions;

    @Value("${getFascicoliUtente}")
    private String bdsGetFascicoliUtentePath;

    @Value("${babelGestisciIter}")
    private String babelGestisciIterPath;

    @Value("${proctonGestisciIter}")
    private String proctonGestisciIterPath;

    @Value("${deteGestisciIter}")
    private String deteGestisciIterPath;

    @Value("${deliGestisciIter}")
    private String deliGestisciIterPath;

    @Value("${proctonDeleteDocumentoIter}")
    private String proctonDeleteDocumentoIterPath;

    @Value("${deteDeleteDocumentoIter}")
    private String deteDeleteDocumentoIterPath;

    @Value("${deliDeleteDocumentoIter}")
    private String deliDeleteDocumentoIterPath;

    @Value("${updateGdDoc}")
    private String updateGdDocPath;

    @Value("${getFascicoliConPermessi}")
    private String getFascicoliConPermessi;

    @Value("${hasUserAnyPermissionOnFascicolo}")
    private String hasUserAnyPermissionOnFascicoloPath;

    @Value("${updateFascicoloGediPath}")
    private String updateFascicoloGediPath;

    @Value("${babelsuite.uri.localhost}")
    private String localhostBaseUrl;

    @Value("${updateFascicolo}")
    private String updateFascicoloPath;

    @Value("${babelAnnullaIter}")
    private String babelAnnullaIterPath;

    @Value("${inviaNotificheWebApi}")
    private String inviaNotificheWebApiPath;

    public static enum GetFascicoli {
        TIPO_FASCICOLO, SOLO_ITER, CODICE_FISCALE, ANCHE_CHIUSI, DAMMI_PERMESSI
    }

    public static enum WebApi {
        GESTISCI_STATO_ITER,
        CANCELLA_DOCUMENTO_ITER
    }

    public static enum AzioneRichiesta {
        CAMBIO_DI_STATO("cambio_di_stato"),
        CAMBIO_DI_STATO_DIFFERITO("cambio_di_stato_differito"),
        ASSOCIAZIONE("associazione"),
        ASSOCIAZIONE_DIFFERITA("associazione_differita"),
        CREAZIONE("creazione");

        private final String text;

        /**
         * @param text
         */
        AzioneRichiesta(final String text) {
            this.text = text;
        }

        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return text;
        }
    }

    public static enum CodiceEvento {
        AVVIO_ITER("avvio_iter"),
        CHIUSURA_ITER("chiusura_iter"),
        AGGIUNTA_DOCUMENTO("aggiunta_documento"),
        APERTURA_SOSPENSIONE("apertura_sospensione"),
        CHIUSURA_SOSPENSIONE("chiusura_sospensione"),
        ITER_IN_CORSO("iter_in_corso"),
        MODIFICA_ITER("modifica_iter"),
        AGGIUNTA_PRECEDENTE("aggiunta_precedente"),
        CANCELLAZIONE_PRECEDENTE("cancellazione_precedente"),
        ANNULLAMENTO_ITER("annullamento_iter");

        private final String text;

        CodiceEvento(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public static enum CodiceRegistro {
        PG("PG"),
        DELI("DELI"),
        DETE("DETE");

        private final String text;

        CodiceRegistro(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public static enum Applicazione {
        PROCTON("procton"),
        DELI("deli"),
        DETE("dete");

        private final String text;

        Applicazione(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public static enum AzioneSuiPrecedenti {
        ADD("ADD"),
        DEL("DEL");

        private final String text;

        AzioneSuiPrecedenti(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public static final okhttp3.MediaType JSON = okhttp3.MediaType.parse("application/json; charset=utf-8");

    @Autowired
    private EntitiesCachableUtilities entitiesCachableUtilities;

    @Autowired
    AziendaRepository aziendaRepository;

    QIter qIter = QIter.iter;
    QDocumentoIter qDocumentoIter = QDocumentoIter.documentoIter;
    QEventoIter qEventoIter = QEventoIter.eventoIter;
    QEvento qEvento = QEvento.evento;
    QStruttura qStruttura = QStruttura.struttura;
    QRegistroTipoProcedimento qRegistroTipoProcedimento = QRegistroTipoProcedimento.registroTipoProcedimento;

    private static final Logger log = LoggerFactory.getLogger(IterController.class);

    @RequestMapping(value = "avviaNuovoIter", method = RequestMethod.POST)
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public ResponseEntity<Iter> AvviaNuovoIter(@RequestBody IterParams data, HttpServletRequest request)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException, OlingoRequestRollbackException, org.json.simple.parser.ParseException, Throwable {

        Iter i = creaIter.creaIter(data, request.getServerName().equalsIgnoreCase("localhost"));
        if (data.getIterPrecedenteString() != null) {
            log.debug("Devo aggiungere anche il precedente: " + data.getIterPrecedenteString());
            try {
                JSONObject jo = (JSONObject) new JSONParser().parse(data.getIterPrecedenteString());
                JsonObject params = new JsonObject();
                params.addProperty("idIter", i.getId());
                params.addProperty("azione", AzioneSuiPrecedenti.ADD.toString());
                params.addProperty("idIterPrecedente", (Number) jo.get("idIterPrecedente"));
                JSONObject motivoPrecedente = (JSONObject) jo.get("motivoPrecedente");
                params.addProperty("codiceMotivo", (String) motivoPrecedente.get("codice"));
                params.addProperty("noteMotivoPrecedente", jo.get("noteMotivoPrecedente") != null ? (String) jo.get("noteMotivoPrecedente") : null);
                ResponseEntity res = settaPrecedente(params.toString(), request);
                log.debug(res.toString());

            } catch (Throwable t) {
                log.error("ERRORE NEL SETTING DEL PRECEDENTE");
                t.printStackTrace();
            }
        }

        JsonObject o = new JsonObject();
        o.addProperty("idIter", i.getId().toString());
        o.addProperty("numero", i.getNumero());

        return new ResponseEntity(o.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "stepOn", method = RequestMethod.POST)
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public ResponseEntity stepOn(@RequestBody SteponParams data, HttpServletRequest request) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, ParseException, GipiRequestParamsException, IOException, IOException {
//        Class<?> clazz = data.getClass();
//        Field field = clazz.getField("iter"); //Note, this can throw an exception if the field doesn't exist.
//        Object fieldValue = field.get(data);

        JPQLQuery<Iter> queryIter = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);

        Iter iter = queryIter
                .from(qIter)
                .where(qIter.id.eq(data.getIdIter()))
                .fetchOne();

        ProcessSteponParams processSteponParams = new ProcessSteponParams();

//        HashMap h = new HashMap<String, Object>();
//        h.put("dataPassaggio", data.getDataPassaggio());
//        h.put("documento", data.getDocumento());
//        h.put("esito", data.getEsito());
//        h.put("motivazioneEsito", data.getMotivazioneEsito());
//        h.put("notePassaggio", data.getNotePassaggio());
//        processSteponParams.setParams(h);
        processSteponParams.insertParam("dataPassaggio", data.getDataPassaggio());
        processSteponParams.insertParam("codiceRegistroDocumento", data.getCodiceRegistroDocumento());
        processSteponParams.insertParam("numeroDocumento", data.getNumeroDocumento());
        processSteponParams.insertParam("annoDocumento", data.getAnnoDocumento());
        processSteponParams.insertParam("notePassaggio", data.getNotePassaggio());
        processSteponParams.insertParam("esito", data.getEsito());
        processSteponParams.insertParam("motivazioneEsito", data.getMotivazioneEsito());
        processSteponParams.insertParam("oggettoDocumento", data.getOggettoDocumento());
        processSteponParams.insertParam("idOggettoOrigine", data.getIdOggettoOrigine());
        processSteponParams.insertParam("tipoOggettoOrigine", data.getTipoOggettoOrigine());
        processSteponParams.insertParam("descrizione", data.getDescrizione());

        process.stepOn(iter, processSteponParams, request.getServerName().equalsIgnoreCase("localhost"));

        // Devo salvare l'iter, il procedimento_cache, la fase iter, l'evento iter, creare il fascicolo dell'iter
//        return new ResponseEntity(new ArrayList<Object>() , HttpStatus.OK);
        return new ResponseEntity(data, HttpStatus.OK);
    }

    @RequestMapping(value = "getCurrentFase", method = RequestMethod.GET)
    public ResponseEntity<Fase> getCurrentFase(@RequestParam("idIter") Integer idIter) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        JPQLQuery<Iter> queryIter = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);

        Iter iter = queryIter
                .from(qIter)
                .where(qIter.id.eq(idIter))
                .fetchOne();

        Fase currentFase = process.getCurrentFase(iter);

//        JsonObject jsonFase = new JsonObject();
//        jsonFase.addProperty("nomeFase", currentFase.getNomeFase());
//
//        return new ResponseEntity(jsonFase.toString(), HttpStatus.OK);
        return new ResponseEntity(currentFase, HttpStatus.OK);
    }

    @RequestMapping(value = "getProcessStatus", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
    public ResponseEntity getProcessStatus(@RequestParam("idIter") Integer idIter) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, GipiDatabaseException {

        // TODO: QUI BISOGNERA USARE L'OGGETTO PROCESS STATUS, ora non lo uso perch?? devo restituire solo i nomi delle fasi perch?? se no da errore
        JPQLQuery<Iter> queryIter = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);

        Iter iter = queryIter
                .from(qIter)
                .where(qIter.id.eq(idIter))
                .fetchOne();

        Fase currentFase = process.getCurrentFase(iter);
        Fase nextFase = process.getNextFase(iter);
//        if (nextFase == null) {
//            throw new GipiDatabaseException("La fase successiva e' null");
//        }

        JsonObject jsonCurrFase = new JsonObject();
        JsonObject jsonNextFase = new JsonObject();
        jsonCurrFase.addProperty("nomeFase", currentFase.getNome());
        jsonCurrFase.addProperty("faseDiChiusura", currentFase.getFaseDiChiusura());
        if (nextFase != null) {
            jsonNextFase.addProperty("nomeFase", nextFase.getNome());
            jsonNextFase.addProperty("faseDiChiusura", nextFase.getFaseDiChiusura());
        }
        JsonObject processStatus = new JsonObject();
        processStatus.addProperty("currentFase", jsonCurrFase.toString());
        processStatus.addProperty("nextFase", jsonNextFase.toString());

        return new ResponseEntity(processStatus.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "getUltimaSospensione", method = RequestMethod.GET)
    public ResponseEntity getUltimaSospensione(@RequestParam("idIter") Integer idIter) {
        JPQLQuery<EventoIter> q = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);

        EventoIter ei = q
                .from(qEventoIter)
                .where(qEventoIter.idIter.id.eq(idIter)
                        .and(qEventoIter.idEvento.eq(JPAExpressions.selectFrom(qEvento).where(qEvento.codice.eq("apertura_sospensione")))))
                .orderBy(qEventoIter.dataOraEvento.desc()).fetchFirst();

        return new ResponseEntity(ei.getDataOraEvento(), HttpStatus.OK);
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

    @RequestMapping(value = "gestisciStatoIter", method = RequestMethod.POST)
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public ResponseEntity gestisciStatoIter(@RequestBody GestioneStatiParams gestioneStatiParams) throws IOException, GipiPubblicazioneException {
        log.info("Gestisci Stato Iter -> Operazione da svolgere: " + gestioneStatiParams.getAzione());
        log.info("Parametri passati = " + gestioneStatiParams.toString());
        if (gestioneStatiParams.getNumeroDocumento().equals("")) {
            return gestisciStatoIterDaBozza(gestioneStatiParams);
        }

        Utente u = GetEntityById.getUtenteFromPersonaByCodiceFiscaleAndIdAzineda(gestioneStatiParams.getCfAutore(), gestioneStatiParams.getIdAzienda(), em);
        Iter i = GetEntityById.getIter(gestioneStatiParams.idIter, em);
        //Evento e = GetEntityById.getEventoByCodice(gestioneStatiParams.getStatoRichiesto().getCodice().toString(), em);
        Evento eventoDiCambioStato = new Evento();
        FaseIter fi = getFaseIter(i);

        Boolean isChiusura = false;

//        Stato s = GetEntityById.getStatoById(gestioneStatiParams.getStato(), em);
        if (gestioneStatiParams.getAzione().equals(AzioneRichiesta.ASSOCIAZIONE.toString()) || gestioneStatiParams.getAzione().equals(AzioneRichiesta.ASSOCIAZIONE_DIFFERITA.toString())) // qui siamo se stiamo solo aggiungendo un documento
        {
            eventoDiCambioStato = this.entitiesCachableUtilities.loadEventoByCodice("aggiunta_documento");
        } else if (gestioneStatiParams.getAzione().equals(AzioneRichiesta.CAMBIO_DI_STATO.toString()) || gestioneStatiParams.getAzione().equals(AzioneRichiesta.CAMBIO_DI_STATO_DIFFERITO.toString())) {
            Stato s = GetEntityById.getStatoByCodice(gestioneStatiParams.getStatoRichiesto(), em);
            // Questa non ?? una cosa bellissima e bisognerebbe fare un refactoring anche di questo
            // Infatti non abbiamo un modo automatico per determinare l'Evento in base allo Stato, n?? abbiamo un enum sugli eventi
            if (s.getCodice().equals(Stato.CodiciStato.SOSPESO.toString())) {
                eventoDiCambioStato = this.entitiesCachableUtilities.loadEventoByCodice("apertura_sospensione");
            } else if (s.getCodice().equals(Stato.CodiciStato.CHIUSO.toString())) {
                eventoDiCambioStato = this.entitiesCachableUtilities.loadEventoByCodice("chiusura_iter");
                i.setDataChiusura(gestioneStatiParams.getDataEvento());
                i.setEsito(gestioneStatiParams.getEsito());
                i.setEsitoMotivazione(gestioneStatiParams.getEsitoMotivazione());
                isChiusura = true;
            } else {
                eventoDiCambioStato = this.entitiesCachableUtilities.loadEventoByCodice("chiusura_sospensione");
            }

            // Aggiorno l'iter
            i.setIdStato(s);
        }

        em.persist(i);

        Boolean isDifferito = gestioneStatiParams.isDifferito();

        // Lo definisco qui fuori perch?? mi pu?? servire due volte
        JsonObject datiAggiuntivi = new JsonObject();

        DocumentoIter d;
        if (!isDifferito) {
            // Creo il documento iter
            d = new DocumentoIter();
            d.setAnno(gestioneStatiParams.getAnnoDocumento());
            d.setIdIter(i);
            d.setNumeroRegistro(gestioneStatiParams.getNumeroDocumento());
            d.setRegistro(gestioneStatiParams.getCodiceRegistroDocumento());
            d.setOggetto(gestioneStatiParams.getOggettoDocumento());
            d.setDescrizione(gestioneStatiParams.getDescrizione());
            d.setIdOggetto(gestioneStatiParams.getIdOggettoOrigine());
            d.setParziale(Boolean.FALSE);
            datiAggiuntivi.addProperty("azione", gestioneStatiParams.getAzione());
            datiAggiuntivi.addProperty("statoRichiesto", gestioneStatiParams.getStatoRichiesto());
            d.setDatiAggiuntivi(datiAggiuntivi.toString());
            em.persist(d);
            em.flush();
        } else {
            JPQLQuery<DocumentoIter> queryDocumentoIter = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);

            d = queryDocumentoIter
                    .from(qDocumentoIter)
                    .where(qDocumentoIter.idOggetto.eq(gestioneStatiParams.getIdOggettoOrigine())
                            .and(qDocumentoIter.idIter.eq(i)).and(qDocumentoIter.parziale.eq(Boolean.TRUE)))
                    .fetchOne();
            d.setAnno(gestioneStatiParams.getAnnoDocumento());
            d.setNumeroRegistro(gestioneStatiParams.getNumeroDocumento());
            d.setOggetto(gestioneStatiParams.getOggettoDocumento()); // Non so se riaggiungerlo o lasciare la descrizione di quando era parziale
            d.setDescrizione(gestioneStatiParams.getDescrizione());
            d.setParziale(Boolean.FALSE);
            em.merge(d);
        }

        // Creo l'evento iter
        EventoIter ei = new EventoIter();
        ei.setIdDocumentoIter(d);
        ei.setNote(gestioneStatiParams.getNote());
        ei.setIdEvento(eventoDiCambioStato);
        ei.setIdIter(i);
        ei.setAutore(u);
        ei.setDataOraEvento(isDifferito ? new Date() : gestioneStatiParams.getDataEvento());
        ei.setIdFaseIter(fi);
        em.persist(ei);

        log.info("PRIMA: " + i.getGiorniSospensioneTrascorsi());
        log.info("FUNZIONE: " + aggiornaCampiIter.calcolaGiorniSospensioneTrascorsi(i));
        i.setGiorniSospensioneTrascorsi(aggiornaCampiIter.calcolaGiorniSospensioneTrascorsi(i));
        log.info("POST: " + i.getGiorniSospensioneTrascorsi());
        em.persist(i);

        // Chiamo la web api solo se l'azione non ?? "cambio_di_stato_differito"
        // (perch?? il lavoro parte Babel lo esegue gi?? il mestiere che chiama questa)
        if (!isDifferito) {
            // Fascicolo il documento se non ?? differito in quanto viene gi?? fascicolato

            Response fascicolato = iterUtilities.inserisciFascicolazione(i, gestioneStatiParams, u.getIdPersona().getCodiceFiscale());
            if (!fascicolato.isSuccessful()) {
                throw new InternalServerErrorResponseException(FASCICOLAZIONE_ERROR, "La fascicolazione non ?? andata a buon fine.", fascicolato.body() != null ? fascicolato.body().string() : null);
            }

            // Comunico a Babel l'associazione documento/iter appena avvenuta
            String urlChiamata = GetBaseUrls.getBabelSuiteWebApiUrl(i.getIdProcedimento().getIdAziendaTipoProcedimento().getIdAzienda().getId(), em, objectMapper) + getWebApiPathByIdApplicazione(gestioneStatiParams.getIdApplicazione(), WebApi.GESTISCI_STATO_ITER);

            // localhost da commentare
            // urlChiamata = "http://localhost:8080" + getWebApiPathByIdApplicazione(gestioneStatiParams.getIdApplicazione(), WebApi.GESTISCI_STATO_ITER);
            //String baseUrl = "http://gdml:8080" + baseUrlBabelGestisciIter;
            //        gestioneStatiParams.setCfResponsabileProcedimento(i.getIdResponsabileProcedimento().getIdPersona().getCodiceFiscale());
            //        gestioneStatiParams.setAnnoIter(i.getAnno());
            //        gestioneStatiParams.setNomeProcedimento(i.getIdProcedimento().getIdAziendaTipoProcedimento().getIdTipoProcedimento().getNome());
            JsonObject o = new JsonObject();
            o.addProperty("idIter", i.getId());
            o.addProperty("numeroIter", i.getNumero());
            o.addProperty("annoIter", i.getAnno());
            o.addProperty("cfResponsabileProcedimento", i.getIdResponsabileProcedimento().getIdPersona().getCodiceFiscale());
            o.addProperty("nomeProcedimento", i.getIdProcedimento().getIdAziendaTipoProcedimento().getIdTipoProcedimento().getNome());
            o.addProperty("codiceRegistroDocumento", gestioneStatiParams.getCodiceRegistroDocumento());
            o.addProperty("numeroDocumento", gestioneStatiParams.getNumeroDocumento());
            o.addProperty("annoDocumento", gestioneStatiParams.getAnnoDocumento());
            o.addProperty("idOggettoOrigine", gestioneStatiParams.getIdOggettoOrigine());
            // Tra i dati aggiuntivi metto cosa fa questo documento sull'iter
            o.addProperty("datiAggiuntivi", datiAggiuntivi.toString());
            o.addProperty("glogParams", gestioneStatiParams.getGlogParams());
            o.addProperty("modificaAssociazioneParziale", 0); // non sto modificando un'associazione parziale

            okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, o.toString().getBytes("UTF-8"));

            Request requestg = new Request.Builder()
                    .url(urlChiamata)
                    .addHeader("X-HTTP-Method-Override", "associaDocumento")
                    .post(body)
                    .build();

            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(200, TimeUnit.SECONDS)
                    .connectTimeout(200, TimeUnit.SECONDS)
                    .writeTimeout(200, TimeUnit.SECONDS).build();
            Response responseg = client.newCall(requestg).execute();

            if (!responseg.isSuccessful()) {
                throw new IOException("La chiamata a Babel non ?? andata a buon fine.");
            }
        }

        // Lancio comando a primus per aggiornamento istantaneo del box dati di archivio
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UtenteCachable userInfo = (UtenteCachable) authentication.getPrincipal();
        String codiceFiscaleUtenteLoggato = (String) userInfo.get(UtenteCachable.KEYS.CODICE_FISCALE);
        Azienda aziendaUtenteLoggato = aziendaRepository.findById((Integer) ((AziendaCachable) userInfo.get(UtenteCachable.KEYS.AZIENDA_LOGIN)).get(AziendaCachable.KEYS.ID)).get();
        List<String> cfUtentiDaRefreshare = new ArrayList<>();
        cfUtentiDaRefreshare.add(codiceFiscaleUtenteLoggato);
        PrimusCommandParams command = new RefreshBoxDatiDiArchivioCommandParams();
        utilityFunctions.sendPrimusCommand(aziendaUtenteLoggato, cfUtentiDaRefreshare, command, gestioneStatiParams.getIdApplicazione());

        JsonObject obj = new JsonObject();
        obj.addProperty("idIter", i.getId().toString());

        if (isChiusura) {
            JPQLQuery<RegistroTipoProcedimento> query = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
            /* Controllo se l'iter deve essere pubblicato - Avr?? un elemento
             * nella lista per ogni registro in cui dovr?? essere pubblicato */
            log.info("Check pubblicazione iter...");
            List<RegistroTipoProcedimento> registriTipoProc = query
                    .from(qRegistroTipoProcedimento)
                    .where(qRegistroTipoProcedimento.idTipoProcedimento.id.eq(i.getIdProcedimento()
                            .getIdAziendaTipoProcedimento().getIdTipoProcedimento().getId()))
                    .fetch();
            if (!registriTipoProc.isEmpty()) {
                JsonObject pubblicazioni = iterUtilities.pubblicaIter(i, d, registriTipoProc);
                log.info("Stato pubblicazioni: " + pubblicazioni.toString());
            }
        }

        return new ResponseEntity(obj.toString(), HttpStatus.OK);
    }

    public ResponseEntity gestisciStatoIterDaBozza(@RequestBody GestioneStatiParams gestioneStatiParams) throws IOException {
        // Tutto quello che mi serve lo si ricava da GestioneStatiParams o nell'iter (che si ricava da GestioneStatiParams)
//        GestioneStatoIter gsi = new GestioneStatoIter();
//        gsi.gestisciStatoIterDaBozza(gestioneStatiParams);
        log.info("Il documento ?? una bozza.");
        // Recupero il codice fiscale dall'utente cacheable
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UtenteCachable userInfo = (UtenteCachable) authentication.getPrincipal();
        String codiceFiscaleUtenteLoggato = (String) userInfo.get(UtenteCachable.KEYS.CODICE_FISCALE);
        log.info("Recupero l'iter...");
        Iter i = GetEntityById.getIter(gestioneStatiParams.idIter, em);

        // Setto i datiAggiuntivi (sono quelli da usare alla numerazione del documento)
        log.info("Setto i datiAggiuntivi e preparo l'oggetto da passare..."); // sono quelli da usare alla numerazione del documento
        JsonObject datiAggiuntivi = new JsonObject();
        datiAggiuntivi.addProperty("cfAutore", gestioneStatiParams.getCfAutore());
        datiAggiuntivi.addProperty("idAzienda", gestioneStatiParams.getIdAzienda());
        datiAggiuntivi.addProperty("azione", gestioneStatiParams.getAzione());
        datiAggiuntivi.addProperty("statoRichiesto", gestioneStatiParams.getStatoRichiesto());
        datiAggiuntivi.addProperty("note", gestioneStatiParams.getNote());
        datiAggiuntivi.addProperty("esito", gestioneStatiParams.getEsito());
        datiAggiuntivi.addProperty("esitoMotivazione", gestioneStatiParams.getEsitoMotivazione());

        // Preparo l'oggetto da passare alla web api di associazione documento
        JsonObject o = new JsonObject();
        o.addProperty("idIter", i.getId());
        o.addProperty("numeroIter", i.getNumero());
        o.addProperty("annoIter", i.getAnno());
        o.addProperty("cfResponsabileProcedimento", i.getIdResponsabileProcedimento().getIdPersona().getCodiceFiscale());
        o.addProperty("nomeProcedimento", i.getIdProcedimento().getIdAziendaTipoProcedimento().getIdTipoProcedimento().getNome());
        o.addProperty("idOggettoOrigine", gestioneStatiParams.getIdOggettoOrigine());
        o.addProperty("numeroDocumento", gestioneStatiParams.getNumeroDocumento());
        o.addProperty("annoDocumento", gestioneStatiParams.getAnnoDocumento());
        o.addProperty("codiceRegistroDocumento", gestioneStatiParams.getCodiceRegistroDocumento());
        o.addProperty("datiAggiuntivi", datiAggiuntivi.toString());
        o.addProperty("glogParams", gestioneStatiParams.getGlogParams());

        log.info("Creo il documento iter parziale e salvo sul db di gipi...");
        log.info("Ma non ?? che per caso l'ho gi?? associato e devo cambiare?");
        // DocumentoIter d = new DocumentoIter();
        JPQLQuery<DocumentoIter> queryDocumentoIter = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);

        log.info("Allora provo a caricare il DocumentoIter.");
        DocumentoIter d = queryDocumentoIter
                .from(qDocumentoIter)
                .where(qDocumentoIter.idOggetto.eq(gestioneStatiParams.getIdOggettoOrigine())
                        .and(qDocumentoIter.idIter.eq(i)))
                .fetchOne();

        if (d != null) {
            log.info("Eh gi??, allora cambio i datiAggiuntivi --> " + datiAggiuntivi.toString());
            log.info("Un controllo di sicurezza: l'associazione ?? parziale?  --> " + d.getParziale().toString());
            d.setDatiAggiuntivi(datiAggiuntivi.toString());
            em.merge(d);
            o.addProperty("modificaAssociazioneParziale", -1);
        } else {
            o.addProperty("modificaAssociazioneParziale", 0); // non sto modificando un'associazione parziale
            log.info("No, non esiste ancora, allora faccio un'associazione ex novo");
            d = new DocumentoIter();
            d.setIdIter(i);
            d.setRegistro(gestioneStatiParams.getCodiceRegistroDocumento());
            d.setOggetto(gestioneStatiParams.getOggettoDocumento());
            d.setIdOggetto(gestioneStatiParams.getIdOggettoOrigine());
            d.setDescrizione(gestioneStatiParams.getDescrizione());
            d.setParziale(Boolean.TRUE);
            d.setDatiAggiuntivi(datiAggiuntivi.toString());
            em.persist(d);
            em.flush();
            log.info("Fascicolo il documento nel fascicolo dell'iter...");
            Response fascicolato = iterUtilities.inserisciFascicolazione(i, gestioneStatiParams, codiceFiscaleUtenteLoggato);
            if (!fascicolato.isSuccessful()) {
                throw new InternalServerErrorResponseException(FASCICOLAZIONE_ERROR, "La fascicolazione non ?? andata a buon fine.", fascicolato.body() != null ? fascicolato.body().string() : null);
            } else {
                log.info("Fascicolazione effettuata con successo!");
            }
        }

        okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, o.toString().getBytes("UTF-8"));
        System.out.println(o.toString());

        // Chiamata alla web api GestisciIter.associaDocumento
        log.info("Carico l'url della webApi ed effettuo la chiamata...");
        String urlChiamata = GetBaseUrls.getBabelSuiteWebApiUrl(gestioneStatiParams.getIdAzienda(), em, objectMapper) + getWebApiPathByIdApplicazione(gestioneStatiParams.getIdApplicazione(), WebApi.GESTISCI_STATO_ITER);

        // localhost da commentare
        // urlChiamata = "http://localhost:8080" + getWebApiPathByIdApplicazione(gestioneStatiParams.getIdApplicazione(), WebApi.GESTISCI_STATO_ITER);
        System.out.println(urlChiamata);
        Request requestg = new Request.Builder()
                .url(urlChiamata)
                .addHeader("X-HTTP-Method-Override", "associaDocumento")
                .post(body)
                .build();

        System.out.println(requestg.toString());
        OkHttpClient client = new OkHttpClient();
        Response responseg = client.newCall(requestg).execute();
        String responseBodyString = responseg.body().string();
        log.debug("RISPOSTA: ");
        log.debug(responseg.toString() + " body: " + responseBodyString);
        JsonObject obj = new JsonObject();
        if (!responseg.isSuccessful()) {
            throw new HttpResponseException(responseg.code(), "La chiamata a Babel non ?? andata a buon fine. " + responseg);
        } else if (responseBodyString.equals("0")) {
            obj.addProperty("idIter", "-1");
        } else {
            // ritorno un oggetto di ok
            log.info("Chiamata effettuata con successo!");
            obj.addProperty("idIter", i.getId().toString());
            obj.addProperty("object", responseg.toString());

            // Lancio comando a primus per aggiornamento istantaneo del box dati di archivio
            Azienda aziendaUtenteLoggato = aziendaRepository.findById((Integer) ((AziendaCachable) userInfo.get(UtenteCachable.KEYS.AZIENDA_LOGIN)).get(AziendaCachable.KEYS.ID)).get();
            List<String> cfUtentiDaRefreshare = new ArrayList<>();
            cfUtentiDaRefreshare.add(codiceFiscaleUtenteLoggato);
            PrimusCommandParams command = new RefreshBoxDatiDiArchivioCommandParams();
            utilityFunctions.sendPrimusCommand(aziendaUtenteLoggato, cfUtentiDaRefreshare, command, gestioneStatiParams.getIdApplicazione());
        }
        return new ResponseEntity(obj.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "hasPermissionOnFascicolo", method = RequestMethod.POST)
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public ResponseEntity hasPermissionOnFascicolo(@RequestBody String fascicolo) throws IOException {
//        int i = Integer.parseInt(data.get("utenteLoggato").toString());
//        String numerazioneGerarchica = data.get("numerazioneGerarchica").toString();

        // String baseUrl = "http://gdml:8080" + baseUrlhasPermissionOnFascicolo;  //gdml
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UtenteCachable userInfo = (UtenteCachable) authentication.getPrincipal();
        String codiceFiscale = (String) userInfo.get(UtenteCachable.KEYS.CODICE_FISCALE);

        AziendaCachable aziendaInfo = (AziendaCachable) userInfo.get(UtenteCachable.KEYS.AZIENDA_LOGIN);
        int idAzienda = (int) aziendaInfo.get(AziendaCachable.KEYS.ID);
        String urlChiamata = GetBaseUrls.getBabelSuiteBdsToolsUrl(idAzienda, em, objectMapper) + hasUserAnyPermissionOnFascicoloPath;

        // String localUrl =  " http://localhost:8081/" + hasUserAnyPermissionOnFascicoloPath;
        Researcher r = new Researcher(null, null, 0);
        HashMap additionalData = (HashMap) new java.util.HashMap();
        additionalData.put("user", codiceFiscale);
        // additionalData.put("ng", data.get("numerazioneGerarchica").toString());
        additionalData.put("ng", fascicolo);

        IodaRequestDescriptor irdg = new IodaRequestDescriptor("gipi", "gipi", r, additionalData);
        okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, irdg.getJSONString().getBytes("UTF-8"));
        // body = okhttp3.RequestBody.create(JSON, o.toString().getBytes("UTF-8"));

        Request requestg = new Request.Builder()
                .url(urlChiamata)
                .post(body)
                .build();

        // OkHttpClient client = new OkHttpClient();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Response responseg = client.newCall(requestg).execute();

        if (!responseg.isSuccessful()) {
            throw new IOException("La chiamata a Babel non ?? andata a buon fine. " + responseg);
        }

        log.debug("OK!!!  " + responseg.toString());
        log.debug("responseg.message() --> " + responseg.message());
        log.debug("responseg.body() --> " + responseg.body());
        log.debug("responseg.responseg.headers().toString() --> " + responseg.headers().toString());
        // System.out.println("hasPermission??? ---> " + responseg.header("hasPermssion"));

        JsonObject jo = new JsonObject();
        jo.addProperty("hasPermission", responseg.header("hasPermssion").toString());

        return new ResponseEntity(jo.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "getCurrentStato", method = RequestMethod.GET)
    public ResponseEntity<Fase> getCurrentStato(@RequestParam("idIter") Integer idIter) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        Iter iter = GetEntityById.getIter(idIter, em);

        String CurrentStato = iter.getIdStato().getCodice();

        return new ResponseEntity(CurrentStato, HttpStatus.OK);
    }

    @RequestMapping(value = "riattivaIterSenzaDocumento", method = RequestMethod.POST)
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public ResponseEntity RiattivaIterSenzaDocumento(@RequestBody GestioneStatiParams params) throws IOException {
        // Mi prendo l'occorrente
        Utente u = utilityFunctions.getUtenteLoggatto();
        Iter i = iterRepository.findById(params.getIdIter()).get();
        FaseIter fi = getFaseIter(i);
        Evento e = this.entitiesCachableUtilities.loadEventoByCodice("chiusura_sospensione");
        Stato s = GetEntityById.getStatoByCodice(Stato.CodiciStato.IN_CORSO.toString(), em);

        // Mi assicuro che l'utente abbia il permesso sull'iter
        ResponseEntity re = hasPermissionOnFascicolo(i.getIdFascicolo());
        JsonObject obj = new JsonParser().parse(re.getBody().toString()).getAsJsonObject();
        if (!obj.get("hasPermission").getAsBoolean()) {
            throw new ForbiddenResponseException(0, "Attenzione, non sei abilitato all'utilizzo di questa funzione.", "");
        }

        // Aggiorno l'iter
        i.setIdStato(s);
        em.persist(i);

        // Creo l'evento
        EventoIter ei = new EventoIter();
        ei.setNote(params.getNote());
        ei.setIdIter(i);
        ei.setAutore(u);
        ei.setIdEvento(e);
        ei.setDataOraEvento(new Date());
        ei.setIdFaseIter(fi);
        em.persist(ei);

        JsonObject o = new JsonObject();
        o.addProperty("tuttook", "ehsi");
        return new ResponseEntity(o.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "getFascicoloConPermessi", method = RequestMethod.POST)
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public ResponseEntity getFascicoloConPermessi(@RequestBody String numerazioneGerarchica) throws IOException {
        log.debug("Sono dentro la getFascicoloConPermessi");
        log.debug("Numerazione gerarchica: " + numerazioneGerarchica);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UtenteCachable userInfo = (UtenteCachable) authentication.getPrincipal();
        // String codiceFiscale = (String) userInfo.get(UtenteCachable.KEYS.CODICE_FISCALE);

        AziendaCachable aziendaInfo = (AziendaCachable) userInfo.get(UtenteCachable.KEYS.AZIENDA_LOGIN);
        int idAzienda = (int) aziendaInfo.get(AziendaCachable.KEYS.ID);
        String urlChiamata = GetBaseUrls.getBabelSuiteBdsToolsUrl(idAzienda, em, objectMapper) + getFascicoliConPermessi;
        // urlChiamata = "http://localhost:8084" + getFascicoliConPermessi;
        Researcher r = new Researcher(null, numerazioneGerarchica, 0);
        HashMap additionalData = (HashMap) new java.util.HashMap();
        additionalData.put(GetFascicoli.SOLO_ITER.toString(), "true");
        additionalData.put(GetFascicoli.ANCHE_CHIUSI.toString(), "true");

        IodaRequestDescriptor irdg = new IodaRequestDescriptor("gipi", "gipi", r, additionalData);
        okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, irdg.getJSONString().getBytes("UTF-8"));

        Request requestg = new Request.Builder()
                .url(urlChiamata)
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient();

        Response response = client.newCall(requestg).execute();

        if (!response.isSuccessful()) {
            throw new IOException("La chiamata a bds_tools non ?? andata a buon fine. " + response);
        }

        log.debug("OK!!!  " + response.toString());
        log.debug("responseg.message() --> " + response.message());
        log.debug("responseg.body() --> " + response.body());
        log.debug("responseg.responseg.headers().toString() --> " + response.headers().toString());

        Fascicoli fs = (Fascicoli) it.bologna.ausl.ioda.iodaobjectlibrary.Requestable.parse(response.body().string(), Fascicoli.class);

        // Mi aspetto che il fascicolo sia uno
        if (fs.getSize() != 1) {
            log.debug("Trovato o zero o pi?? di un fascicolo. Questo non deve accadere");
            // Qui vorrei lanciare la 412 Precondition Failed
        }

        Fascicolo f = fs.getFascicolo(0);

        return new ResponseEntity(f.getJSONString(), HttpStatus.OK);
    }

    @RequestMapping(value = "cambiaResponsabileProcedimento", method = RequestMethod.POST)
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public ResponseEntity cambiaResponsabileProcedimento(@RequestBody String params) throws IOException, GipiPubblicazioneException {
        log.info("PARAMS = " + params);
        JsonParser parser = new JsonParser();
        JsonObject dati = (JsonObject) parser.parse(params);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UtenteCachable userInfo = (UtenteCachable) authentication.getPrincipal();
        Utente utenteLoggato = GetEntityById.getUtente((int) userInfo.get(UtenteCachable.KEYS.ID), em);

        AziendaCachable aziendaInfo = (AziendaCachable) userInfo.get(UtenteCachable.KEYS.AZIENDA_LOGIN);
        int idAzienda = (int) aziendaInfo.get(AziendaCachable.KEYS.ID);

        String urlChiamata = GetBaseUrls.getBabelSuiteBdsToolsUrl(idAzienda, em, objectMapper) + updateFascicoloGediPath;
        //String urlChiamata = "http://localhost:8083/bds_tools/ioda/api/fascicolo/UpdateFascicolo";

        Fascicolo fascicolo;
        // Se ho passato anche i vicari, allora, vuol dire che l'utente non c'era tra di loro e vanno aggiornati
        if (dati.has("vicari")) {
            List<String> vicari = new Gson().fromJson(dati.get("vicari").getAsJsonArray(), new TypeToken<List<String>>() {
            }.getType());
            fascicolo = new Fascicolo(dati.get("idFascicolo").getAsString(), dati.get("cfResponsabile").getAsString(), vicari);
        } else {
            fascicolo = new Fascicolo(dati.get("idFascicolo").getAsString(), dati.get("cfResponsabile").getAsString());
        }

        IodaRequestDescriptor irdg = new IodaRequestDescriptor("gipi", "gipi", fascicolo);
        okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, irdg.getJSONString().getBytes("UTF-8"));

        Request requestg = new Request.Builder()
                .url(urlChiamata)
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Response responseg = client.newCall(requestg).execute();
        /* Se la chiamata a Gedi ?? andata a buon fine aggiorno la procedimenti cache e loggo l'evento  */
        if (responseg.isSuccessful()) {
            iterUtilities.aggiornaProcCacheEloggaEvento(dati.get("idIter").getAsInt(),
                    dati.get("idUtenteResponsabile").getAsInt(), dati.get("idStrutturaResponsabile").getAsInt(),
                    utenteLoggato, entitiesCachableUtilities);
        } else {
            throw new IOException("La chiamata a Babel non ?? andata a buon fine. " + responseg);
        }

        return new ResponseEntity(params, HttpStatus.OK);
    }

    @RequestMapping(value = "aggiornaVicariDelFascicolo", method = RequestMethod.POST)
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public ResponseEntity aggiornaVicariDelFascicolo(@RequestBody String params) throws IOException {
        log.info("aggiornaVicariDelFascicolo");
        log.info("PARAMS = " + params);
        JsonParser parser = new JsonParser();
        JsonObject dati = (JsonObject) parser.parse(params);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UtenteCachable userInfo = (UtenteCachable) authentication.getPrincipal();
        Utente utenteLoggato = GetEntityById.getUtente((int) userInfo.get(UtenteCachable.KEYS.ID), em);

        AziendaCachable aziendaInfo = (AziendaCachable) userInfo.get(UtenteCachable.KEYS.AZIENDA_LOGIN);
        int idAzienda = (int) aziendaInfo.get(AziendaCachable.KEYS.ID);

        String urlChiamata = GetBaseUrls.getBabelSuiteBdsToolsUrl(idAzienda, em, objectMapper) + updateFascicoloGediPath;
        //String urlChiamata = "http://localhost:8083/bds_tools/ioda/api/fascicolo/UpdateFascicolo";

        List<String> vicari = new Gson().fromJson(dati.get("vicari").getAsJsonArray(), new TypeToken<List<String>>() {
        }.getType());
        Fascicolo fascicolo = new Fascicolo(dati.get("numerazioneGerarchica").getAsString(), null, vicari);

        IodaRequestDescriptor irdg = new IodaRequestDescriptor("gipi", "gipi", fascicolo);
        okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, irdg.getJSONString().getBytes("UTF-8"));

        Request requestg = new Request.Builder()
                .url(urlChiamata)
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Response responseg = client.newCall(requestg).execute();

        if (!responseg.isSuccessful()) {
            throw new IOException("La chiamata a bds-tools non ?? andata a buon fine. " + responseg);
        }

        // Mi faccio dare il fascicolo aggiornato
        ResponseEntity re = getFascicoloConPermessi(dati.get("numerazioneGerarchica").getAsString());

        return re;
    }

    public String getWebApiPathByIdApplicazione(String application, WebApi webApi) {
        String path = "";
        switch (application) {

            case "procton":
                if (webApi == WebApi.GESTISCI_STATO_ITER) {
                    path = proctonGestisciIterPath;
                } else if (webApi == WebApi.CANCELLA_DOCUMENTO_ITER) {
                    path = proctonDeleteDocumentoIterPath;
                }
                break;

            case "dete":
                if (webApi == WebApi.GESTISCI_STATO_ITER) {
                    path = deteGestisciIterPath;
                } else if (webApi == WebApi.CANCELLA_DOCUMENTO_ITER) {
                    path = deteDeleteDocumentoIterPath;
                }
                break;

            case "deli":
                if (webApi == WebApi.GESTISCI_STATO_ITER) {
                    path = deliGestisciIterPath;
                } else if (webApi == WebApi.CANCELLA_DOCUMENTO_ITER) {
                    path = deliDeleteDocumentoIterPath;
                }
                break;
        }
        return path;
    }

    @RequestMapping(value = "rollbackEventoIterById", method = RequestMethod.POST)
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public ResponseEntity rollbackEventoIterById(@RequestBody int idEventoIter) throws IOException {
        log.info("ITER CONTROLLER");
        log.info("Chiamata alla funzione eliminaEventoIter -> idEventoIter = " + idEventoIter);

        // CARICARE LE ENTITA'
        log.info("Carico l'EventoIter");
        JPQLQuery<EventoIter> q = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
        EventoIter ei = (EventoIter) q
                .from(qEventoIter)
                .where(qEventoIter.id.eq(idEventoIter))
                .fetchOne();
        log.info("EventoIter trovato: " + ei.toString());
        log.info("Iter dell'EventoIter trovato: " + ei.getIdIter().toString());
        log.info("Evento dell'EventoIter trovato: " + ei.getIdEvento().toString());

        // ripristino stato e aggiorno i giorni sospensione dell'iter
        Iter i = rollbackEventoIter(ei);

        boolean eliminato = false;
        DocumentoIter di = null;

        // se ho un documento iter, lego tutto all'eliminazione del documento
        if (ei.getIdDocumentoIter() != null) {

            log.info("documento_iter dell'evento trovato: " + ei.getIdDocumentoIter().toString());

            //Carico il DocumentoIter da eliminare
            JPQLQuery<DocumentoIter> queryDocumentoIter = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);

            di = queryDocumentoIter
                    .from(qDocumentoIter)
                    .where(qDocumentoIter.id.eq(ei.getIdDocumentoIter().getId()))
                    .fetchOne();

            log.info("idIter: " + di.getIdIter().getId() + " oggetto: '" + di.getOggetto() + "', guid_oggetto: '" + di.getIdOggetto() + "' dati_aggiuntivi: '" + di.getDatiAggiuntivi() + "'");

            // ora mi recupero di dati che mi servono per la chiamata alla WebApi: idDocumentoIter, applicazione
            int idIterDelDocumentoIterDaEliminare = di.getIdIter().getId();
            String idOggettoOrigine = di.getIdOggetto();
            // Ho codice registro PG ? procton :altrimenti ho codice registro DETE ? dete :altrimenti  deli.
            String applicazione = di.getRegistro().equals(CodiceRegistro.PG.toString()) ? Applicazione.PROCTON.toString() : di.getRegistro().equals(CodiceRegistro.DETE.toString()) ? Applicazione.DETE.toString() : Applicazione.DELI.toString();

            eliminato = this.eliminaDocumentoIterSuPDD(idIterDelDocumentoIterDaEliminare, idOggettoOrigine, applicazione, i.getIdProcedimento().getIdAziendaTipoProcedimento().getIdAzienda());

            // se non sono riuscito a eliminare allora ?? inutile che continuo la cancellazione, anzi, meglio che lasciamo la roba cos?? com'?? di modo che poi sistemiamo
            if (!eliminato) {
                log.error("C'?? stato un'errore nella cancellazione del documento iter da argo: controllare il log.");
                throw new IOException("La chiamata a PDD per eliminazione del documento iter non ?? andata a buon fine.");
            }
            log.info("Ho eliminato il documento iter da gipi");
            log.info("l'associazione documento_iter e la fascicolazione sono state cancellate da argo.");

        } else {
            log.info("non c'?? un documento associato all'evento_iter");
        }

        log.info("Procedo al salvataggio dei dati.");
        log.info("Eliminazione EventoIter --> id: " + ei.getId() + " " + ei.getIdEvento().getCodice() + " " + ei.getDataOraEvento());
        em.remove(ei);
        log.info("EventoIter eliminato");
        if (eliminato == true && di != null) {
            log.info("Eliminazione DocumentoIter --> id: " + di.toString() + " idOggetto: " + di.getIdOggetto() + " idIter: " + di.getIdIter());
            em.remove(di);
            log.info("DocumentoIter eliminato");
        }
        log.info("Salvataggio aggiornamenti iter");

        i.setGiorniSospensioneTrascorsi(aggiornaCampiIter.calcolaGiorniSospensioneTrascorsi(i));
        log.info("setto giorni di sospensione prima di aggiornare iter -> id_iter: " + i.getId() + "; numero_giorni_sospensione: " + i.getGiorniSospensioneTrascorsi());
        em.merge(i);
        log.info("Iter aggiornato");

        // altri casi non sono stati previsti
        JsonObject o = new JsonObject();
        o.addProperty("tuttook", "ehsi");
        return new ResponseEntity(o.toString(), HttpStatus.OK);
    }

    public boolean eliminaDocumentoIterSuPDD(int idIter, String idOggettoOrigine, String applicazione, Azienda azienda) throws UnsupportedEncodingException, IOException {
        log.info("Mi piglio il cf dell'utente loggato...");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UtenteCachable userInfo = (UtenteCachable) authentication.getPrincipal();
        String codiceFiscaleUtenteLoggato = (String) userInfo.get(UtenteCachable.KEYS.CODICE_FISCALE);
        log.info("... " + codiceFiscaleUtenteLoggato);

        // String urlChiamata = "http://localhost:8080" + getWebApiPathByIdApplicazione(applicazione, WebApi.CANCELLA_DOCUMENTO_ITER);
        String urlChiamata = GetBaseUrls.getBabelSuiteWebApiUrl(azienda.getId(), em, objectMapper) + getWebApiPathByIdApplicazione(applicazione, WebApi.CANCELLA_DOCUMENTO_ITER);

        log.info("Ora chiamo la Web Api -> " + urlChiamata);

        JsonObject o = new JsonObject();
        o.addProperty("idIter", idIter);
        o.addProperty("idOggettoOrigine", idOggettoOrigine);
        o.addProperty("cfUtente", codiceFiscaleUtenteLoggato);

        log.info("Dati passati: " + o.toString());
        okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, o.toString().getBytes("UTF-8"));

        Request requestg = new Request.Builder()
                .url(urlChiamata)
                .addHeader("X-HTTP-Method-Override", "deleteDocumentiIter")
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        Response responseg = client.newCall(requestg).execute();

        log.info("response " + responseg.toString());
        if (responseg.isSuccessful()) {
            List<String> cfUtentiDaRefreshare = new ArrayList<>();
            cfUtentiDaRefreshare.add(codiceFiscaleUtenteLoggato);
            PrimusCommandParams command = new RefreshBoxDatiDiArchivioCommandParams();
            log.info("aggiorno l'interfaccia con primus..." + "azienda " + azienda.toString() + "cfUtentiDaRefreshare " + cfUtentiDaRefreshare.toString()
                    + "command " + command + "applicazione " + applicazione);
            utilityFunctions.sendPrimusCommand(azienda, cfUtentiDaRefreshare, command, applicazione);
        }

        return responseg.isSuccessful();
    }

    /**
     * Prende come parametro un EventoIter, e ne restituisce l'iter coinvolto,
     * ripulito di tutte le affezioni: ripristina lo stato, aggiorna i giorni di
     * sospensione come se l'evento passato come parametro non ci fosse mai
     * stato. NB: la funzione non cancella l'evento iter: questo deve essere
     * cancellato fuori!
     */
    public Iter rollbackEventoIter(EventoIter ei) {
        log.info("Entrato in rollbackEventoIter");
        log.info("Codice dell'evento " + ei.getIdEvento().getCodice());
        log.info("Carico l'iter...");
        JPQLQuery<Iter> queryIter = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
        Iter i = queryIter
                .from(qIter)
                .where(qIter.id.eq((ei.getIdIter().getId())))
                .fetchOne();
        log.info("iter caricato");

        /* Cosa deve succere ora?
           - devono essere ciclati gli eventi perch?? vanno ricalcolati i giorni di sospensione DAL PRIMO EVENTO!
           - si rimette l'iter nello stato precedente
           - si restituisce l'iter
         */
        // calcolo giorni di sospensione usando il metodo usato anche dal servizio schedulato
        // PARTE DI SAL questo serve solo per ricalcolare i giorni di sospensione: se trovo incongruenze, esco
//        for (EventoIter ev : eventi) {
//            log.info(ev.getDataOraEvento() + " -> " + ev.getIdEvento().getCodice());
//            // se becco l'evento allora finisco
//            if (ev.getId().equals(ei.getId())) {
//                if (!inCorso) { // se sto ad esempio eliminando un'associazione al documento di un iter sospeso, l'iter deve restare sospeso
//                    long diff = TimeUnit.DAYS.convert(Math.abs(new Date().getTime() - dataInizio.getTime()), TimeUnit.MILLISECONDS);
//                    giorniSospensione += diff;
//                }
//                log.info("l'iter risulta aver " + giorniSospensione + " giorni di sospensione");
//                i.setGiorniSospensioneTrascorsi(giorniSospensione);
//                break;
//            }
//
//            if (ev.getIdEvento().getCodice().equals(CodiceEvento.APERTURA_SOSPENSIONE.toString())) {
//                if (inCorso) {
//                    log.info("aggiorno i giorni di sospensione per " + ev.getIdEvento().getCodice().toString());
//                    dataInizio = ev.getDataOraEvento();
//                    inCorso = false;
//                } else {
//                    log.info("ho riscontrato un'incongruenza negli eventi: l'iter sembra sospeso ma dovrebbe venire rispospeso dall'evento!");
//                    break;
//                }
//            } else if (ev.getIdEvento().getCodice().equals(CodiceEvento.CHIUSURA_SOSPENSIONE.toString()) || ev.getIdEvento().getCodice().equals(CodiceEvento.ITER_IN_CORSO.toString())) {
//                if (!inCorso && dataInizio != null) {
//                    log.info("aggiorno i giorni di sospensione per " + ev.getIdEvento().getCodice().toString());
//                    dataFine = ev.getDataOraEvento();
//                    long diff = TimeUnit.DAYS.convert(Math.abs(dataFine.getTime() - dataInizio.getTime()), TimeUnit.MILLISECONDS);
//                    giorniSospensione += diff;
//                    inCorso = true;
//                } else {
//                    log.info("ho riscontrato un'incongruenza negli eventi: l'iter sembra in corso ma dovrebbe venire de-sospeso dall'evento!");
//                    break;
//                }
//            }
//            // nel caso di un'aggiunta documento non ho bisogno di modificare i giorni di sospensione.
//        }
        log.info("Ripristino lo stato dell'iter alla condizione precedente");
        // se l'evento che devo cancellare ?? di chiusura sospensione, allora rimetto l'iter in corso
        if (ei.getIdEvento().getCodice().equals(CodiceEvento.APERTURA_SOSPENSIONE.toString())) {
            i.setIdStato(GetEntityById.getStatoByCodice(Stato.CodiciStato.IN_CORSO.toString(), em));
        } else if (ei.getIdEvento().getCodice().equals(CodiceEvento.CHIUSURA_SOSPENSIONE.toString()) || ei.getIdEvento().getCodice().equals(CodiceEvento.ITER_IN_CORSO.toString())) // per ogni altro tipo di evento possibile lo rimetto in sospeso
        {
            i.setIdStato(GetEntityById.getStatoByCodice(Stato.CodiciStato.SOSPESO.toString(), em));
        }

//        i.setGiorniSospensioneTrascorsi(aggiornaCampiIter.calcolaGiorniSospensioneTrascorsi(i));
        log.info("Lo stato dell'iter ?? -> " + i.getIdStato().toString());
        return i;
    }

    @RequestMapping(value = "setPrecedente", method = RequestMethod.POST)
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public ResponseEntity setPrecedente(@RequestBody String params, HttpServletRequest request) throws IOException {
        log.info("********   CHIAMATA WEB      ********");
        ResponseEntity settaPrecedente = settaPrecedente(params, request);
        return settaPrecedente;
    }

    public ResponseEntity settaPrecedente(String params, HttpServletRequest request) throws IOException {
        HashMap additionalData = (HashMap) new java.util.HashMap();
        additionalData.put(OperazioniFascicolo.PROVENIENZA_GIPI.toString(), true);
        log.info("********  ENTRATO IN SET PRECEDENTE   ********");
        log.info("params ---> " + params);
        log.info("***********************");

        String noteDellEvento = ""; // queste note verranno mostrate in interfaccia

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UtenteCachable userInfo = (UtenteCachable) authentication.getPrincipal();
        Utente u = GetEntityById.getUtente((int) userInfo.get(UtenteCachable.KEYS.ID), em);
        JsonObject o = new JsonObject();
        JsonParser parser = new JsonParser();
        JsonObject dati = (JsonObject) parser.parse(params);
        Evento e = new Evento();
        EventoIter ei = new EventoIter();
        Iter iter = iterUtilities.getIterById(dati.get("idIter").getAsInt());

        Fascicolo fascicolo = new Fascicolo();
        fascicolo.setIdIter(iter.getId());

        String codiceDellEvento = dati.get("azione").getAsString().equals(AzioneSuiPrecedenti.ADD.toString()) ? "aggiunta_precedente" : "cancellazione_precedente";
        e = entitiesCachableUtilities.loadEventoByCodice(codiceDellEvento);
        log.info("Evento --> " + e.getNome());

        log.info("..iter caricato");

        log.info("Evento --> " + e.getNome());

        log.info("..iter caricato");
        // cosa faccio? aggiungo o cancello?
        if (dati.get("azione").getAsString().equals(AzioneSuiPrecedenti.ADD.toString())) {
            log.info("Sto aggiungendo un precedente");

            // prendo i dati e li setto
            log.info("carico e setto l'iter precedente");
            Iter iterPrecedente = iterUtilities.getIterById(dati.get("idIterPrecedente").getAsInt());

            log.info("carico e setto il motivo precedente");
            MotivoPrecedente mp = iterUtilities.getMotivoPrecedenteByCodice(dati.get("codiceMotivo").getAsString());
            log.info("MOTIVO --> " + mp.getDescrizione());
            iter.setIdMotivoPrecedente(mp);

            log.info("setto le note motivo precedente");
            log.info(dati.get("noteMotivoPrecedente").toString());
            iter.setNoteMotivoPrecedente(dati.get("noteMotivoPrecedente") != null ? dati.get("noteMotivoPrecedente").toString() : null);

            boolean risultatoDellUpdateCatena = iterRepository.setIdCatenaAndPrecedenza(iter.getId(), iterPrecedente.getIdCatena(), iterPrecedente.getId());
            log.info("risultato: ", risultatoDellUpdateCatena);

            additionalData.put(OperazioniFascicolo.ID_ITER_PRECEDENTE.toString(), iterPrecedente.getId());
            noteDellEvento = "Collegamento all'iter " + iterPrecedente.getNumero() + "/" + iterPrecedente.getAnno();
        } else { // sto cancellando l'associazione al precedente
            log.info("Sto camncellando il precedente all'iter");
            log.info("Prima per?? lo carico per recuperarmi le sue informazioni");
            Iter iterPrecedente = iterUtilities.getIterById(iter.getIdIterPrecedente().getId());
            log.info("Iter Precedente: id ", iterPrecedente.getId(), "numero", iterPrecedente.getNumero(), "anno", iterPrecedente.getAnno());
            iter.setIdMotivoPrecedente(null);
            iter.setNoteMotivoPrecedente(null);

            boolean risultatoDellUpdateCatena = iterRepository.setIdCatenaAndPrecedenza(iter.getId(), null, null);
            log.info("risultato: ", risultatoDellUpdateCatena);

            additionalData.put(OperazioniFascicolo.DELETE_ITER_PRECEDENTE.toString(), true);
            noteDellEvento = "Cancellazione collegamento ad Iter " + iterPrecedente.getNumero() + "/" + iterPrecedente.getAnno();
        }

        // setto l'evento iter
        log.info("setto l'evento iter");
        ei.setAutore(u);
        ei.setDataOraEvento(new Date());
        ei.setIdIter(iter);
        ei.setIdEvento(e);
        ei.setDettagli(noteDellEvento);
        ei.setIdFaseIter(getFaseIter(iter));

        log.info("salvo l'evento iter");
        IodaRequestDescriptor ird = new IodaRequestDescriptor("gipi", "gipi", fascicolo, additionalData);
        String baseUrl;
        if (request.getServerName().equalsIgnoreCase("localhost")) {
            baseUrl = localhostBaseUrl;
        } else {
            baseUrl = GetBaseUrls.getBabelSuiteBdsToolsUrl(iter.getIdProcedimento().getIdAziendaTipoProcedimento().getIdAzienda().getId(), em, objectMapper);
        }

        String urlChiamata = baseUrl + updateFascicoloPath;
        log.info("Url chiamata chiamata = " + urlChiamata);
        OkHttpClient client = new OkHttpClient();
        okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, ird.getJSONString().getBytes("UTF-8"));
        log.info("Preparo la request");
        Request req = new Request.Builder()
                .url(urlChiamata)
                .post(body)
                .build();
        log.info("faccio la chimata...");
        Response response = client.newCall(req).execute();
        String resString = null;
        log.info("response --> " + response.toString());
        if (response != null && response.body() != null) {
            resString = response.body().string();
        }

        if (response.isSuccessful()) {
            em.persist(ei);
            log.info("FATTO");

            log.info("Salvo l'iter (persist)");

            em.persist(iter);
            o.addProperty("risultato", "tutto ok");
            return new ResponseEntity(o.toString(), HttpStatus.OK);
        } else {
            o.addProperty("risultato", "errore nell'associazione cin con il padre della catena fascicolare");
            return new ResponseEntity(o.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "annullaIter", method = RequestMethod.POST)
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public ResponseEntity annullaIter(@RequestBody String params, HttpServletRequest request) throws IOException, org.json.simple.parser.ParseException {
        // ho l'idIter: carico l'iter;
        log.info("****   SONO ENTRATO IN 'ANNULLAITER()'  ****");
        log.info("PARAMS = " + params);
        JsonParser parser = new JsonParser();
        JsonObject dati = (JsonObject) parser.parse(params);
        int idIter = dati.get("idIter").getAsInt();
        String noteEventoAnnullamento = dati.get("note").getAsString();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UtenteCachable userInfo = (UtenteCachable) authentication.getPrincipal();
        Utente u = GetEntityById.getUtente((int) userInfo.get(UtenteCachable.KEYS.ID), em);
        Iter iter = iterUtilities.getIterById(idIter);
        Iter precedente = iter.getIdIterPrecedente() != null ? iterUtilities.getIterById(iter.getIdIterPrecedente().getId()) : null;
        log.info("Annullamento dell'iter numero", iter.getNumero() + "/" + iter.getAnno());
        log.info("Setto flag annullato e data annullamento");
        iter.setAnnullato(true);
        iter.setDataAnnullamento(new Date());
        log.info("Setto stato iter 'CHIUSO'");
        iter.setIdStato(GetEntityById.getStatoByCodice(Stato.CodiciStato.CHIUSO.toString(), em));
        SpettanzaAnnullamento spettanza = new SpettanzaAnnullamento();
        spettanza.setIdIter(iter);
        spettanza.setIdUtenteAnnullante(u);
        log.info("Ora una parte delicata: mi carico la struttura di afferenza diretta dell'utente loggato");
        JPQLQuery<Struttura> queryStrutturaUtente = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);
        Struttura strutturaAfferenzaDirettaUtenteAnnullante = (Struttura) queryStrutturaUtente
                .from(qStruttura)
                .where(qStruttura.id.eq(userInfo.getIdStruttureAfferenzaDiretta().get(0)))
                .fetchOne();
        log.debug("trovata questa: ", strutturaAfferenzaDirettaUtenteAnnullante.getNome());
        spettanza.setIdStrutturaUtenteAnnullante(strutturaAfferenzaDirettaUtenteAnnullante);
        spettanza.setDataAnnullamento(new Date());
        em.persist(spettanza);

        // evento iter con dettagli sui documenti cancellati
        EventoIter ei = new EventoIter();
        ei.setIdEvento(entitiesCachableUtilities.loadEventoByCodice(CodiceEvento.ANNULLAMENTO_ITER.toString()));
        ei.setIdIter(iter);
        ei.setIdFaseIter(getFaseIter(iter));
        ei.setAutore(u);
        ei.setDataOraEvento(spettanza.getDataAnnullamento());
        ei.setNote(noteEventoAnnullamento);

        // COMPOSIZIONE DEL MESSAGGIO DELL'EVENTO (prima devo fare questo perch?? la web api chiama un mestiere che elimina tutto! Documenti e fascicolazioni)
        String dettagliEvento = "L'utente " + userInfo.get(UtenteCachable.KEYS.COGNOME).toString() + " " + userInfo.get(UtenteCachable.KEYS.NOME).toString()
                + " ha annullato l'iter " + iter.getNumero() + "/" + iter.getAnno() + " '" + iter.getOggetto() + "'.\n";
        dettagliEvento += "Il fascicolo " + iter.getIdFascicolo() + " '" + iter.getNomeFascicolo() + "' ?? stato declassifficato a tipo \"Affare\".\n";

//        JPQLQuery<DocumentoIter> queryDocumentiIterList = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);
//        List<DocumentoIter> documenti = (List) queryDocumentiIterList
//                .from(qDocumentoIter)
//                .where(qDocumentoIter.idIter.id.eq(iter.getId()));
        List<DocumentoIter> docIterList = new ArrayList<DocumentoIter>(iter.getDocumentiIterList());

//      QUESTA ROBA NON SERVE: NON SONO CANCELLATI DAL FASCICOLO        
        dettagliEvento += "I documenti: \n";
        for (DocumentoIter doc : iter.getDocumentiIterList()) {
            dettagliEvento += " * " + doc.getRegistro() + doc.getNumeroRegistro() + "/" + doc.getAnno() + "\n";
        }
        dettagliEvento += "sono stati disassociati dall'iter.";
        log.debug(" --->  dettagliEvento", dettagliEvento);

        // chiamare web api PDD
        /**
         * ***********************
         */
        /**
         * **** PARTE PDD *****
         */
        /**
         * ***********************
         */
        log.info("ora chiamiamo la web api su BABEL");
        log.info("setto i parametri di chiamata");
        JsonObject o = new JsonObject();
        o.addProperty("cf", (String) userInfo.get(UtenteCachable.KEYS.CODICE_FISCALE));
        o.addProperty("idIter", idIter);
        o.addProperty("codiceFascicolo", iter.getIdFascicolo());  // la numerazione_gerarchica
        log.info("Dati da passare: " + o.toString());

        String urlChiamata = GetBaseUrls.getBabelSuiteWebApiUrl(iter.getIdProcedimento().getIdAziendaTipoProcedimento().getIdAzienda().getId(), em, objectMapper)
                + babelAnnullaIterPath;
        log.info("Ora chiamo la Web Api -> " + urlChiamata);
        okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, o.toString().getBytes("UTF-8"));

        log.info("Preparo la requestg");
        Request requestg = new Request.Builder()
                .url(urlChiamata)
                .addHeader("X-HTTP-Method-Override", "annullaAndGloggaDocIter")
                .post(body)
                .build();
        log.info("requestg", requestg.toString());
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        Response responseg = client.newCall(requestg).execute();
        log.info("***responseg da babel...", responseg.toString());
        if (responseg != null && responseg.body() != null) {
            log.info("response.body().string()", responseg.body().toString());
            if (!responseg.isSuccessful()) {
                log.error("Risposta non Successful: qualcosa ?? andato male ?? faccio il rollback", responseg.body().toString());
                log.info("Ho provato a gloggare su questi documenti: ");
                for (DocumentoIter docX : docIterList) {
                    log.info(docX.getNumeroRegistro(), docX.getAnno(), docX.getIdOggetto(), docX.getDatiAggiuntivi());
                }
                throw new IOException("La chiamata alla webApi Babel/AnnullaIter non ?? andata a buon fine. \n "
                        + "Controllare i log sul tomcat-mestieri per i dettagli dei documenti. " + responseg);
            }
        }
        /*  ***FINE PARTE PDD*** */
        /**
         * **********************
         */

        log.info("Procedo con la detipizzazione del fascicolo ", iter.getIdFascicolo(), iter.getNomeFascicolo(), " e suo sganciamento dall'iter");

        /**
         * *********************
         */
        /**
         * **** PARTE IODA *****
         */
        /**
         * *********************
         */
        log.info("*** PREPARO LA CHIAMATA A IODA...");
        Fascicolo fascicolo = new Fascicolo();
        fascicolo.setIdIter(iter.getId());
        HashMap additionalData = (HashMap) new java.util.HashMap();
        additionalData.put(OperazioniFascicolo.PROVENIENZA_GIPI.toString(), true);
        additionalData.put(OperazioniFascicolo.UPDATE_PER_ANNULLAMENTO_ITER.toString(), true);
        log.info("Data to ioda: ", additionalData.toString());
        IodaRequestDescriptor ird = new IodaRequestDescriptor("gipi", "gipi", fascicolo, additionalData);
        log.info("IodaObjectDescriptor -> ", ird.getJSONString().getBytes("UTF-8"));
        String baseUrl;
        if (request.getServerName().equalsIgnoreCase("localhost")) {
            baseUrl = localhostBaseUrl;
        } else {
            baseUrl = GetBaseUrls.getBabelSuiteBdsToolsUrl(iter.getIdProcedimento().getIdAziendaTipoProcedimento().getIdAzienda().getId(), em, objectMapper);
        }
        log.debug(baseUrl);
        String urlChiamataUdateFascicolo = baseUrl + updateFascicoloPath;
        //String urlChiamata =  " http://localhost:8080/" + updateFascicoloPath;
        log.info("Url chiamata chiamata = " + urlChiamataUdateFascicolo);
        OkHttpClient clientPerIoda = new OkHttpClient();
        okhttp3.RequestBody bodyIoda = okhttp3.RequestBody.create(JSON, ird.getJSONString().getBytes("UTF-8"));
        log.info("Preparo la request");
        Request req = new Request.Builder()
                .url(urlChiamataUdateFascicolo)
                .post(bodyIoda)
                .build();
        log.info("faccio la chimata...");
        Response response = clientPerIoda.newCall(req).execute();
        String resString = null;
        log.info("response --> " + response.toString());
        if (response != null && response.body() != null) {
            resString = response.body().string();
            if (!response.isSuccessful()) {
                log.error("Chiamata a ioda fallita, lancio errore");
                log.info("Ho gi?? cancellato per?? questi documenti dall'iter: ");
                for (DocumentoIter docX : docIterList) {
                    log.info(docX.getNumeroRegistro(), docX.getAnno(), docX.getIdOggetto(), docX.getDatiAggiuntivi());
                }
                throw new IOException("La chiamata a ioda non ?? andata a buon fine. \n "
                        + "Controllare i log sul tomcat-mestieri per i dettagli dei documenti cancellati. " + response);

            }
        }
        /*
            ***  FINE PARTE IODA ****
         */

        // CANCELLAZIONE DEI PRECEDENTI (IRREVERSIBILE)
        // cancellare precedenti con la funzione postgres (non chiamare tutti gli eventi della funzione)
        log.info("Procedo alla cancellazione dei precedenti e/o lo sganciamento dei figli");
        boolean risultatoDellUpdateCatena = iterRepository.setIdCatenaAndPrecedenza(iter.getId(), null, null);
        log.info("risultato: ", risultatoDellUpdateCatena);
        if (precedente != null) {
            dettagliEvento += "\nL'iter non ?? pi?? associato con il suo precedente " + precedente.getNumero() + "/" + precedente.getAnno() + ".";
        }
        log.info("Setto a null motivo precedente e note motivo precedente");
        iter.setIdMotivoPrecedente(null);
        iter.setNoteMotivoPrecedente(null);
        ei.setDettagli(dettagliEvento);
        em.persist(ei);
        iter.setIdSpettanzaAnnullamento(spettanza);
        em.persist(iter);

        //Ok, ora chiamo webapi per notificare l'evento di annullamento: ciclo su tutti quelli che hanno permesso sul fascicolo.
        log.info("Ora invio le notifiche a Babel");
        JSONObject datiDaInviare = new JSONObject();
        ArrayList<String> utenti = new ArrayList<String>();
        utenti.add((String) userInfo.get(UtenteCachable.KEYS.CODICE_FISCALE));
        log.info("getIdResponsabileProcedimento ", iter.getIdResponsabileProcedimento().getIdPersona().getCodiceFiscale());
        if (!utenti.contains(iter.getIdResponsabileProcedimento().getIdPersona().getCodiceFiscale())) {
            utenti.add(iter.getIdResponsabileProcedimento().getIdPersona().getCodiceFiscale());
        }
        log.info("getIdResponsabileAdozioneAttoFinale ", iter.getIdProcedimento().getIdResponsabileAdozioneAttoFinale().getIdPersona().getCodiceFiscale());
        if (!utenti.contains(iter.getIdProcedimento().getIdResponsabileAdozioneAttoFinale().getIdPersona().getCodiceFiscale())) {
            utenti.add(iter.getIdProcedimento().getIdResponsabileAdozioneAttoFinale().getIdPersona().getCodiceFiscale());
        }
        log.info("getIdTitolarePotereSostitutivo ", iter.getIdProcedimento().getIdTitolarePotereSostitutivo().getIdPersona().getCodiceFiscale());
        if (!utenti.contains(iter.getIdProcedimento().getIdTitolarePotereSostitutivo().getIdPersona().getCodiceFiscale())) {
            utenti.add(iter.getIdProcedimento().getIdTitolarePotereSostitutivo().getIdPersona().getCodiceFiscale());
        }
        datiDaInviare.put("cfUtenti", utenti);
        datiDaInviare.put("messaggio", String.format("Notifica Annullamento Iter %s: %s", iter.getNumero() + "/" + iter.getAnno().toString(), iter.getIdProcedimento().getIdAziendaTipoProcedimento().getIdTipoProcedimento().getNome()));
        datiDaInviare.put("idIter", iter.getId());
        datiDaInviare.put("descrizioneNotifica", "Annullamento Iter " + iter.getNumero() + "/" + iter.getAnno().toString());
        log.info("dati da inviare ", datiDaInviare.toString());

        log.info("aggiungo i dati al json array");
        JSONArray ja = new JSONArray();
        ja.add(datiDaInviare);
        log.info("aggiungo i dati al json array");
        try {
            mandaNotificheSuBabel(iter, ja);
        } catch (Error e) {
            log.error("AHIA! ERRORE NELL'INVIO DELLA NOTIFICA... MA ORMAI TUTTO IL RESTO E' FATTO", e.toString());
        }

        JsonObject risultato = new JsonObject();
        risultato.addProperty("dettagliEvento", dettagliEvento);
        return new ResponseEntity(risultato.toString(), HttpStatus.OK);
    }

    public void mandaNotificheSuBabel(Iter iter, JSONArray ja) throws org.json.simple.parser.ParseException {
        String functionName = "IterController.mandaNotificheSuBabel";
        try {
            String urlChiamata = GetBaseUrls.getBabelSuiteWebApiUrl(iter.getIdProcedimento().getIdAziendaTipoProcedimento().getIdAzienda().getId(), em, objectMapper) + inviaNotificheWebApiPath;
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
                log.error(functionName + " ERRORE -> la response non ?? successful");
                throw new IOException("La chiamata a Babel non ?? andata a buon fine.");
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
