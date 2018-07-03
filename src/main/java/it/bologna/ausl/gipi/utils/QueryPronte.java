/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.utils;

import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.baborg.Azienda;
import it.bologna.ausl.entities.baborg.QAzienda;
import it.bologna.ausl.entities.gipi.EventoIter;
import it.bologna.ausl.entities.gipi.Iter;
import it.bologna.ausl.entities.gipi.QEventoIter;
import it.bologna.ausl.entities.gipi.QIter;
import it.bologna.ausl.entities.gipi.Stato;
import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Giuseppe Russo <g.russo@nsi.it>
 */
@Component
public class QueryPronte {
    
    @Autowired
    EntityManager em;
    
    QAzienda qAzienda = QAzienda.azienda;
    QIter qIter = QIter.iter;
    QEventoIter qEventoIter = QEventoIter.eventoIter;
    
    public List<Azienda> getListaAziende() {
        JPQLQuery<Azienda> query = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);
        return query.from(qAzienda).fetch();
    }
    
    public List<Iter> getIterInCorso() {
        JPQLQuery<Iter> queryIter = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
        return queryIter.from(qIter)
            .where(qIter.idStato.codice.eq(Stato.CodiciStato.IN_CORSO.toString()))
            .fetch();
    }
    
    public EventoIter getEventoInizialeIter(int idIter) {
        JPQLQuery<EventoIter> queryEventiIter = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
        return queryEventiIter
            .from(qEventoIter)
            .where(qEventoIter.idIter.id.eq(idIter).and(qEventoIter.idEvento.id.eq(1))) // 1 evento apertura
            .fetchOne();
    }
}
