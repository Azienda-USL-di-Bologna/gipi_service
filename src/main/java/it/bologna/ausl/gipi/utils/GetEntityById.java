/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.utils;

import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.baborg.QUtente;
import it.bologna.ausl.entities.baborg.Utente;
import it.bologna.ausl.entities.gipi.AziendaTipoProcedimento;
import it.bologna.ausl.entities.gipi.Procedimento;
import it.bologna.ausl.entities.gipi.QAziendaTipoProcedimento;
import it.bologna.ausl.entities.gipi.QProcedimento;
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
}
