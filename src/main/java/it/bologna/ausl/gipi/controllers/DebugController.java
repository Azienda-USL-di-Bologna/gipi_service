/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.controllers;

import com.google.gson.JsonObject;
import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.gipi.DocumentoIter;
import it.bologna.ausl.entities.gipi.EventoIter;
import it.bologna.ausl.entities.gipi.Iter;
import it.bologna.ausl.entities.gipi.QDocumentoIter;
import it.bologna.ausl.entities.gipi.QEventoIter;
import it.bologna.ausl.entities.gipi.QIter;
import it.bologna.ausl.entities.gipi.QRegistroTipoProcedimento;
import it.bologna.ausl.entities.gipi.RegistroTipoProcedimento;
import it.bologna.ausl.gipi.exceptions.GipiPubblicazioneException;
import it.bologna.ausl.gipi.utils.IterUtilities;
import java.io.IOException;
import java.util.List;
import javax.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Giuseppe Russo <g.russo@nsi.it>
 */
@RestController
@RequestMapping(value = "${custom.mapping.url.root}" + "/debug")
public class DebugController {
    private static final Logger log = LoggerFactory.getLogger(DebugController.class);
    
    @Autowired
    EntityManager em;
    
    @Autowired
    IterUtilities iterUtilities;
    
    QEventoIter qEventoIter = QEventoIter.eventoIter;
    QRegistroTipoProcedimento qRegistroTipoProcedimento = QRegistroTipoProcedimento.registroTipoProcedimento;
    QDocumentoIter qDocumentoIter = QDocumentoIter.documentoIter;
    QIter qIter = QIter.iter;
    
    @RequestMapping(value = "pubblicaIter/{idIter}", method = RequestMethod.GET)
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void pubblicaIter(@PathVariable int idIter) throws GipiPubblicazioneException, IOException {
        log.info(("ID ITER = " + idIter));
        JPQLQuery<EventoIter> queryEventiIter = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
        
        EventoIter evFinale = queryEventiIter
                .from(qEventoIter)
                .where(qEventoIter.idIter.id.eq(idIter).and(qEventoIter.idEvento.id.eq(2)))
                .fetchOne();
        
        JPQLQuery<DocumentoIter> queryDocumentoIter = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
        DocumentoIter doc = queryDocumentoIter
                .from(qDocumentoIter).where(qDocumentoIter.id.eq(evFinale.getIdDocumentoIter().getId())).fetchOne();
        
        
        JPQLQuery<Iter> queryIter = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
        Iter iter = queryIter
                .from(qIter).where(qIter.id.eq(idIter)).fetchOne();
        
        JPQLQuery<RegistroTipoProcedimento> query = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
        /* Controllo se l'iter deve essere pubblicato - Avrò un elemento
         * nella lista per ogni registro in cui dovrà essere pubblicato */
        log.info("Check pubblicazione iter...");
        List<RegistroTipoProcedimento> registriTipoProc = query
            .from(qRegistroTipoProcedimento)
            .where(qRegistroTipoProcedimento.idTipoProcedimento.id.eq(iter.getIdProcedimento()
                .getIdAziendaTipoProcedimento().getIdTipoProcedimento().getId()))
            .fetch();
        if (!registriTipoProc.isEmpty()){
            JsonObject pubblicazioni = iterUtilities.pubblicaIter(iter, doc, registriTipoProc);
            log.info("Stato pubblicazioni: " + pubblicazioni.toString());
        }        
    }    
}
