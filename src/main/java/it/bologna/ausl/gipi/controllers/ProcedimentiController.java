package it.bologna.ausl.gipi.controllers;

import com.google.gson.JsonObject;
import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.baborg.Struttura;
import it.bologna.ausl.entities.gipi.AziendaTipoProcedimento;
import it.bologna.ausl.entities.gipi.Procedimento;
import it.bologna.ausl.entities.gipi.QProcedimento;
import it.bologna.ausl.gipi.exceptions.GipiDatabaseException;
import it.bologna.ausl.gipi.exceptions.GipiRequestParamsException;
import it.bologna.ausl.gipi.odata.complextypes.StrutturaCheckTipoProcedimento;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

@RestController
@RequestMapping("/gipi/resources/custom/")
@PropertySource("classpath:query.properties")
public class ProcedimentiController {

    @Autowired
    private Sql2o sql2o;

    @Autowired
    private EntityManager em;

    @Value("${functionimports.query-strutture-con-check}")
    private String queryStruttureText;

    @RequestMapping(value = "updateProcedimenti", method = RequestMethod.POST)
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public ResponseEntity updateProcedimenti(
            @RequestBody UpdateProcedimentiParams data
    ) throws GipiRequestParamsException, GipiDatabaseException {

        Map<Integer, UpdateProcedimentiParams.Operations> nodeInvolved = data.getNodeInvolved();
        Integer idAziendaTipoProcedimento = data.getIdAziendaTipoProcedimento();

        if (nodeInvolved != null && !nodeInvolved.isEmpty()) {
            //em.getTransaction().begin();

            for (Map.Entry<Integer, UpdateProcedimentiParams.Operations> entry : nodeInvolved.entrySet()) {
                Integer idStruttura = entry.getKey();
                UpdateProcedimentiParams.Operations operation = entry.getValue();

                if (null == operation) {
                    throw new GipiRequestParamsException("Tipo di operazione non prevista");
                } else {
                    switch (operation) {
                        case INSERT:
                            Procedimento p = new Procedimento();
                            p.setDataInizio(new Date());
                            p.setIdAziendaTipoProcedimento(new AziendaTipoProcedimento(idAziendaTipoProcedimento));
                            p.setIdStruttura(new Struttura(idStruttura));
                            em.persist(p);
                            break;
                        case DELETE:
                            JPQLQuery<Procedimento> query = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
                            List<Procedimento> result = query.from(QProcedimento.procedimento)
                                    .where(QProcedimento.procedimento.idAziendaTipoProcedimento.id.eq(idAziendaTipoProcedimento)
                                            .and(QProcedimento.procedimento.idStruttura.id.eq(idStruttura))).fetch();
                            if (result != null && !result.isEmpty()) {
                                em.remove(result.get(0));
                            } else {
                                throw new GipiDatabaseException("procedimento da rimuovere non trovato, questo non dovrebbe capitare!");
                            }
                            break;
                        default:
                            throw new GipiRequestParamsException("Tipo di operazione non prevista");
                    }
                }
            }

        }
        //RITORNIAMO UN OGGETTO CHE IN REALTA' E' VUOTO PERCHE ALTRIMENTI LATO CLIENT PERCHE' LA SUBSCRIBE SI ASPETTA UN OGGETTO (ANCHE VUOTO) IN CASO
        //DI ESITO POSITIVO DELL'OPERAZIONE
        return new ResponseEntity(new ArrayList<Object>(), HttpStatus.OK);
        // oppure  em.remove(employee);
//        em.getTransaction().commit();
    }

//    @RequestMapping(value = "view", method = RequestMethod.GET)
//
//    public void view(HttpServletRequest request) {
//        System.out.println("abc");
//    }
}
