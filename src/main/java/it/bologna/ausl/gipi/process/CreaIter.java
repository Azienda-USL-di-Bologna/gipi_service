package it.bologna.ausl.gipi.process;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.baborg.QAzienda;
import it.bologna.ausl.entities.baborg.Struttura;
import it.bologna.ausl.entities.baborg.Utente;
import it.bologna.ausl.entities.baborg.UtenteStruttura;
import it.bologna.ausl.entities.baborg.AfferenzaStruttura;
import it.bologna.ausl.entities.baborg.QStruttura;
import it.bologna.ausl.entities.baborg.QUtenteStruttura;
import it.bologna.ausl.entities.cache.cachableobject.UtenteCachable;
import it.bologna.ausl.entities.gipi.DocumentoIter;
import it.bologna.ausl.entities.gipi.Evento;
import it.bologna.ausl.entities.gipi.EventoIter;
import it.bologna.ausl.entities.gipi.Fase;
import it.bologna.ausl.entities.gipi.FaseIter;
import it.bologna.ausl.entities.gipi.Iter;
import it.bologna.ausl.entities.gipi.Procedimento;
import it.bologna.ausl.entities.gipi.ProcedimentoCache;
import it.bologna.ausl.entities.gipi.QEvento;
import it.bologna.ausl.entities.gipi.QFase;
import it.bologna.ausl.entities.gipi.QIter;
import it.bologna.ausl.entities.gipi.Stato;
import it.bologna.ausl.entities.gipi.utilities.EntitiesCachableUtilities;
import it.bologna.ausl.gipi.controllers.IterController;
import it.bologna.ausl.gipi.controllers.IterParams;
import it.bologna.ausl.gipi.odata.interceptor.IterInterceptor;
import it.bologna.ausl.gipi.utils.GetBaseUrls;
import it.bologna.ausl.gipi.utils.GetEntityById;
import it.bologna.ausl.gipi.utils.GipiUtilityFunctions;
import it.bologna.ausl.gipi.utils.IterUtilities;
import it.bologna.ausl.ioda.iodaobjectlibrary.Document;
import it.bologna.ausl.ioda.iodaobjectlibrary.Fascicolazione;
import it.bologna.ausl.ioda.iodaobjectlibrary.Fascicolo;
import it.bologna.ausl.ioda.iodaobjectlibrary.GdDoc;
import it.bologna.ausl.ioda.iodaobjectlibrary.IodaRequestDescriptor;
import it.bologna.ausl.primuscommanderclient.PrimusCommandParams;
import it.bologna.ausl.primuscommanderclient.RefreshBoxDatiDiArchivioCommandParams;
import it.nextsw.olingo.interceptor.OlingoInterceptorOperation;
import it.nextsw.olingo.interceptor.exception.OlingoRequestRollbackException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http2.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 *
 * @author f.gusella
 */
@Component
@PropertySource("classpath:application.properties")
public class CreaIter {

    QFase qFase = QFase.fase;
    QStruttura qStruttura = QStruttura.struttura;
    QUtenteStruttura qUtenteStruttura = QUtenteStruttura.utenteStruttura;
    QEvento qEvento = QEvento.evento;
    QIter qIter = QIter.iter;
    QAzienda qAzienda = QAzienda.azienda;

    private static final String EVENTO_CREAZIONE_ITER = "avvio_iter";

    @Autowired
    private EntitiesCachableUtilities entitiesCachableUtilities;

    @Autowired
    IterInterceptor interceptor;

    public static enum OperazioniFascicolo {
        TRADUCI_VICARI,
        DELETE_ITER_PRECEDENTE,
        ID_ITER_PRECEDENTE,
        UPDATE_PER_ANNULLAMENTO_ITER,
        PROVENIENZA_GIPI
    }

    @Value("${insertFascicolo}")
    private String insertFascicoloPath;

    @Value("${deleteFascicolo}")
    private String deleteFascicoloPath;

    @Value("${deleteDocumentoIterPerErroreCreazione}")
    private String deleteDocumentoIterPerErroreCreazione;

    @Value("${updateGdDoc}")
    private String updateGdDocPath;

    @Value("${babelGestisciIter}")
    private String babelGestisciIterPath;

    @Value("${proctonGestisciIter}")
    private String proctonGestisciIterPath;

    @Value("${deteGestisciIter}")
    private String deteGestisciIterPath;

    @Value("${deliGestisciIter}")
    private String deliGestisciIterPath;

