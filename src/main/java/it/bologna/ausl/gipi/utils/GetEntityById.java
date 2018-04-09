/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.utils;

import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.baborg.QAzienda;
import it.bologna.ausl.entities.baborg.QPersona;
import it.bologna.ausl.entities.baborg.QUtente;
import it.bologna.ausl.entities.baborg.Utente;
import it.bologna.ausl.entities.gipi.AziendaTipoProcedimento;
import it.bologna.ausl.entities.gipi.Evento;
import it.bologna.ausl.entities.gipi.Fase;
import it.bologna.ausl.entities.gipi.FaseIter;
import it.bologna.ausl.entities.gipi.Iter;
import it.bologna.ausl.entities.gipi.Procedimento;
import it.bologna.ausl.entities.gipi.QAziendaTipoProcedimento;
import it.bologna.ausl.entities.gipi.QEvento;
import it.bologna.ausl.entities.gipi.QFase;
import it.bologna.ausl.entities.gipi.QFaseIter;
import it.bologna.ausl.entities.gipi.QIter;
import it.bologna.ausl.entities.gipi.QProcedimento;
import it.bologna.ausl.entities.gipi.QStato;
import it.bologna.ausl.entities.gipi.Stato;
import javax.persistence.EntityManager;

/**
 *
 * @author f.gusella
 */
public class GetEntityById {

    /* SCHEMA ORGANIGRAMMA */
    public static Utente getUtente(int idUtente, EntityManager em) {
        QUtente qUtente = QUtente.utente;
        JPQLQuery<Utente> query = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);
        Utente u = query
                .from(qUtente)
                .where(qUtente.id.eq(idUtente))
                .fetchOne();
        return u;
    }
    
    public static Utente getUtenteFromPersonaByCodiceFiscaleAndIdAzineda(String codiceFiscale, int idAzienda, EntityManager em) {
        QUtente qUtente = QUtente.utente;
        JPQLQuery<Utente> query = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);
        Utente u = query
                .from(qUtente)
                .where(qUtente.idPersona.codiceFiscale.eq(codiceFiscale)
                    .and(qUtente.idAzienda.id.eq(idAzienda)))
                .fetchOne();
        return u;
    }

    /* SCHEMA GIPI */
    public static Procedimento getProcedimento(int idProcedimento, EntityManager em) {
        QProcedimento qProcedimento = QProcedimento.procedimento;
        JPQLQuery<Procedimento> query = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);
        Procedimento p = query
                .from(qProcedimento)
                .where(qProcedimento.id.eq(idProcedimento))
                .fetchOne();
        return p;
    }

    public static AziendaTipoProcedimento getAziendaTipoProcedimento(int idAziendaTipoProcedimento, EntityManager em) {
        QAziendaTipoProcedimento qAziendaTipoProcedimento = QAziendaTipoProcedimento.aziendaTipoProcedimento;
        JPQLQuery<AziendaTipoProcedimento> query = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);
        AziendaTipoProcedimento a = query
                .from(qAziendaTipoProcedimento)
                .where(qAziendaTipoProcedimento.id.eq(idAziendaTipoProcedimento))
                .fetchFirst();
        return a;
    }
    
    
    public static Iter getIter(int idIter, EntityManager em) {
        QIter qIter = QIter.iter;
        JPQLQuery<Iter> query = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);
        Iter i = query
                .from(qIter)
                .where(qIter.id.eq(idIter))
                .fetchFirst();
        return i;
    }
    
    public static Fase getFase(int idFase, EntityManager em) {
        QFase qFase = QFase.fase;
        JPQLQuery<Fase> query = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);
        Fase f = query
                .from(qFase)
                .where(qFase.id.eq(idFase))
                .fetchFirst();
        return f;
    }
    
    public static FaseIter getFaseIter(int idFaseIter, EntityManager em) {
        QFaseIter qFaseIter = QFaseIter.faseIter;
        JPQLQuery<FaseIter> query = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);
        FaseIter fi = query
                .from(qFaseIter)
                .where(qFaseIter.id.eq(idFaseIter))
                .fetchFirst();
        return fi;
    }
    
    public static Evento getEventoByCodice(String codice, EntityManager em) {
        QEvento qEvento = QEvento.evento;
        JPQLQuery<Evento> query = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);
        Evento e = query
                .from(qEvento)
                .where(qEvento.codice.eq(codice))
                .fetchOne();
        return e;
    }
    
    public static Stato getStatoByCodice(String codice, EntityManager em) {
        QStato qStato = QStato.stato;
        JPQLQuery<Stato> query = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);
        Stato s = query
                .from(qStato)
                .where(qStato.codice.eq(codice))
                .fetchOne();
        return s;
    }
    
    public static Stato getStatoById(int id, EntityManager em) {
        QStato qStato = QStato.stato;
        JPQLQuery<Stato> query = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);
        Stato s = query
                .from(qStato)
                .where(qStato.id.eq(id))
                .fetchOne();
        return s;
    }
}
