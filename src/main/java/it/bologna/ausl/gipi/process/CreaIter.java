package it.bologna.ausl.gipi.process;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
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

    public static enum InsertFascicolo {
        TRADUCI_VICARI
    }

    @Value("${insertFascicolo}")
    private String insertFascicoloPath;

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
        JPQLQuery<Evento> query = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
        Evento e = query
                .from(this.qEvento)
                .where(this.qEvento.codice.eq(EVENTO_CREAZIONE_ITER))
                .fetchFirst();
        return e;
    }

    public int getNumeroIterMax() {
        JPQLQuery<Iter> query = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
        int i = query.select(this.qIter.numero.max())
                .from(this.qIter).fetchFirst();
        return i;
    }

    public Iter creaIter(IterParams iterParams, boolean isLocalHost) throws IOException, OlingoRequestRollbackException {

        // Mi prendo l'idUtente loggato
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UtenteCachable userInfo = (UtenteCachable) authentication.getPrincipal();
        Integer idUtenteLoggato = (Integer) userInfo.get(UtenteCachable.KEYS.ID);
        log.info("IdUtenteLoggato" + idUtenteLoggato.toString());
        
        // Mi carico i dati di cui ho bisogno per creare l'iter.
        Procedimento p = GetEntityById.getProcedimento(iterParams.getIdProcedimento(), em);
        
        log.info("Carico utente loggato");
        Utente uLoggato = GetEntityById.getUtente(idUtenteLoggato, em);
        log.info("Carico la struttura di afferenza diretta dell'utente loggato");
        
        JPQLQuery<UtenteStruttura> query = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
        List<UtenteStruttura> utenteStrutturaDelCreatore = new ArrayList<>();
        utenteStrutturaDelCreatore = query.select(this.qUtenteStruttura)
                .from(this.qUtenteStruttura)
                .where(this.qUtenteStruttura.idUtente.id.eq(uLoggato.getId())
                .and(this.qUtenteStruttura.idAfferenzaStruttura.codice.eq(AfferenzaStruttura.CodiciAfferenzaStruttura.DIRETTA.toString())))
                .fetch();
        log.info("Quante strutture utente ho trovato con afferenza diretta per l'utente loggato? " + utenteStrutturaDelCreatore.size());
        Struttura idStrutturaUtenteLoggato = utenteStrutturaDelCreatore.size() > 0 ? utenteStrutturaDelCreatore.get(0).getIdStruttura() : new Struttura();
        
        
        log.info("Carico utente responsabile");
        Utente uResponsabile = GetEntityById.getUtente(iterParams.getIdUtenteResponsabile(), em);
        
        log.info("Carico utente_struttura del responsabile");
        UtenteStruttura us = GetEntityById.getUtenteStruttura(iterParams.getIdUtenteStrutturaResponsabile(), em);
        log.info("Carico utente resp. adoz.");
        Utente uResponsabileAdozione = GetEntityById.getUtente(p.getIdResponsabileAdozioneAttoFinale().getId(), em);
        log.info("Carico utente titolare pot. esec.");
        Utente uTitolare = GetEntityById.getUtente(p.getIdTitolarePotereSostitutivo().getId(), em);
        Fase f = this.getFaseIniziale(p.getIdAziendaTipoProcedimento().getIdAzienda().getId());
        Evento e = this.getEventoCreazioneIter();
        // Sistemo il numero documento che ho in iterParams deve avere 7 cifre, se non le ha, aggiungo degli zeri da sinistra
        iterParams.setNumeroDocumento(String.format("%07d", Integer.parseInt(iterParams.getNumeroDocumento())));

        // *********************************************
        // Buildo l'iter
        log.info("Build dell'iter...");
        Iter i = new Iter();
        i.setIdFaseCorrente(f);
        i.setIdProcedimento(p);
        i.setIdResponsabileProcedimento(uResponsabile);
        i.setIdStrutturaResponsabileProcedimento(new Struttura(us.getIdStruttura().getId()));
        i.setOggetto(iterParams.getOggettoIter());
        i.setIdStato(entitiesCachableUtilities.loadStatoByCodice(Stato.CodiciStato.IN_CORSO));
        i.setDataCreazione(iterParams.getDataCreazioneIter());
        i.setDataAvvio(iterParams.getDataAvvioIter());
        // i.setIdFascicolo(fascicolo.getNumerazioneGerarchica()); // Questo lo mettiamo dopo quando lo avremo
        i.setNomeFascicolo(iterParams.getOggettoIter());
        i.setIdTitolo(p.getIdAziendaTipoProcedimento().getIdTitolo());
        i.setNomeTitolo(p.getIdAziendaTipoProcedimento().getIdTitolo().getNome());
        i.setPromotore(iterParams.getPromotore());
        i.setIdUtenteCreazione(uLoggato);
        i.setIdStrutturaUtenteCreazione(idStrutturaUtenteLoggato);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(iterParams.getDataAvvioIter());
        calendar.add(Calendar.DATE, p.getIdAziendaTipoProcedimento().getDurataMassimaProcedimento());
        i.setDataChiusuraPrevista(calendar.getTime());
        // Genero il numero dell'iter in base all'azienda
        Object iNew = interceptor.onChangeInterceptor(OlingoInterceptorOperation.CREATE, i, em, null);
        i = (Iter) iNew;
        em.persist(i);
        em.flush();
        log.info("Iter salvato");
        // *********************************************
        // Creo il fascicolo dell'iter.
        log.info("Creazione fascicolo");
        Fascicolo fascicolo = new Fascicolo(null, iterParams.getOggettoIter(), null, null, new DateTime(), 0,
                Calendar.getInstance().get(Calendar.YEAR), "1", null, new DateTime(), null, null, "a",
                0, null, -1, null, null, uLoggato.getIdPersona().getCodiceFiscale(), uResponsabile.getIdPersona().getCodiceFiscale(), null,
                p.getIdAziendaTipoProcedimento().getIdTitolo().getClassificazione(), i.getId());
        fascicolo.setIdTipoFascicolo(2);
        // Aggiungo l'elenco dei codicifiscali dei vicari
        Set<String> set = new HashSet<String>();
        if (!uLoggato.getIdPersona().getCodiceFiscale().equals(uResponsabile.getIdPersona().getCodiceFiscale())) {
            set.add(uLoggato.getIdPersona().getCodiceFiscale());
        }
        set.add(p.getIdTitolarePotereSostitutivo().getIdPersona().getCodiceFiscale());
        set.add(p.getIdResponsabileAdozioneAttoFinale().getIdPersona().getCodiceFiscale());
        List<String> vicari = new ArrayList<String>();
        // Passare da set a list è un trucco per non avere doppioni nella lista.
        vicari.addAll(set);
        log.info("Setto i vicari del fascicolo...");
        fascicolo.setVicari(vicari);
        HashMap additionalData = (HashMap) new java.util.HashMap();
        additionalData.put(InsertFascicolo.TRADUCI_VICARI.toString(), true);
        IodaRequestDescriptor ird = new IodaRequestDescriptor("gipi", "gipi", fascicolo, additionalData);
        // String baseUrl = "http://localhost:8084/bds_tools/InsertFascicolo";             // Questo va spostato e reso parametrico
        String baseUrl;
        if (isLocalHost) {
            baseUrl = localhostBaseUrl;
        } else {
            baseUrl = GetBaseUrls.getBabelSuiteBdsToolsUrl(p.getIdAziendaTipoProcedimento().getIdAzienda().getId(), em, objectMapper);
        }
        String urlChiamata = baseUrl + insertFascicoloPath;
        log.info("Url chiamata chiamata = " + urlChiamata);
        OkHttpClient client = new OkHttpClient();
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
        log.info("Aggiungo la numerazione gerarchica del fascicolo all'iter" );
        i.setIdFascicolo(fascicolo.getNumerazioneGerarchica());
        em.persist(i);
        log.info("fascicolo salvato!");

        // *********************************************
        // Fascicolo il documento // baseUrl = "http://localhost:8084/bds_tools/ioda/api/document/update";
        urlChiamata = baseUrl + updateGdDocPath;
        log.info("urlChiamata per l'updategddoc = " + urlChiamata);
        GdDoc g = new GdDoc(null, null, null, null, null, null, null, iterParams.getCodiceRegistroDocumento(), null, iterParams.getNumeroDocumento(), null, null, null, null, null, null, null, iterParams.getAnnoDocumento());
        Fascicolazione fascicolazione = new Fascicolazione(fascicolo.getNumerazioneGerarchica(), fascicolo.getNomeFascicolo(), fascicolo.getIdUtenteCreazione(), null, DateTime.now(), Document.DocumentOperationType.INSERT);
        ArrayList a = new ArrayList();
        a.add(fascicolazione);
        g.setFascicolazioni(a);
        IodaRequestDescriptor irdg = new IodaRequestDescriptor("gipi", "gipi", g);
        RequestBody bodyg = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("request_descriptor", null, okhttp3.RequestBody.create(JSON, irdg.getJSONString().getBytes("UTF-8")))
                .build();
        Request requestg = new Request.Builder()
                .url(urlChiamata)
                .post(bodyg)
                .build();
        log.info("chiamo ioda");
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
        ei.setDataOraEvento(iterParams.getDataCreazioneIter());
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
        utilityFunctions.sendPrimusCommand(uLoggato.getIdAzienda(), cfUtentiDaRefreshare, command, iterParams.getIdApplicazione());
        
        log.info("Ritorno l'iter");
        return i;
    }
    
    public String switchGestisciIterPahtByCodiceRegistro(String codiceRegistroDocumento){
        String path = babelGestisciIterPath;
        switch(codiceRegistroDocumento){
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