    @Value("${babelsuite.uri.localhost}")
    private String localhostBaseUrl;

    @Autowired
    EntityManager em;

    @Autowired
    ObjectMapper objectMapper;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final Logger log = LoggerFactory.getLogger(CreaIter.class);

    @Autowired
    @Qualifier("GipiUtilityFunctions")
    GipiUtilityFunctions utilityFunctions;

    public Fase getFaseIniziale(int idAzienda) {
        JPQLQuery<Fase> query = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
        Fase f = query
                .from(this.qFase)
                .where(this.qFase.idAzienda.id.eq(idAzienda)
                        .and(this.qFase.ordinale.eq(1)))
                .fetchFirst();
        return f;
    }

    public Evento getEventoCreazioneIter() {
        log.info("Dentro getEventoCreazioneIter()...");
        JPQLQuery<Evento> query = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
        Evento e = query
                .from(this.qEvento)
                .where(this.qEvento.codice.eq(EVENTO_CREAZIONE_ITER))
                .fetchFirst();
        log.info("Ritorno evento creazione dell'iter... " + e.toString());
        return e;
    }

    public int getNumeroIterMax() {
        JPQLQuery<Iter> query = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
        int i = query.select(this.qIter.numero.max())
                .from(this.qIter).fetchFirst();
        return i;
    }

