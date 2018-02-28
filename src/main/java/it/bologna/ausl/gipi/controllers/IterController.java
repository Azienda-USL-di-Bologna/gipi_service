/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.controllers;

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
import io.jsonwebtoken.Claims;
import it.bologna.ausl.entities.baborg.Utente;
import it.bologna.ausl.entities.cache.cachableobject.AziendaCachable;
import it.bologna.ausl.entities.cache.cachableobject.UtenteCachable;
import it.bologna.ausl.entities.gipi.DocumentoIter;
import it.bologna.ausl.entities.gipi.Evento;
import it.bologna.ausl.entities.gipi.EventoIter;
import it.bologna.ausl.entities.gipi.FaseIter;
import it.bologna.ausl.entities.gipi.QEvento;
import it.bologna.ausl.entities.gipi.QEventoIter;
import it.bologna.ausl.entities.gipi.QFaseIter;
import it.bologna.ausl.entities.gipi.QIter;
import it.bologna.ausl.gipi.exceptions.GipiDatabaseException;
import it.bologna.ausl.gipi.exceptions.GipiRequestParamsException;
import it.bologna.ausl.gipi.process.CreaIter;
import static it.bologna.ausl.gipi.process.CreaIter.JSON;
import it.bologna.ausl.gipi.utils.GetBaseUrl;
import it.bologna.ausl.gipi.utils.GetEntityById;
import it.bologna.ausl.ioda.iodaobjectlibrary.Fascicoli;
import it.bologna.ausl.ioda.iodaobjectlibrary.Fascicolo;
import it.bologna.ausl.ioda.iodaobjectlibrary.IodaRequestDescriptor;
import it.bologna.ausl.ioda.iodaobjectlibrary.Researcher;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 * @author f.gusella
 */
