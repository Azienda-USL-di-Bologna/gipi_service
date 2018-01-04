/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.controllers;

import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.gipi.Fase;
import it.bologna.ausl.entities.gipi.Iter;
import it.bologna.ausl.entities.gipi.QIter;
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
import it.bologna.ausl.entities.gipi.DocumentoIter;
import it.bologna.ausl.entities.gipi.Evento;
import it.bologna.ausl.entities.gipi.EventoIter;
import it.bologna.ausl.entities.gipi.FaseIter;
import it.bologna.ausl.entities.gipi.QEvento;
import it.bologna.ausl.entities.gipi.QEventoIter;
import it.bologna.ausl.entities.gipi.QFaseIter;
import it.bologna.ausl.gipi.exceptions.GipiDatabaseException;
import it.bologna.ausl.gipi.exceptions.GipiRequestParamsException;
import it.bologna.ausl.gipi.process.CreaIter;
import it.bologna.ausl.gipi.utils.GetEntityById;
import java.io.IOException;
import java.text.ParseException;
import javax.servlet.http.HttpServletRequest;

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

    @RequestMapping(value = "getProcessStatus", method = RequestMethod.GET)
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
        
//        JPQLQuery<EventoIter> q2 = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
//        EventoIter ei = q
//                .from(qEventoIter)
//                .where(qEventoIter.idIter.id.eq(idIter)
//                    .and(qEventoIter.idEvento.eq(q2.from(qEvento).where(qEvento.codice.eq("apertura_sospensione").isTrue()))
//                .orderBy(qEventoIter.dataOraEvento.desc())
//                .fetchAll();
        
        JsonObject eventoSospensione = new JsonObject();
        eventoSospensione.addProperty("dataSospensione", ei.getDataOraEvento().toString());
        return new ResponseEntity(eventoSospensione.toString(), HttpStatus.OK);
    }
    
    public FaseIter getFaseIter(Iter i) {
        QFaseIter qFaseIter = QFaseIter.faseIter;
        JPQLQuery<FaseIter> q = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);
        
        FaseIter fi = q
                .from(qFaseIter)
                .where(qFaseIter.idIter.id.eq(i.getId())
                        .and(qFaseIter.idFase.id.eq(i.getIdFaseCorrente().getId())))
                .orderBy(qFaseIter.dataInizioFase.desc())
                .fetchFirst();
        return fi;
    }
    
    @RequestMapping(value = "terminaSospensione", method = RequestMethod.POST)
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public ResponseEntity terminaSospensione(@RequestBody SospensioneParams sospensioneParams){
        
        Evento e = GetEntityById.getEventoByCodice("chiusura_sospensione", em);
        Utente u = GetEntityById.getUtente(sospensioneParams.idUtente, em);
        Iter i = GetEntityById.getIter(sospensioneParams.idIter, em);
        FaseIter fi = getFaseIter(i);
        
        i.setStato("in_corso");
        em.persist(i);
        
        DocumentoIter d = new DocumentoIter();
        d.setAnno(sospensioneParams.getAnnoDocumento());
        d.setIdIter(i);
        d.setNumeroRegistro(sospensioneParams.getNumeroDocumento());
        d.setRegistro(sospensioneParams.getCodiceRegistroDocumento());
        em.persist(d);
        em.flush();
         System.out.println("s££££££££££££££££££££££££££££££££££££££££££££££££££££");
        System.out.println(e.toString());
        System.out.println(i.toString());
        System.out.println(fi.toString());
        System.out.println(d.toString());
        System.out.println(u.toString());
        System.out.println(sospensioneParams.getSospesoAl());
         System.out.println("sono ££££££££££££££££££££££££££££££££££££££££££££££££££qua");
        EventoIter ei = new EventoIter();
        ei.setIdDocumentoIter(d);
        ei.setNote(sospensioneParams.getNote());
        ei.setIdEvento(e);
        ei.setIdIter(i);
        ei.setAutore(u);
        ei.setDataOraEvento(sospensioneParams.getSospesoAl());
        ei.setIdFaseIter(fi);
        System.out.println("sono qua");
        System.out.println(ei.toString());
        em.persist(ei);
        System.out.println("sono qua3333");
        
        JsonObject eventoSospensione = new JsonObject();
        eventoSospensione.addProperty("dataFineSospensione", ei.getDataOraEvento().toString());
        
        return new ResponseEntity(eventoSospensione.toString(), HttpStatus.OK);
    }
    
    
    @RequestMapping(value = "lanciaSospensione", method = RequestMethod.POST)
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public ResponseEntity lanciaSospensione(@RequestBody SospensioneParams sospensioneParams){
        
        System.out.println("SOSPENSIONEPARAMS: ");
        System.out.println(sospensioneParams.toString());
        
        Evento e = GetEntityById.getEventoByCodice("apertura_sospensione", em);
        System.out.println("EVENTO: ");
        System.out.println(e.toString());
        Utente u = GetEntityById.getUtente(sospensioneParams.idUtente, em);
        System.out.println("UTENTE: ");
        System.out.println(u.toString());
        Iter i = GetEntityById.getIter(sospensioneParams.idIter, em);
        System.out.println("ITER: ");
        System.out.println(i.toString());
        FaseIter fi = getFaseIter(i);
        System.out.println("FASEITER: ");
        System.out.println(fi.toString());
        
        i.setStato("sospeso");
        em.persist(i);
        
        DocumentoIter d = new DocumentoIter();
        d.setAnno(sospensioneParams.getAnnoDocumento());
        d.setIdIter(i);
        d.setNumeroRegistro(sospensioneParams.getNumeroDocumento());
        d.setRegistro(sospensioneParams.getCodiceRegistroDocumento());
        em.persist(d);
        em.flush();
        System.out.println("s££££££££££££££££££££££££££££££££££££££££££££££££££££");
        System.out.println(e.toString());
        System.out.println(i.toString());
        System.out.println(fi.toString());
        System.out.println(d.toString());
        System.out.println(u.toString());
        System.out.println(sospensioneParams.getSospesoAl());
         System.out.println("sono ££££££££££££££££££££££££££££££££££££££££££££££££££qua");
        EventoIter ei = new EventoIter();
        ei.setIdDocumentoIter(d);
        ei.setNote(sospensioneParams.getNote());
        ei.setIdEvento(e);
        ei.setIdIter(i);
        ei.setAutore(u);
        ei.setDataOraEvento(sospensioneParams.getSospesoAl());
        ei.setIdFaseIter(fi);
        System.out.println("sono qua");
        System.out.println(ei.toString());
        em.persist(ei);
        System.out.println("sono qua3333");
        
        JsonObject eventoSospensione = new JsonObject();
        eventoSospensione.addProperty("dataSospensione", ei.getDataOraEvento().toString());
        
        return new ResponseEntity(eventoSospensione.toString(), HttpStatus.OK);
    }
}