    public Iter creaIter(IterParams iterParams, boolean isLocalHost) throws IOException, OlingoRequestRollbackException, Throwable {
        String idFascicoloCreato = null;
        String cfResponsabileProcedimento = null;
        Iter i = new Iter();
        Procedimento p = null;
        Fascicolo fascicolo = null;
        Integer idIterTemp = null;
        try {
            // Mi prendo l'idUtente loggato
            log.debug("Carico authentication...");
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            log.debug("Carico userInfo...");
            UtenteCachable userInfo = (UtenteCachable) authentication.getPrincipal();
            log.debug("Carico idUtenteLoggato...");
            Integer idUtenteLoggato = (Integer) userInfo.get(UtenteCachable.KEYS.ID);
            log.info("IdUtenteLoggato " + idUtenteLoggato.toString());

            // Mi carico i dati di cui ho bisogno per creare l'iter.
            log.debug("Mi carico i dati di cui ho bisogno per creare l'iter:");
            log.debug("recupero il procedimento...");
            p = GetEntityById.getProcedimento(iterParams.getIdProcedimento(), em);

            log.info("Carico utente loggato...");
            Utente uLoggato = GetEntityById.getUtente(idUtenteLoggato, em);
            log.info("Utente caricato -> " + uLoggato.getUsername());

            log.info("Carico la struttura di afferenza diretta dell'utente loggato:");
            log.info("preparazione della query...");
            JPQLQuery<UtenteStruttura> query = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
            List<UtenteStruttura> utenteStrutturaDelCreatore = new ArrayList<>();
            log.info("costruzione ed esecuzione...");
            utenteStrutturaDelCreatore = query.select(this.qUtenteStruttura)
                    .from(this.qUtenteStruttura)
                    .where(
                            this.qUtenteStruttura.idUtente.id.eq(uLoggato.getId())
                                    .and(
                                            this.qUtenteStruttura.idAfferenzaStruttura.codice.eq(AfferenzaStruttura.CodiciAfferenzaStruttura.DIRETTA.toString())
                                                    .or(
                                                            this.qUtenteStruttura.idAfferenzaStruttura.codice.eq(AfferenzaStruttura.CodiciAfferenzaStruttura.UNIFICATA.toString())
                                                    )
                                    )
                    )
                    .fetch();
            log.info("Quante strutture utente ho trovato con afferenza diretta per l'utente loggato? " + utenteStrutturaDelCreatore.size());
            Struttura idStrutturaUtenteLoggato = utenteStrutturaDelCreatore.size() > 0 ? utenteStrutturaDelCreatore.get(0).getIdStruttura() : new Struttura();

            log.info("Carico utente responsabile...");
            Utente uResponsabile = GetEntityById.getUtente(iterParams.getIdUtenteResponsabile(), em);
            cfResponsabileProcedimento = uResponsabile.getIdPersona().getCodiceFiscale(); // questo ci servirà poi dopo SE dovremo rollbackare
            log.info("Carico utente_struttura del responsabile...");
            UtenteStruttura us = GetEntityById.getUtenteStruttura(iterParams.getIdUtenteStrutturaResponsabile(), em);
            log.info("Carico utente resp. adoz. ...");
            Utente uResponsabileAdozione = GetEntityById.getUtente(p.getIdResponsabileAdozioneAttoFinale().getId(), em);
            log.info("Carico utente titolare pot. esec. ...");
            Utente uTitolare = GetEntityById.getUtente(p.getIdTitolarePotereSostitutivo().getId(), em);
            log.info("Recupero la fase iniziale dell'iter in base al tipo di procedimento....");
            Fase f = this.getFaseIniziale(p.getIdAziendaTipoProcedimento().getIdAzienda().getId());
            log.info("Creo l'evento di creazione dell'iter...");
            Evento e = this.getEventoCreazioneIter();
            log.info("Setto il numero del documento...");
            // Sistemo il numero documento che ho in iterParams deve avere 7 cifre, se non le ha, aggiungo degli zeri da sinistra
            iterParams.setNumeroDocumento(String.format("%07d", Integer.parseInt(iterParams.getNumeroDocumento())));

            // *********************************************
            // Buildo l'iter
            log.info("Build dell'iter...");
            log.info("Setto la fase corrente... " + f.getNome());
            i.setIdFaseCorrente(f);
            log.info("setto procedimento... " + p.getId());
            i.setIdProcedimento(p);
            log.info("setto responsabile di procedimento... " + uResponsabile.getUsername());
            i.setIdResponsabileProcedimento(uResponsabile);
            log.info("setto l'idStrutturaResponsabileProcedimento...");
            i.setIdStrutturaResponsabileProcedimento(new Struttura(us.getIdStruttura().getId()));
            log.info("setto l'oggetto iter...");
            i.setOggetto(iterParams.getOggettoIter());
            log.info("setto lo stato IN_CORSO...");
            i.setIdStato(entitiesCachableUtilities.loadStatoByCodice(Stato.CodiciStato.IN_CORSO));
            log.info("setto la data di creazione...");
            i.setDataCreazione(iterParams.getDataCreazioneIter());
            log.info("setto la data di avvio...");
            i.setDataAvvio(iterParams.getDataAvvioIter());
            // i.setIdFascicolo(fascicolo.getNumerazioneGerarchica()); // Questo lo mettiamo dopo quando lo avremo
            log.info("setto il nome del fascicolo: " + iterParams.getOggettoIter());
            i.setNomeFascicolo(iterParams.getOggettoIter());
            log.info("Setto il idTitolo e nomeTitolo...");
            i.setIdTitolo(p.getIdAziendaTipoProcedimento().getIdTitolo());
            i.setNomeTitolo(p.getIdAziendaTipoProcedimento().getIdTitolo().getNome());
            log.info("Setto il promotore...");
            i.setPromotore(iterParams.getPromotore());
            log.info("Setto utenteCreazione (in base all'utente loggato)...");
            i.setIdUtenteCreazione(uLoggato);
            log.info("Setto la struttura dell'utente creazione (in base alla struttura dell'utente loggato)...");
            i.setIdStrutturaUtenteCreazione(idStrutturaUtenteLoggato);
            log.info("Faccio un po' di calcoli per impostare la data di chiusura prevista");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(iterParams.getDataAvvioIter());
            calendar.add(Calendar.DATE, p.getIdAziendaTipoProcedimento().getDurataMassimaProcedimento());
            log.info("Setto la data di chiusura prevista: " + calendar.getTime().toString());
            i.setDataChiusuraPrevista(calendar.getTime());
            // Genero il numero dell'iter in base all'azienda
            log.info("Genero il numero dell'iter in base all'azienda...");
            Object iNew = interceptor.onChangeInterceptor(OlingoInterceptorOperation.CREATE, i, em, null);
            log.info("set iter...");
            i = (Iter) iNew;
            em.persist(i);
            em.flush();
            log.info("Iter salvato");

            idIterTemp = i.getId();
            // *********************************************
            // Creo il fascicolo dell'iter.
            log.info("Creazione fascicolo");
            fascicolo = new Fascicolo(null, iterParams.getOggettoIter(), null, null, new DateTime(), 0,
                    Calendar.getInstance().get(Calendar.YEAR), "1", null, new DateTime(), null, null, "a",
                    0, null, -1, null, null, uLoggato.getIdPersona().getCodiceFiscale(), uResponsabile.getIdPersona().getCodiceFiscale(), null,
                    p.getIdAziendaTipoProcedimento().getIdTitolo().getClassificazione(), i.getId(), us.getIdStruttura().getId());
            fascicolo.setIdTipoFascicolo(2);
            // per uno strano bug, pare che l'id_iter del fascicolo potrebbe non essere valorizzato
            if (fascicolo.getIdIter() == null) {
                fascicolo.setIdIter(i.getId());
            }

            log.info("setto visibilità del fascicolo: " + iterParams.getVisibile().toString());
            fascicolo.setVisibile(iterParams.getVisibile());
            // Aggiungo l'elenco dei codicifiscali dei vicari
            log.info("Inserisco i codicifiscali dei vicari a un set...");
            Set<String> set = new HashSet<String>();
            if (!uLoggato.getIdPersona().getCodiceFiscale().equals(uResponsabile.getIdPersona().getCodiceFiscale())) {
                log.info("utLoggato --> " + uLoggato.getIdPersona().getCodiceFiscale());
                set.add(uLoggato.getIdPersona().getCodiceFiscale());
            }
            log.info("titPotSost --> " + p.getIdTitolarePotereSostitutivo().getIdPersona().getCodiceFiscale());
            set.add(p.getIdTitolarePotereSostitutivo().getIdPersona().getCodiceFiscale());
            log.info("respAdozAttFinale --> " + p.getIdTitolarePotereSostitutivo().getIdPersona().getCodiceFiscale());
            set.add(p.getIdResponsabileAdozioneAttoFinale().getIdPersona().getCodiceFiscale());
            List<String> vicari = new ArrayList<String>();
            // Passare da set a list è un trucco per non avere doppioni nella lista.
            log.info("Passo da Set a List per non avere doppioni");
            vicari.addAll(set);
            log.info("Setto i vicari del fascicolo...");
            fascicolo.setVicari(vicari);
            HashMap additionalData = (HashMap) new java.util.HashMap();
            log.info("Imposto l'operzione fascicolo a TRADUCI_VICARI...");
            additionalData.put(OperazioniFascicolo.TRADUCI_VICARI.toString(), true);
            log.info("Creazione di IodaRequestDescriptor...");
            IodaRequestDescriptor ird = new IodaRequestDescriptor("gipi", "gipi", fascicolo, additionalData);
            // String baseUrl = "http://localhost:8084/bds_tools/InsertFascicolo";             // Questo va spostato e reso parametrico
            String baseUrl;
            if (isLocalHost) {
                baseUrl = localhostBaseUrl;
            } else {
                baseUrl = GetBaseUrls.getBabelSuiteBdsToolsUrl(p.getIdAziendaTipoProcedimento().getIdAzienda().getId(), em, objectMapper);
            }
            log.info("baseUrl = " + baseUrl);
            String urlChiamata = baseUrl + insertFascicoloPath;
            log.info("Url chiamata chiamata per insertFascicolo= " + urlChiamata);
            OkHttpClient client = new OkHttpClient();
            log.info("IodaRequestDescriptor: " + ird.getJSONString());
            RequestBody body = RequestBody.create(JSON, ird.getJSONString().getBytes("UTF-8"));
            log.info("Preparo la request");
            Request request = new Request.Builder()
                    .url(urlChiamata)
                    .post(body)
                    .build();
            log.info("faccio la chimata...");
            Response response = client.newCall(request).execute();
            String resString = null;
            log.info("response --> " + response.toString());
            if (response != null && response.body() != null) {
                resString = response.body().string();
            }
            log.info("casto il fascicolo...");
            fascicolo = (Fascicolo) it.bologna.ausl.ioda.iodaobjectlibrary.Requestable.parse(resString, Fascicolo.class);

            // Aggiungo la numerazione gerarchica del fascicolo all'iter
            log.info("Aggiungo la numerazione gerarchica del fascicolo all'iter");
            i.setIdFascicolo(fascicolo.getNumerazioneGerarchica());
            em.persist(i);
            log.info("fascicolo salvato!");

            // *********************************************
            // Fascicolo il documento // baseUrl = "http://localhost:8084/bds_tools/ioda/api/document/update";
            urlChiamata = baseUrl + updateGdDocPath;
            log.info("urlChiamata per l'updategddoc = " + urlChiamata);
            log.info("Creo stanzza gddoc...");
            GdDoc g = new GdDoc(null, null, null, null, null, null, null, iterParams.getCodiceRegistroDocumento(), null, iterParams.getNumeroDocumento(), null, null, null, null, null, null, null, iterParams.getAnnoDocumento());
            log.debug("Creao istanza fascicolazione...");
            Fascicolazione fascicolazione = new Fascicolazione(fascicolo.getNumerazioneGerarchica(), fascicolo.getNomeFascicolo(), fascicolo.getIdUtenteCreazione(), null, DateTime.now(), Document.DocumentOperationType.INSERT);
            ArrayList a = new ArrayList();
            a.add(fascicolazione);
            log.debug("Setto le fascicolazioni sul gddoc...");
            g.setFascicolazioni(a);
            IodaRequestDescriptor irdg = new IodaRequestDescriptor("gipi", "gipi", g);
            log.debug("Preparo la chiamata a ioda: il body (MultipartBody.FORM)...");
            RequestBody bodyg = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("request_descriptor", null, okhttp3.RequestBody.create(JSON, irdg.getJSONString().getBytes("UTF-8")))
                    .build();
            log.debug("...e la request...");
            Request requestg = new Request.Builder()
                    .url(urlChiamata)
                    .post(bodyg)
                    .build();
            log.info("chiamo ioda...");
            Response responseg = client.newCall(requestg).execute();
            log.info("responseg --> " + responseg.toString());
            if (!responseg.isSuccessful()) {
                throw new IOException("La fascicolazione non è andata a buon fine.");
            }

            // *********************************************
            // Costruisco gli altri vari oggetti connessi all'iter
            // Buildo il Procedimento Cache
            log.info("buildo il procedimento");
            ProcedimentoCache pc = new ProcedimentoCache();
            pc.setId(i.getId());                                // ?? Ma qui non dovrei passere l'iter intero e non solo l'id?? forse manca fk?
            pc.setNomeTipoProcedimento(p.getIdAziendaTipoProcedimento().getIdTipoProcedimento().getNome());
            pc.setDescrizioneTipoProcedimento(p.getIdAziendaTipoProcedimento().getDescrizioneTipoProcedimento());
            pc.setIdStruttura(p.getIdStruttura());
            pc.setIdTitolarePotereSostitutivo(p.getIdTitolarePotereSostitutivo());
            pc.setIdStrutturaTitolarePotereSostitutivo(p.getIdStrutturaTitolarePotereSostitutivo());
            pc.setIdResponsabileAdozioneAttoFinale(p.getIdResponsabileAdozioneAttoFinale());
            pc.setIdStrutturaResponsabileAdozioneAttoFinale(p.getIdStrutturaResponsabileAdozioneAttoFinale());
            pc.setDurataMassimaProcedimento(p.getIdAziendaTipoProcedimento().getDurataMassimaProcedimento());
            pc.setDurataMassimaSospensione(p.getIdAziendaTipoProcedimento().getDurataMassimaSospensione());
            em.persist(pc);
            log.info("procedimento salvato");

            // Buildo la fase Iter
            log.info("buildo la faseIter");
            FaseIter fi = new FaseIter();
            fi.setIdIter(i);
            fi.setIdFase(f);
            fi.setDataInizioFase(i.getDataAvvio());
            em.persist(fi);
            log.info("fase iter salvata");

            // Mi preparo il json dati aggiuntivi. Lo userò due volte
            log.info("preparo il json dati_aggiuntivi");
            JsonObject datiAggiuntivi = new JsonObject();
            datiAggiuntivi.addProperty("azione", IterController.AzioneRichiesta.CREAZIONE.toString());
            datiAggiuntivi.addProperty("statoRichiesto", Stato.CodiciStato.IN_CORSO.toString());
            datiAggiuntivi.addProperty("sendAcipByEmail", String.valueOf(iterParams.getSendAcipByEmail()));

            JsonObject acipParams = new JsonObject();
            acipParams.addProperty("codiceRegistroDocumento", iterParams.getCodiceRegistroDocumento());
            acipParams.addProperty("numeroDocumento", String.valueOf(iterParams.getNumeroDocumento()));
            acipParams.addProperty("annoDocumento", String.valueOf(iterParams.getAnnoDocumento()));
            acipParams.addProperty("numeroIter", String.valueOf(i.getNumero()));
            acipParams.addProperty("annoIter", String.valueOf(i.getAnno()));
            acipParams.addProperty("tipoProcedimento", p.getIdAziendaTipoProcedimento().getIdTipoProcedimento().getNome());
            acipParams.addProperty("dataCreazioneIter", new SimpleDateFormat("dd/MM/yyyy").format(i.getDataCreazione()));
            acipParams.addProperty("dataAvvioIter", new SimpleDateFormat("dd/MM/yyyy").format(i.getDataAvvio()));
            acipParams.addProperty("responsabileDelProcedimento", uResponsabile.getIdPersona().getCodiceFiscale());
            acipParams.addProperty("responsabileAdozioneAttoFinale", uResponsabileAdozione.getIdPersona().getCodiceFiscale());
            acipParams.addProperty("titolarePotereEsecutivo", uTitolare.getIdPersona().getCodiceFiscale());

            acipParams.addProperty("azienda", p.getIdAziendaTipoProcedimento().getIdAzienda().getDescrizione());
            acipParams.addProperty("oggettoIter", i.getOggetto());
            acipParams.addProperty("StrutturaResponsabileDelProcedimento", p.getIdStrutturaTitolarePotereSostitutivo().getNome());
            // questi poi?? come faremo??
            acipParams.addProperty("dataRegistrazione", new SimpleDateFormat("dd/MM/yyyy").format(iterParams.getDataRegistrazioneDocumento()));

            Date chiusuraPrevista = new Date();

            int durataMassimaProcedimento = i.getIdProcedimento().getIdAziendaTipoProcedimento().getDurataMassimaProcedimento();
            Calendar cal = Calendar.getInstance();
            cal.setTime(i.getDataAvvio());
            cal.add(Calendar.DATE, durataMassimaProcedimento); // aggiungo la durata massima del procedimento.
            chiusuraPrevista = cal.getTime();

            acipParams.addProperty("dataConclusionePrevista", new SimpleDateFormat("dd/MM/yyyy").format(chiusuraPrevista));

            datiAggiuntivi.addProperty("acipParams", acipParams.toString());
            log.info("dati_aggiuntivi --> " + datiAggiuntivi.toString());

            // Buildo il documento
            DocumentoIter di = new DocumentoIter();
            log.info("preparo il documentoIter");
            di.setIdIter(i);
            di.setNumeroRegistro(String.valueOf(iterParams.getNumeroDocumento()));
            di.setAnno(iterParams.getAnnoDocumento());
            di.setRegistro(iterParams.getCodiceRegistroDocumento());
            di.setOggetto(iterParams.getOggettoDocumento());
            di.setIdOggetto(iterParams.getIdOggettoOrigine());
            di.setDescrizione(iterParams.getDescrizione());
            di.setParziale(Boolean.FALSE);
            di.setDatiAggiuntivi(datiAggiuntivi.toString());
            em.persist(di);
            log.info("documentoIter salvato");

            // Buildo l'evento Iter
            log.info("Buildo evento iter");
            EventoIter ei = new EventoIter();
            ei.setIdEvento(e);
            ei.setIdIter(i);
            ei.setIdFaseIter(fi);
            ei.setDataOraEvento(iterParams.getDataAvvioIter());
            ei.setIdDocumentoIter(di);
            ei.setAutore(uLoggato);
            em.persist(ei);
            log.info("eventoIter salvato");

            // Comunico a Babel l'iter appena creato
            baseUrl = GetBaseUrls.getBabelSuiteWebApiUrl(p.getIdAziendaTipoProcedimento().getIdAzienda().getId(), em, objectMapper);
            urlChiamata = baseUrl + switchGestisciIterPahtByCodiceRegistro(iterParams.getCodiceRegistroDocumento());

            // logalhost da commentare
            // urlChiamata = "http://localhost:8080" + switchGestisciIterPahtByCodiceRegistro(iterParams.getCodiceRegistroDocumento());
            log.info("urlChiamata per la web api PDD --> " + urlChiamata);

            JsonObject o = new JsonObject();
            o.addProperty("idIter", i.getId());
            o.addProperty("numeroIter", i.getNumero());
            o.addProperty("annoIter", i.getAnno());
            o.addProperty("cfResponsabileProcedimento", uResponsabile.getIdPersona().getCodiceFiscale());
            o.addProperty("nomeProcedimento", p.getIdAziendaTipoProcedimento().getIdTipoProcedimento().getNome());
            o.addProperty("codiceRegistroDocumento", iterParams.getCodiceRegistroDocumento());
            o.addProperty("numeroDocumento", iterParams.getNumeroDocumento());
            o.addProperty("annoDocumento", iterParams.getAnnoDocumento());
            o.addProperty("idOggettoOrigine", iterParams.getIdOggettoOrigine());
            o.addProperty("datiAggiuntivi", datiAggiuntivi.toString());
            o.addProperty("glogParams", iterParams.getGlogParams());
            o.addProperty("modificaAssociazioneParziale", 0); // non sto modificando un'associazione parziale, sto creando sicuramente un'associazione nuova!

            body = RequestBody.create(JSON, o.toString().getBytes("UTF-8"));

            log.info("JSON inviato --> " + o.toString());

            log.info("body a pdd questi dati --> " + body.toString());

            log.info("chiamo pdd...");
            requestg = new Request.Builder()
                    .url(urlChiamata)
                    .addHeader("X-HTTP-Method-Override", "associaDocumento")
                    .post(body)
                    .build();

            client = new OkHttpClient();
            responseg = client.newCall(requestg).execute();
            log.info("responseg --> " + responseg.toString());
            if (responseg != null && responseg.body() != null) {
                log.info("GDM RESPONSE STRING = " + responseg.body().string());
                log.info("GDM RESPONSE MESSAGE = " + responseg.message());
                log.info("GDM RESPONSE ISREDIRECT = " + responseg.isRedirect());
                log.info("GDM RESPONSE TOSTRING= " + responseg.body().toString());
            } else {
                log.info("la response è null");
            }

            if (!responseg.isSuccessful()) {
                log.info("la risposta non è successful --> La chiamata a Babel non è andata a buon fine");
                throw new IOException("La chiamata a Babel non è andata a buon fine.");
            }

            log.info("mando messaggio a primus di aggiornare finestra aperta pdd");
            // Lancio comando a primus per aggiornamento istantaneo del box dati di archivio
            List<String> cfUtentiDaRefreshare = new ArrayList<>();
            cfUtentiDaRefreshare.add(uLoggato.getIdPersona().getCodiceFiscale());
            PrimusCommandParams command = new RefreshBoxDatiDiArchivioCommandParams();
            try {
                utilityFunctions.sendPrimusCommand(uLoggato.getIdAzienda(), cfUtentiDaRefreshare, command, iterParams.getIdApplicazione());
            } catch (Throwable t) {
                log.error("ERRORE nel mandare il PrimusCommand per aggiornare la pagine dell'utente... Eh vabbè...");
                log.error("Message: " + t.getMessage());
                log.error("LocalizedMessage: " + t.getLocalizedMessage());
                log.error("Vado avanti lo stesso");
            }

        } catch (Throwable t) {
            log.error("ERRORE in creatIter! --> " + t.getMessage());
            log.error("Messaggio localizzato: " + t.toString());
            if (t.getCause() != null) {
                log.error("Causa: " + t.getCause().getMessage());
            }
            log.error("Ora arriva la parte difficile: eliminare il fascicolo e la fascicolazione appena create....");
            boolean eliminazioneOk = false;
            try {
                eliminazioneOk = procedureEliminazionePerErroreCreazione(fascicolo, p, isLocalHost, idIterTemp, cfResponsabileProcedimento);
            } catch (Throwable error) {
                log.error("Non sono riuscito a sistemare le cose..." + error.toString());
            }
            log.error("Sono riuscito a eliminare i dati iter? " + eliminazioneOk);
            throw t;
        }
        log.info("Ritorno l'iter");

        return i;
    }

