/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.baborg.Azienda;
import it.bologna.ausl.entities.baborg.AziendaParametriJson;
import it.bologna.ausl.entities.baborg.QAzienda;
import java.io.IOException;
import javax.persistence.EntityManager;

/**
 *
 * @author f.gusella
 */
public class GetBaseUrl {
    
    public static String getBaseUrl(int idAzienda, EntityManager em, ObjectMapper objectMapper) throws IOException {
        
        QAzienda qAzienda = QAzienda.azienda;
        JPQLQuery<Azienda> query = new JPAQuery(em, EclipseLinkTemplates.DEFAULT);
 
        String parametri = query.select(qAzienda.parametri)
                .from(qAzienda)
                .where(qAzienda.id.eq(idAzienda)).fetchFirst();
  
        AziendaParametriJson params = AziendaParametriJson.parse(objectMapper, parametri);
        String url = params.getBaseUrl();
       
        return url;
    }
}
