package it.bologna.ausl.gipi.controllers;

import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.baborg.Struttura;
import it.bologna.ausl.entities.gipi.AziendaTipoProcedimento;
import it.bologna.ausl.entities.gipi.Procedimento;
import it.bologna.ausl.entities.gipi.QProcedimento;
import it.bologna.ausl.entities.utilities.response.exceptions.ConflictResponseException;
import it.bologna.ausl.entities.utilities.response.controller.ControllerHandledExceptions;
import it.bologna.ausl.gipi.exceptions.GipiDatabaseException;
import it.bologna.ausl.gipi.exceptions.GipiRequestParamsException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.PersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.sql2o.Sql2o;

@RestController
@RequestMapping(value = "${custom.mapping.url.root}")
@PropertySource("classpath:query.properties")
public class ProcedimentiController extends ControllerHandledExceptions {

    private final Integer NO_DELETE_ERROR_CODE = 1;

    @Autowired
    private Sql2o sql2o;

    @Autowired
    private EntityManager em;

    @Value("${functionimports.query-strutture-con-check}")
    private String queryStruttureText;

    @RequestMapping(value = "UpdateProcedimenti", method = RequestMethod.POST)
    @Transactional(rollbackFor = {Exception.class, Error.class, ConflictResponseException.class})
    public ResponseEntity updateProcedimenti(
            @RequestBody UpdateProcedimentiParams data
    ) throws GipiRequestParamsException, GipiDatabaseException, ConflictResponseException {

        Map<Integer, UpdateProcedimentiParams.Operations> nodeInvolved = data.getNodeInvolved();
        Integer idAziendaTipoProcedimento = data.getIdAziendaTipoProcedimento();

        if (nodeInvolved != null && !nodeInvolved.isEmpty()) {
            //em.getTransaction().begin();

            try {
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
                
                // flush per fare in modo che, in caso di errore venga lanciata l'eccezione e possa essere gestita per capire il tipo di errore
                // voglio beccare l'errore generato dal fatto che sta cercando di eliminare un procedimento con degli iter attaccati
                em.flush();
            } catch (PersistenceException ex) {
                // controllo il tipo di errore
                if (ex.getCause() != null
                        && ex.getCause().getCause() != null
                        && SQLException.class.isAssignableFrom(ex.getCause().getCause().getClass())) {
                    String sqlState = ((SQLException) ex.getCause().getCause()).getSQLState();
                    // se l'errore inizia per "23", vuol dire che c'è una violazione di foreign key, quindi quasi sicuramente è il caso che ci interessa
                    if (sqlState.startsWith("23")) {
                        // lancio l'eccezione che tornerà lo status-code Conflict(409), con codice errore NO_DELETE_ERROR_CODE (1)
                        throw new ConflictResponseException(NO_DELETE_ERROR_CODE, "impossibile eliminare perché ha già degli iter", ex.getMessage());
                    }
                }
            }
        }

        //RITORNIAMO UN OGGETTO CHE IN REALTA' E' VUOTO PERCHE ALTRIMENTI LATO CLIENT PERCHE' LA SUBSCRIBE SI ASPETTA UN OGGETTO (ANCHE VUOTO) IN CASO
        //DI ESITO POSITIVO DELL'OPERAZIONE
        return new ResponseEntity(new ArrayList<>(), HttpStatus.OK);
        // oppure  em.remove(employee);
//        em.getTransaction().commit();
    }
    @RequestMapping(value = "espandiProcedimenti", method = RequestMethod.POST)
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public ResponseEntity espandiProcedimenti(
            @RequestBody Integer idProcedimentoPassato
    ) throws GipiRequestParamsException, GipiDatabaseException {

        System.out.println(idProcedimentoPassato);

        // carico il procedimento
        JPQLQuery<Procedimento> queryProcedimento = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
        Procedimento procedimentoPassato = queryProcedimento.from(QProcedimento.procedimento)
                .where(QProcedimento.procedimento.id.eq(idProcedimentoPassato)).fetchOne();
        if (procedimentoPassato == null) {
            throw new GipiDatabaseException("procedimento non trovato");
        }

        String queryString = "select * from organigramma.get_strutture_figlie(?);";
        Query queryStruttureFiglie = em.createNativeQuery(queryString);
        queryStruttureFiglie.setParameter(1, procedimentoPassato.getIdStruttura().getId());
        List<Integer> struttureFiglie = queryStruttureFiglie.getResultList();
        // aggiungo anche il procedimento corrente, salvo tutto assieme
        // Anzi no, il procedimento radice l'ho gia salvato quando arrivo qua (problemi a passarmi l'entità dal client)
        //struttureFiglie.add(procedimento.getIdStruttura().getId());

        // mi ricarico i procedimenti gia associati a quella struttura
        JPAQuery<Procedimento> queryProcedimentiEsistenti = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
        List<Procedimento> procedimentiEsistenti = queryProcedimentiEsistenti.from(QProcedimento.procedimento)
                .where(QProcedimento.procedimento.idAziendaTipoProcedimento.id.eq(procedimentoPassato.getIdAziendaTipoProcedimento().getId())
                        .and(QProcedimento.procedimento.idStruttura.id.in(struttureFiglie))).fetch();

        List<Integer> giaMergiati = new ArrayList<>();

        // update sui procedimenti gia esistenti
        for (Procedimento procedimentoEsistente : procedimentiEsistenti) {
            System.out.println(procedimentoEsistente);
            procedimentoEsistente.setIdTitolarePotereSostitutivo(procedimentoPassato.getIdTitolarePotereSostitutivo());
            procedimentoEsistente.setDataInizio(procedimentoPassato.getDataInizio());
            procedimentoEsistente.setDataFine(procedimentoPassato.getDataFine());
            procedimentoEsistente.setUfficio(procedimentoPassato.getUfficio());
            procedimentoEsistente.setModalitaInfo(procedimentoPassato.getModalitaInfo());
            procedimentoEsistente.setIdAziendaTipoProcedimento(procedimentoPassato.getIdAziendaTipoProcedimento());
            procedimentoEsistente.setIterList(procedimentoPassato.getIterList());
            procedimentoEsistente.setIdResponsabileAdozioneAttoFinale(procedimentoPassato.getIdResponsabileAdozioneAttoFinale());
            procedimentoEsistente.setIdStrutturaTitolarePotereSostitutivo(procedimentoPassato.getIdStrutturaTitolarePotereSostitutivo());
            procedimentoEsistente.setIdStrutturaResponsabileAdozioneAttoFinale(procedimentoPassato.getIdStrutturaResponsabileAdozioneAttoFinale());
            procedimentoEsistente.setStrumenti(procedimentoPassato.getStrumenti());
            procedimentoEsistente.setDescrizioneAtti(procedimentoPassato.getDescrizioneAtti());

            em.merge(procedimentoEsistente);
            giaMergiati.add(procedimentoEsistente.getIdStruttura().getId());
        }

        // inserimento di procedimenti nuovi
        for (int idStruttura : struttureFiglie) {
            // escludo quelli su cui ho fatto l'update
            if (giaMergiati.contains(idStruttura)) {
                continue;
            }
            Struttura struttura = new Struttura();
            struttura.setId(idStruttura);

            Procedimento procedimentoToInsert
                    = new Procedimento(null,
                            procedimentoPassato.getIdTitolarePotereSostitutivo(),
                            struttura,
                            procedimentoPassato.getDataInizio(),
                            procedimentoPassato.getDataFine(),
                            procedimentoPassato.getUfficio(),
                            procedimentoPassato.getModalitaInfo(),
                            procedimentoPassato.getIdAziendaTipoProcedimento(),
                            procedimentoPassato.getIterList(),
                            procedimentoPassato.getIdResponsabileAdozioneAttoFinale(),
                            procedimentoPassato.getIdStrutturaTitolarePotereSostitutivo(),
                            procedimentoPassato.getIdStrutturaResponsabileAdozioneAttoFinale(),
                            procedimentoPassato.getStrumenti(),
                            procedimentoPassato.getDescrizioneAtti()
                    );

            em.persist(procedimentoToInsert);

        }

        //RITORNIAMO UN OGGETTO CHE IN REALTA' E' VUOTO PERCHE LATO CLIENT LA SUBSCRIBE SI ASPETTA UN OGGETTO (ANCHE VUOTO) IN CASO
        //DI ESITO POSITIVO DELL'OPERAZIONE
        return new ResponseEntity(new ArrayList<Object>(), HttpStatus.OK);

    }

}
