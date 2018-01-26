/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.gson.JsonObject;
import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.baborg.Azienda;
import it.bologna.ausl.entities.baborg.Pec;
import it.bologna.ausl.entities.baborg.QAzienda;
import it.bologna.ausl.entities.baborg.QPec;
import it.bologna.ausl.entities.gipi.Fase;
import it.bologna.ausl.entities.gipi.Iter;
import it.bologna.ausl.entities.gipi.Procedimento;
import it.bologna.ausl.entities.gipi.QProcedimento;
import it.bologna.ausl.gipi.exceptions.GipiRequestParamsException;
import it.bologna.ausl.gipi.odata.complextypes.StrutturaCheckTipoProcedimento;
import static it.bologna.ausl.gipi.process.CreaIter.JSON;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import it.bologna.ausl.gipi.process.Process;
import it.bologna.ausl.ioda.iodaobjectlibrary.Document;
import it.bologna.ausl.ioda.iodaobjectlibrary.Fascicolazione;
import it.bologna.ausl.ioda.iodaobjectlibrary.Fascicolo;
import it.bologna.ausl.ioda.iodaobjectlibrary.GdDoc;
import it.bologna.ausl.ioda.iodaobjectlibrary.IodaRequestDescriptor;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.apache.http.entity.ContentType;

/**
 *
 * @author user
 */
@RestController
@RequestMapping("/gipi/resources/custom/tests")
public class Tests {

    @Autowired
    Process process;

    @Autowired
    EntityManager em;

//    @RequestMapping(value = "getNextFase", method = RequestMethod.GET)
//    public ResponseEntity getNextFase() {
//
//        Fase fase = new Fase();
//        fase.setId(1);
//        fase.setNomeFase("Prova");
//        fase.setOrdinale(2);
//
//        process.get(fase);
//        return new ResponseEntity(HttpStatus.OK);
//    }
    
    @RequestMapping(value = "TestFascicolazione", method = RequestMethod.GET)
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public ResponseEntity<Iter> TestFascicolazione() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException {
        // 213/2018  hy
        // PG 0000459  2017
        // f.gusella
        
        GdDoc g = new GdDoc(null, null, null, null, null, null, null, "PG", null, "0000459", null, null, null, null, null, null, null, 2017);
        Fascicolazione f = new Fascicolazione("213/2018", "hy", "f.gusella", null, DateTime.now(), Document.DocumentOperationType.INSERT);
        ArrayList a = new ArrayList();
        a.add(f);
        g.setFascicolazioni(a);
        
        IodaRequestDescriptor ird = new IodaRequestDescriptor("gipi", "gipi", g);
        String baseUrl = "http://localhost:8084/bds_tools/ioda/api/document/update";             // Questo va spostato e reso parametrico
        //String baseUrl = getBaseUrl(iterParams.getIdAzienda()) + baseUrlBds;
        
        OkHttpClient client = new OkHttpClient();
        //okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, ird.getJSONString().getBytes("UTF-8"));
        
//        private final String REQUEST_DESCRIPTOR_PART_NAME = "request_descriptor";
// entityBuilder.addTextBody(REQUEST_DESCRIPTOR_PART_NAME, requestData.toJSONString(), ContentType.APPLICATION_JSON);

        RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("request_descriptor", null, okhttp3.RequestBody.create(JSON, ird.getJSONString().getBytes("UTF-8")))
                    .build();

        
        Request request = new Request.Builder()
                .url(baseUrl)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        String resString = response.body().string();
        //g = (GdDoc) it.bologna.ausl.ioda.iodaobjectlibrary.Requestable.parse(resString, GdDoc.class);
        
        JsonObject o = new JsonObject();
        o.addProperty("gddoc", "ciao");

        return new ResponseEntity(o.toString(), HttpStatus.OK);
    }

    
    @RequestMapping(value = "getNextFase", method = RequestMethod.GET)
    public ResponseEntity getNextFase() {
        Iter iter = new Iter();
        iter.setId(6);
        Fase fase = new Fase();
        fase.setId(2);
        iter.setIdFaseCorrente(fase);
        process.getNextFase(iter);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "stepOn", method = RequestMethod.GET)
    public ResponseEntity stepOn() throws ParseException, GipiRequestParamsException {

        Iter iter = new Iter();
        iter.setId(6);
        Fase fase = new Fase();
        fase.setId(2);
        iter.setIdFaseCorrente(fase);

        process.stepOn(iter, null, null);
        return new ResponseEntity(HttpStatus.OK);
    }

//    @JsonView(View.Summary.class)
    @RequestMapping(value = "dammi", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"})
    public ResponseEntity dammi() throws ParseException, GipiRequestParamsException {
//        List<Procedimento> p1 = em.createQuery(
//                "select p "
//                + "from Procedimento p "
//                + "where p.id = :idProcedimento", Procedimento.class)
//                .setParameter("idProcedimento", 580)
//                .getResultList();
        EntityGraph<Azienda> aziendaEntityGraph = em.createEntityGraph(Azienda.class);
        aziendaEntityGraph.addAttributeNodes("pecList");

//        QProcedimento qProcedimento = QProcedimento.procedimento;
        JPQLQuery<Azienda> query = new JPAQuery(em);

//        Procedimento p = query
//                .from(qProcedimento)
//                .where(qProcedimento.id.eq(8))
//                .fetchOne();
        QAzienda qAzienda = QAzienda.azienda;
        QPec qPec = QPec.pec;

        List<Azienda> aziende = em.createQuery(
                "select a "
                + "from Azienda a "
                + "where a.id = :idAzienda", Azienda.class)
                //.setHint("javax.persistence.fetchgraph", aziendaEntityGraph)
                .setParameter("idAzienda", 5)
                .getResultList();

//        Azienda azienda = query.select(qAzienda)
//                .from(qAzienda)
//                .where(qAzienda.id.eq(5)).fetchFirst();
//        List<Pec> pecList = azienda.getPecList();
//        pecList.stream().forEach(p -> {
//            System.out.println(p.getIndirizzo());
//        });
//        azienda.setPecList(pecList);
//        Azienda azienda = em.createQuery(
//                "select a "
//                + "from Azienda a "
//                + "where a.id = :idAzienda", Azienda.class)
//                .setParameter("idAzienda", 2)
//                .getSingleResult();
//        try (Connection con = sql2o.open()) {
//            List<Procedimento> p = con.createQuery(queryStruttureText)
//                    .addParameter("idProcedimento", 580)
//                    .addColumnMapping("id_struttura", "idStruttura")
//                    .executeAndFetch(Procedimento.class);
//        }
//        return new ResponseEntity(aziende.get(0), HttpStatus.OK);
        Azienda azienda = query.select(qAzienda)
                .from(qAzienda)
                .where(qAzienda.id.eq(2))
                .fetchFirst();

//        List<Pec> pec = query.select(qPec)
//                .from(qPec)
//                .where(qPec.idAzienda.id.eq(2))
//                .fetch();
        int lista = azienda.getPecList().size();
        //azienda.getPecList().stream().forEach(p -> {System.out.println("p.id: " + p.getId() + "\n" + "p.indirizzo: " + p.getIndirizzo());});

        return new ResponseEntity(azienda, HttpStatus.OK);
    }

}
