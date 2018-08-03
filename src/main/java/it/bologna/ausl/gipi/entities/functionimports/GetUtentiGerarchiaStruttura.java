package it.bologna.ausl.gipi.entities.functionimports;

import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.baborg.QUtente;
import it.bologna.ausl.entities.baborg.QUtenteStruttura;
import it.bologna.ausl.entities.cache.cachableobject.UtenteCachable;
import it.nextsw.olingo.edmextension.EdmFunctionImportClassBase;
import it.nextsw.olingo.edmextension.annotation.EdmFunctionImportClass;
import java.io.IOException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.olingo.odata2.api.annotation.edm.EdmFacets;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImportParameter;
import org.apache.olingo.odata2.jpa.processor.core.access.data.JPAQueryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 *
 * @author Gus
 */
@EdmFunctionImportClass
@Component
public class GetUtentiGerarchiaStruttura extends EdmFunctionImportClassBase {

    private static final Logger log = LoggerFactory.getLogger(GetUtentiGerarchiaStruttura.class);

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private CacheableFunctions ca;

    /**
     * Dato un idStruttura ritorno tutti gli utentiStruttura appartenenti alla
     * medesima od alle strutture figlie di essa
     *
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
            @EdmFunctionImportParameter(name = "searchString", facets = @EdmFacets(nullable = true)) final String searchString,
            @EdmFunctionImportParameter(name = "idStruttura", facets = @EdmFacets(nullable = true)) final Integer idStruttura
    ) throws IOException {
        
        log.info("Chiamata della function import getUtentiGerarchiaStruttura");
        log.info("Stringa di ricerca: " + searchString);
        log.info("Stringa di ricerca: " + idStruttura);
        List<Integer> lista;
        if (idStruttura == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UtenteCachable userInfo = (UtenteCachable) authentication.getPrincipal();
            int idUtente = (int) userInfo.get(UtenteCachable.KEYS.ID);
            lista = ca.getMieStruttureEcugine(idUtente);
        } else {
            lista = ca.getStruttureFiglieEcugine(idStruttura);
        }
        // Ora creo la query che recupera gli utenti in base alla lista di strutture appena creata
        JPAQuery queryDSL = new JPAQuery(em);
        queryDSL.select(QUtenteStruttura.utenteStruttura)
                .from(QUtenteStruttura.utenteStruttura)
                .join(QUtente.utente).on(QUtenteStruttura.utenteStruttura.idUtente.eq(QUtente.utente))
                .where(QUtenteStruttura.utenteStruttura.idStruttura.id.in(lista))
                .orderBy(QUtenteStruttura.utenteStruttura.idStruttura.id.asc());

        if (searchString != null && !searchString.equals("")) {
            queryDSL.where(QUtente.utente.idPersona.descrizione.likeIgnoreCase("%" + searchString + "%")
                    .or(QUtente.utente.idPersona.nome.likeIgnoreCase(searchString).or(QUtente.utente.idPersona.cognome.likeIgnoreCase(searchString))));
        }

        return createQueryInfo(queryDSL, QUtenteStruttura.utenteStruttura.id.count(), em);
    }
}
