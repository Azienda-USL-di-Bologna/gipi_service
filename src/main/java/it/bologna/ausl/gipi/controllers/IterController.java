package it.bologna.ausl.gipi.controllers;

import it.bologna.ausl.gipi.utils.classes.GestioneStatiParams;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.gipi.Fase;
import it.bologna.ausl.entities.gipi.Iter;

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
import com.querydsl.jpa.JPAExpressions;
import it.bologna.ausl.entities.baborg.Azienda;
import it.bologna.ausl.entities.baborg.Utente;
import it.bologna.ausl.entities.cache.cachableobject.AziendaCachable;
import it.bologna.ausl.entities.cache.cachableobject.UtenteCachable;
import it.bologna.ausl.entities.gipi.DocumentoIter;
import it.bologna.ausl.entities.gipi.Evento;
import it.bologna.ausl.entities.gipi.EventoIter;
import it.bologna.ausl.entities.gipi.FaseIter;
import it.bologna.ausl.entities.gipi.QDocumentoIter;
import it.bologna.ausl.entities.gipi.QEvento;
import it.bologna.ausl.entities.gipi.QEventoIter;
import it.bologna.ausl.entities.gipi.QFaseIter;
import it.bologna.ausl.entities.gipi.QIter;
import it.bologna.ausl.entities.gipi.Stato;
import it.bologna.ausl.entities.gipi.utilities.EntitiesCachableUtilities;
import it.bologna.ausl.entities.repository.AziendaRepository;
import it.bologna.ausl.gipi.exceptions.GipiDatabaseException;
import it.bologna.ausl.gipi.exceptions.GipiRequestParamsException;
import it.bologna.ausl.gipi.process.CreaIter;
import static it.bologna.ausl.gipi.process.CreaIter.JSON;
import it.bologna.ausl.gipi.utils.GetBaseUrl;
import it.bologna.ausl.gipi.utils.GetEntityById;
import it.bologna.ausl.gipi.utils.GipiUtilityFunctions;
import it.bologna.ausl.ioda.iodaobjectlibrary.Document;
import it.bologna.ausl.ioda.iodaobjectlibrary.Fascicolazione;
import it.bologna.ausl.ioda.iodaobjectlibrary.GdDoc;
import it.bologna.ausl.ioda.iodaobjectlibrary.IodaRequestDescriptor;
import it.bologna.ausl.ioda.iodaobjectlibrary.Researcher;
import it.bologna.ausl.primuscommanderclient.PrimusCommandParams;
import it.bologna.ausl.primuscommanderclient.RefreshBoxDatiDiArchivioCommandParams;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.client.HttpResponseException;
import org.joda.time.DateTime;
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
public class IterController {

    @Autowired
    Process process;

    @Autowired
    CreaIter creaIter;

    @Autowired
    EntityManager em;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${getFascicoliUtente}")
    private String bdsGetFascicoliUtentePath;

    @Value("${babelGestisciIter}")
    private String babelGestisciIterPath;

    @Value("${updateGdDoc}")
    private String updateGdDocPath;

    @Value("${hasUserAnyPermissionOnFascicolo}")
    private String hasUserAnyPermissionOnFascicoloPath;

    public static enum GetFascicoliUtente {
        TIPO_FASCICOLO, SOLO_ITER, CODICE_FISCALE
    }

    @Autowired
    private EntitiesCachableUtilities entitiesCachableUtilities;
    
    @Autowired
    AziendaRepository aziendaRepository;
    
    @Autowired
    @Qualifier("GipiUtilityFunctions")
    GipiUtilityFunctions utilityFunctions;

    QIter qIter = QIter.iter;
    QDocumentoIter qDocumentoIter = QDocumentoIter.documentoIter;
    QEventoIter qEventoIter = QEventoIter.eventoIter;
    QEvento qEvento = QEvento.evento;