@RestController
@RequestMapping("/gipi/resources/custom/iter")
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
    private String baseUrlBdsGetFascicoliUtente;
    
    @Value("${babelGestisciIter}")
    private String baseUrlBabelGestisciIter;
    
    @Value("${hasPermissionOnFascicolo}")
    private String baseUrlhasPermissionOnFascicolo;
    
    public static enum GetFascicoliUtente {TIPO_FASCICOLO, SOLO_ITER, CODICE_FISCALE}

    QIter qIter = QIter.iter;
    QEventoIter qEventoIter = QEventoIter.eventoIter;
    QEvento qEvento = QEvento.evento;

    @RequestMapping(value = "avviaNuovoIter", method = RequestMethod.POST)
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public ResponseEntity<Iter> AvviaNuovoIter(@RequestBody IterParams data)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException {

        Iter i = creaIter.creaIter(data);

        JsonObject o = new JsonObject();
        o.addProperty("idIter", i.getId().toString());

        return new ResponseEntity(o.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "stepOn", method = RequestMethod.POST)
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public ResponseEntity stepOn(@RequestBody SteponParams data, HttpServletRequest request) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, ParseException, GipiRequestParamsException {
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

        Claims attribute = (Claims) request.getAttribute("claims");
        String usernameLoggedUser = (String) attribute.get("sub");

        process.stepOn(iter, processSteponParams, usernameLoggedUser);

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
        if (nextFase == null) {
            throw new GipiDatabaseException("La fase successiva e' null");
        }

        JsonObject jsonCurrFase = new JsonObject();
        JsonObject jsonNextFase = new JsonObject();
        jsonCurrFase.addProperty("nomeFase", currentFase.getNome());
        jsonCurrFase.addProperty("faseDiChiusura", currentFase.getFaseDiChiusura());
        jsonNextFase.addProperty("nomeFase", nextFase.getNome());
        jsonNextFase.addProperty("faseDiChiusura", nextFase.getFaseDiChiusura());
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
        Utente u = GetEntityById.getUtente(gestioneStatiParams.idUtente, em);
        Iter i = GetEntityById.getIter(gestioneStatiParams.idIter, em);
        Evento e = GetEntityById.getEventoByCodice(gestioneStatiParams.getStato(), em);
        FaseIter fi = getFaseIter(i);
        
        // Aggiorno l'iter
        i.setStato(gestioneStatiParams.getStato());
        em.persist(i);
        
        // Creo il documento iter
        DocumentoIter d = new DocumentoIter();
        d.setAnno(gestioneStatiParams.getAnnoDocumento());
        d.setIdIter(i);
        d.setNumeroRegistro(gestioneStatiParams.getNumeroDocumento());
        d.setRegistro(gestioneStatiParams.getCodiceRegistroDocumento());
        em.persist(d);
        em.flush();
        
        // Creo l'evento iter
        EventoIter ei = new EventoIter();
        ei.setIdDocumentoIter(d);
        ei.setNote(gestioneStatiParams.getNote());
        ei.setIdEvento(e);
        ei.setIdIter(i);
        ei.setAutore(u);
        ei.setDataOraEvento(gestioneStatiParams.getDataEvento());
        ei.setIdFaseIter(fi);
        em.persist(ei);
        
        // Comunico a Babel l'associazione documento/iter appena avvenuta
//        String baseUrl = GetBaseUrl.getBaseUrl(i.getIdProcedimento().getIdAziendaTipoProcedimento().getIdAzienda().getId(), em, objectMapper) + baseUrlBabelGestisciIter;
        String baseUrl = "http://gdml:8080" + baseUrlBabelGestisciIter;
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

        okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, o.toString().getBytes("UTF-8"));
        
        Request requestg = new Request.Builder()
                .url(baseUrl)
                .addHeader("X-HTTP-Method-Override", "associaDocumento")
                .post(body)
                .build();
        
        OkHttpClient client = new OkHttpClient();
        Response responseg = client.newCall(requestg).execute();

        if (!responseg.isSuccessful()) {
            throw new IOException("La chiamata a Babel non è andata a buon fine.");
        }
        
        JsonObject obj = new JsonObject();
        o.addProperty("idIter", i.getId().toString());
        
        return new ResponseEntity(obj.toString(), HttpStatus.OK);
    }
    
    @RequestMapping(value = "hasPermissionOnFascicolo", method = RequestMethod.POST)
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public ResponseEntity hasPermissionOnFascicolo(@RequestBody Map<String,Object> data) throws IOException {
//        int i = Integer.parseInt(data.get("utenteLoggato").toString());
//        String numerazioneGerarchica = data.get("numerazioneGerarchica").toString();
        
        // String baseUrl = "http://gdml:8080" + baseUrlhasPermissionOnFascicolo;  //gdml
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UtenteCachable userInfo = (UtenteCachable) authentication.getPrincipal();
        String codiceFiscale = (String) userInfo.get(UtenteCachable.KEYS.CODICE_FISCALE);
        
        AziendaCachable aziendaInfo = (AziendaCachable)  userInfo.get(UtenteCachable.KEYS.AZIENDA_LOGIN);
        int idAzienda = (int) aziendaInfo.get(AziendaCachable.KEYS.ID);
        String baseUrl = GetBaseUrl.getBaseUrl(idAzienda, em, objectMapper) +  baseUrlhasPermissionOnFascicolo;  //localhost       
        
        Researcher r = new Researcher(null, null, 0);
        HashMap additionalData = (HashMap) new java.util.HashMap();
        additionalData.put("user", codiceFiscale);
        additionalData.put("ng", data.get("numerazioneGerarchica").toString());

        IodaRequestDescriptor irdg = new IodaRequestDescriptor("gipi", "gipi", r, additionalData);
        okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, irdg.getJSONString().getBytes("UTF-8"));
        // body = okhttp3.RequestBody.create(JSON, o.toString().getBytes("UTF-8"));      

        Request requestg = new Request.Builder()
                .url(baseUrl)
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
        
        System.out.println("OK!!!  " + responseg);
        System.out.println("hasPermission??? ---> " + responseg.header("hasPermssion"));
        
        return new ResponseEntity(responseg.header("hasPermssion"), HttpStatus.OK);
    }
}
