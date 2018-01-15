/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.baborg.Azienda;
import it.bologna.ausl.entities.baborg.Pec;
import it.bologna.ausl.entities.baborg.QAzienda;
import it.bologna.ausl.entities.baborg.QPec;
import it.bologna.ausl.entities.baborg.View;
import it.bologna.ausl.entities.gipi.Fase;
import it.bologna.ausl.entities.gipi.Iter;
import it.bologna.ausl.entities.gipi.Procedimento;
import it.bologna.ausl.entities.gipi.QProcedimento;
import it.bologna.ausl.gipi.exceptions.GipiRequestParamsException;
import it.bologna.ausl.gipi.odata.complextypes.StrutturaCheckTipoProcedimento;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import it.bologna.ausl.gipi.process.Process;
import java.text.ParseException;
import java.util.List;
import java.util.Properties;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

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

    @JsonView(View.Summary.class)
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
        return new ResponseEntity(aziende.get(0), HttpStatus.OK);
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