    @RequestMapping(value = "avviaNuovoIter", method = RequestMethod.POST)
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public ResponseEntity<Iter> AvviaNuovoIter(@RequestBody IterParams data, HttpServletRequest request)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException {

        Iter i = creaIter.creaIter(data, request.getServerName().equalsIgnoreCase("localhost"));

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

        System.out.println("ANTONELLA PARLA PIU' PIANO");
        System.out.println(data.getIdIter());
        System.out.println(data.getDataPassaggio());
        System.out.println(data.getCodiceRegistroDocumento());
        System.out.println(data.getNumeroDocumento());
        System.out.println(data.getAnnoDocumento());
        System.out.println(data.getEsito());
        System.out.println(data.getMotivazioneEsito());
        System.out.println(data.getNotePassaggio());
        System.out.println(data.getOggettoDocumento());

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

        // TODO: QUI BISOGNERA USARE L'OGGETTO PROCESS STATUS, ora non lo uso perchè devo restituire solo i nomi delle fasi perchè se no da errore
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
    public ResponseEntity gestisciStatoIter(@RequestBody GestioneStatiParams gestioneStatiParams) throws IOException {
        if (gestioneStatiParams.getNumeroDocumento().equals("")) {
            return gestisciStatoIterDaBozza(gestioneStatiParams);
        }

        Utente u = GetEntityById.getUtenteFromPersonaByCodiceFiscaleAndIdAzineda(gestioneStatiParams.getCfAutore(), gestioneStatiParams.getIdAzienda(), em);
        Iter i = GetEntityById.getIter(gestioneStatiParams.idIter, em);
        //Evento e = GetEntityById.getEventoByCodice(gestioneStatiParams.getStatoRichiesto().getCodice().toString(), em);
        Evento eventoDiCambioStato = new Evento();
        FaseIter fi = getFaseIter(i);

        Stato s = GetEntityById.getStatoByCodice(gestioneStatiParams.getStatoRichiesto(), em);
//        Stato s = GetEntityById.getStatoById(gestioneStatiParams.getStato(), em);
        if (s.getCodice().equals(i.getIdStato().getCodice())) // qui siamo se stiamo solo aggiungendo un documento
        {
            eventoDiCambioStato = this.entitiesCachableUtilities.loadEventoByCodice("aggiunta_documento");
        } else {
            // Questa non è una cosa bellissima e bisognerebbe fare un refactoring anche di questo
            // Infatti non abbiamo un modo automatico per determinare l'Evento in base allo Stato, nè abbiamo un enum sugli eventi
            if (s.getCodice().equals(Stato.CodiciStato.SOSPESO.toString())) {
                eventoDiCambioStato = this.entitiesCachableUtilities.loadEventoByCodice("apertura_sospensione");
            } else if (s.getCodice().equals(Stato.CodiciStato.CHIUSO.toString())) {
                eventoDiCambioStato = this.entitiesCachableUtilities.loadEventoByCodice("chiusura_iter");
                i.setDataChiusura(gestioneStatiParams.getDataEvento());
                i.setEsito(gestioneStatiParams.getEsito());
                i.setEsitoMotivazione(gestioneStatiParams.getEsitoMotivazione());
            } else {
                eventoDiCambioStato = this.entitiesCachableUtilities.loadEventoByCodice("chiusura_sospensione");
            }

            // Aggiorno l'iter
            i.setIdStato(s);
        }

        em.persist(i);
        
        Boolean isDifferito = gestioneStatiParams.getAzione().equals("cambio_di_stato_differito");
        
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
        ei.setDataOraEvento(gestioneStatiParams.getDataEvento());
        ei.setIdFaseIter(fi);
        em.persist(ei);

        // Fascicolo il documento
        String baseUrl = GetBaseUrl.getBaseUrl(i.getIdProcedimento().getIdAziendaTipoProcedimento().getIdAzienda().getId(), em, objectMapper);

        // baseUrl = "http://localhost:8084/bds_tools/ioda/api/document/update";
        String urlChiamata = urlChiamata = baseUrl + updateGdDocPath;
        GdDoc g = new GdDoc(null, null, null, null, null, null, null, (String) gestioneStatiParams.getCodiceRegistroDocumento(), null, (String) gestioneStatiParams.getNumeroDocumento(), null, null, null, null, null, null, null, (Integer) gestioneStatiParams.getAnnoDocumento());
        Fascicolazione fascicolazione = new Fascicolazione(i.getIdFascicolo(), null, null, null, DateTime.now(), Document.DocumentOperationType.INSERT);
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
        if (!responseg.isSuccessful()) {
            System.out.println("La risposta --> " + responseg.toString());
            throw new IOException("La fascicolazione non è andata a buon fine.");
        } 

        // Chiamo la web api solo se l'azione non è "cambio_di_stato_differito"
        // (perché il lavoro parte Babel lo esegue già il mestiere che chiama questa)
        if(!isDifferito){
            // Comunico a Babel l'associazione documento/iter appena avvenuta
            urlChiamata = GetBaseUrl.getBaseUrl(i.getIdProcedimento().getIdAziendaTipoProcedimento().getIdAzienda().getId(), em, objectMapper) + babelGestisciIterPath;
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
            // questo è solo  un segnaposto: il parametro è obbligatorio ma il documento non è più una bozza.
            JsonObject dati_differiti = new JsonObject();
            o.addProperty("datiDifferiti", dati_differiti.toString());

            body = okhttp3.RequestBody.create(JSON, o.toString().getBytes("UTF-8"));

            requestg = new Request.Builder()
                    .url(urlChiamata)
                    .addHeader("X-HTTP-Method-Override", "associaDocumento")
                    .post(body)
                    .build();

            client = new OkHttpClient();
            responseg = client.newCall(requestg).execute();

            if (!responseg.isSuccessful()) {
                throw new IOException("La chiamata a Babel non è andata a buon fine.");
            }
        }
        
        // Lancio comando a primus per aggiornamento istantaneo del box dati di archivio
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UtenteCachable userInfo = (UtenteCachable) authentication.getPrincipal();
        String codiceFiscaleUtenteLoggato = (String) userInfo.get(UtenteCachable.KEYS.CODICE_FISCALE);
        Azienda aziendaUtenteLoggato = aziendaRepository.findOne((Integer)((AziendaCachable) userInfo.get(UtenteCachable.KEYS.AZIENDA_LOGIN)).get(AziendaCachable.KEYS.ID));
        List<String> cfUtentiDaRefreshare = new ArrayList<>();
        cfUtentiDaRefreshare.add(codiceFiscaleUtenteLoggato);
        PrimusCommandParams command = new RefreshBoxDatiDiArchivioCommandParams();
        utilityFunctions.sendPrimusCommand(aziendaUtenteLoggato, cfUtentiDaRefreshare, command, gestioneStatiParams.getIdApplicazione());
        
        JsonObject obj = new JsonObject();
        obj.addProperty("idIter", i.getId().toString());

        return new ResponseEntity(obj.toString(), HttpStatus.OK);
    }

    public ResponseEntity gestisciStatoIterDaBozza(@RequestBody GestioneStatiParams gestioneStatiParams) throws IOException {
        // Tutto quello che mi serve lo si ricava da GestioneStatiParams o nell'iter (che si ricava da GestioneStatiParams)
//        GestioneStatoIter gsi = new GestioneStatoIter();
//        gsi.gestisciStatoIterDaBozza(gestioneStatiParams);

        // Recupero l'iter
        Iter i = GetEntityById.getIter(gestioneStatiParams.idIter, em);

        // Setto i dati_differiti (sono quelli da usare alla numerazione del documento)
        JsonObject dati_differiti = new JsonObject();
        dati_differiti.addProperty("cfAutore", gestioneStatiParams.getCfAutore());
        dati_differiti.addProperty("idAzienda", gestioneStatiParams.getIdAzienda());
        dati_differiti.addProperty("azione", gestioneStatiParams.getAzione());
        dati_differiti.addProperty("statoRichiesto", gestioneStatiParams.getStatoRichiesto());
        dati_differiti.addProperty("note", gestioneStatiParams.getNote());
        dati_differiti.addProperty("esito", gestioneStatiParams.getEsito());
        dati_differiti.addProperty("esitoMotivazione", gestioneStatiParams.getEsitoMotivazione());

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
        o.addProperty("datiDifferiti", dati_differiti.toString());

         // Creo il documento iter parziale
        DocumentoIter d = new DocumentoIter();
        d.setIdIter(i);
        d.setRegistro(gestioneStatiParams.getCodiceRegistroDocumento());
        d.setOggetto(gestioneStatiParams.getOggettoDocumento());
        d.setIdOggetto(gestioneStatiParams.getIdOggettoOrigine());
        d.setDescrizione(gestioneStatiParams.getDescrizione());
        d.setParziale(Boolean.TRUE);
        d.setDati_differiti(dati_differiti.toString());
        em.persist(d);
        em.flush();
        
        okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, o.toString().getBytes("UTF-8"));
        System.out.println(o.toString());

        // Chiamata alla web api GestisciIter.associaDocumento
        String urlChiamata = GetBaseUrl.getBaseUrl(gestioneStatiParams.getIdAzienda(), em, objectMapper) + babelGestisciIterPath;

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
        System.out.println("RISPOSTA: ");
        System.out.println(responseg.toString() + " body: " + responseBodyString);
        JsonObject obj = new JsonObject();
        if (!responseg.isSuccessful()) {
            throw new HttpResponseException(responseg.code(), "La chiamata a Babel non è andata a buon fine. " + responseg);
        } else if (responseBodyString.equals("0")) {
            obj.addProperty("idIter", "-1");
        } else {
            // ritorno un oggetto di ok
            obj.addProperty("idIter", i.getId().toString());
            obj.addProperty("object", responseg.toString());
            
            // Lancio comando a primus per aggiornamento istantaneo del box dati di archivio
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UtenteCachable userInfo = (UtenteCachable) authentication.getPrincipal();
            String codiceFiscaleUtenteLoggato = (String) userInfo.get(UtenteCachable.KEYS.CODICE_FISCALE);
            Azienda aziendaUtenteLoggato = aziendaRepository.findOne((Integer)((AziendaCachable) userInfo.get(UtenteCachable.KEYS.AZIENDA_LOGIN)).get(AziendaCachable.KEYS.ID));
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
        String urlChiamata = GetBaseUrl.getBaseUrl(idAzienda, em, objectMapper) + hasUserAnyPermissionOnFascicoloPath;

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
            throw new IOException("La chiamata a Babel non è andata a buon fine. " + responseg);
        }

        System.out.println("OK!!!  " + responseg.toString());
        System.out.println("responseg.message() --> " + responseg.message());
        System.out.println("responseg.body() --> " + responseg.body());
        System.out.println("responseg.responseg.headers().toString() --> " + responseg.headers().toString());
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
}
