/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.gipi.entities.functionimports;

import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.baborg.QUtente;
import it.bologna.ausl.entities.baborg.QUtenteStruttura;
import it.nextsw.olingo.edmextension.EdmFunctionImportClassBase;
import it.nextsw.olingo.edmextension.annotation.EdmFunctionImportClass;
import java.io.IOException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.log4j.Logger;
import org.apache.olingo.odata2.api.annotation.edm.EdmFacets;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImportParameter;
import org.apache.olingo.odata2.jpa.processor.core.access.data.JPAQueryInfo;
import org.springframework.stereotype.Component;
/**
 *
 * @author Gus
 */
@EdmFunctionImportClass
@Component
public class GetUtentiGerarchiaStruttura extends EdmFunctionImportClassBase {
    
    private static final Logger logger = Logger.getLogger(GetIterUtente.class);
    
    @PersistenceContext
    private EntityManager em;
    
    /**
     * Dato un idStruttura ritorno tutti gli utentiStruttura appartenenti alla medesima od alle strutture figlie di essa
     * @param idStruttura
     * @param searchString
     * @return
     * @throws IOException 
     */
    @EdmFunctionImport(
            name = "GetUtentiGerarchiaStruttura",
            entitySet = "UtenteStrutturas",
            returnType = @EdmFunctionImport.ReturnType(
                    type = EdmFunctionImport.ReturnType.Type.ENTITY, 
                    formatResult = EdmFunctionImport.FormatResult.PAGINATED_COLLECTION, 
                    EdmEntityTypeName = "UtenteStruttura"),
            httpMethod = EdmFunctionImport.HttpMethod.GET
    )
    public JPAQueryInfo getUtentiGerarchiaStruttura(
            @EdmFunctionImportParameter(name = "idStruttura", facets = @EdmFacets(nullable = false)) final Integer idStruttura,
            @EdmFunctionImportParameter(name = "searchString", facets = @EdmFacets(nullable = false)) final String searchString
    ) throws IOException {
        logger.info("sono in getUtentiGerarchiaStruttura, idStruttura: " + idStruttura);
        logger.info("Stringa di ricerca: " + searchString);
        
        // Recupero la lista delle strutture figlie/nipoti etc della mia struttura
        String query = "select * from organigramma.get_strutture_figlie(?);";
        Query query1 = em.createNativeQuery(query);
        query1.setParameter(1, idStruttura);
        List<Integer> lista = query1.getResultList();
        lista.add(idStruttura);
            
        // Ora creo la query che recupera gli utenti in base alla lista di strutture appena creata
        JPAQuery queryDSL = new JPAQuery(em);
        queryDSL.select(QUtenteStruttura.utenteStruttura)
                .from(QUtenteStruttura.utenteStruttura)
                .join(QUtente.utente).on(QUtenteStruttura.utenteStruttura.idUtente.eq(QUtente.utente))
                .where(QUtenteStruttura.utenteStruttura.idStruttura.id.in(lista));

        if (searchString != null && !searchString.equals("")) {
            queryDSL.where(QUtente.utente.idPersona.descrizione.likeIgnoreCase("%"+searchString+"%"));
        }
        
        return createQueryInfo(queryDSL, QUtenteStruttura.utenteStruttura.id.count(), em);
    }
}