    public Utente getUtenteLoggatoFromAutenticationCache() {
        int idUtenteLoggato = getIdUtenteLoggatoFromAuthenticationCache();
        Utente uLoggato = GetEntityById.getUtente(idUtenteLoggato, em);
        return uLoggato;
    }

    public int getIdUtenteLoggatoFromAuthenticationCache() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.debug("Carico userInfo...");
        UtenteCachable userInfo = (UtenteCachable) authentication.getPrincipal();
        log.debug("Carico idUtenteLoggato...");
        Integer idUtenteLoggato = (Integer) userInfo.get(UtenteCachable.KEYS.ID);
        log.info("IdUtenteLoggato" + idUtenteLoggato.toString());
        return idUtenteLoggato;
    }

    private boolean deleteFascicoloPerErroreCreazione(Fascicolo fascicolo, Procedimento p, boolean isLocalHost) throws JsonProcessingException, UnsupportedEncodingException, IOException {
        boolean fatto = false;
        log.info("Creazione di IodaRequestDescriptor...");
        IodaRequestDescriptor ird = new IodaRequestDescriptor("gipi", "gipi", fascicolo);
        String baseUrl;
        if (isLocalHost) {
            baseUrl = localhostBaseUrl;
        } else {
            baseUrl = GetBaseUrls.getBabelSuiteBdsToolsUrl(p.getIdAziendaTipoProcedimento().getIdAzienda().getId(), em, objectMapper);
        }
        log.info("baseUrl = " + baseUrl);
        String urlChiamata = baseUrl + deleteFascicoloPath;
        log.info("Url chiamata chiamata per deleteFascicoloPath = " + urlChiamata);
        OkHttpClient client = new OkHttpClient();
        log.info("IodaRequestDescriptor: " + ird.getJSONString());
        RequestBody body = RequestBody.create(JSON, ird.getJSONString().getBytes("UTF-8"));
        log.info("Preparo la request");
        Request request = new Request.Builder()
                .url(urlChiamata)
                .post(body)
                .build();
        log.info("faccio la chiamata di delete fascicolo...");
        Response response = client.newCall(request).execute();
        String resString = null;
        log.info("response --> " + response.toString());
        if (response != null && response.body() != null) {
            resString = response.body().string();
            if (resString.equals("true")) {
                log.info("Le questioni del fascicolo " + fascicolo.getNumerazioneGerarchica()
                        + "e delle sue fascicolazioni sono state sistemate...");
                fatto = true;
            }
        }
        return fatto;
    }

    public boolean deleteDocumentoIterFromBabelPerErroreCreazione(int idIter, Procedimento p, boolean isLocalHost, String cfResponsabileProcedimento) throws IOException {
        log.info("Ora mi occupo di eliminare le associazioni del documento con l'iter " + idIter);
        boolean fatto = false;
        log.info("Calcolo il BaseUrl per Babel...");
        String baseUrl = GetBaseUrls.getBabelSuiteWebApiUrl(p.getIdAziendaTipoProcedimento().getIdAzienda().getId(), em, objectMapper);

        log.info("Get Utente loggato...");
        Utente uLoggato = getUtenteLoggatoFromAutenticationCache();

        String urlChiamataPDD = baseUrl + deleteDocumentoIterPerErroreCreazione;
        log.info("chiamo pdd... --> " + urlChiamataPDD);

        // Ora, se abbiamo il cf del responsabile procedimento, usiamo quello per gloggare, altrimenti usiamo l'utente loggato.
        String cf = cfResponsabileProcedimento != null ? cfResponsabileProcedimento : uLoggato.getIdPersona().getCodiceFiscale();
        JsonObject o = new JsonObject();
        o.addProperty("idIter", idIter);
        o.addProperty("cf", cf);
        log.info("JSON inviato per cancellazione--> " + o.toString());

        RequestBody bodyPDD = RequestBody.create(JSON, o.toString().getBytes("UTF-8"));
        log.info("body per cancellazione questi dati --> " + bodyPDD.toString());

        Request requestPDD = new Request.Builder()
                .url(urlChiamataPDD)
                .addHeader("X-HTTP-Method-Override", "main")
                .post(bodyPDD)
                .build();

        OkHttpClient client = new OkHttpClient();

        Response responsePDD = client.newCall(requestPDD).execute();
        log.info("responsePDD --> " + responsePDD.toString());
        if (responsePDD.isSuccessful()) {
            fatto = true;
        } else {
            log.error("Response Message: " + responsePDD.message());
            log.error("Response body: " + responsePDD.body().toString());
        }

        return fatto;
    }

    public boolean procedureEliminazionePerErroreCreazione(Fascicolo fascicolo, Procedimento p, boolean isLocalHost,
            Integer idIterTemp, String cfResponsabileProcedimento) throws JsonProcessingException, UnsupportedEncodingException, IOException {
        log.info("Entrato in procedureEliminazionePerErroreCreazione()");
        boolean fatto = false;

        int idIter = idIterTemp;

        // eliminare da documenti_iter by id_iter
        fatto = deleteFascicoloPerErroreCreazione(fascicolo, p, isLocalHost); // eliminare da fascicoligd by id_fascicolo

        // eliminare da fascicoli_gddocs by id_fascicolo
        if (fatto) {
            fatto = deleteDocumentoIterFromBabelPerErroreCreazione(idIter, p, isLocalHost, cfResponsabileProcedimento);
        }
        return fatto;
    }

    public String switchGestisciIterPahtByCodiceRegistro(String codiceRegistroDocumento) {
        String path = babelGestisciIterPath;
        switch (codiceRegistroDocumento) {
            case "DETE":
                path = deteGestisciIterPath;
                break;

            case "DELI":
                path = deliGestisciIterPath;
                break;

            case "PG":
                path = proctonGestisciIterPath;
                break;
        }
        return path;
    }
}
