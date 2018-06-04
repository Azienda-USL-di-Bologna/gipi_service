package it.bologna.ausl.gipi.entities.functionimports;

import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.cache.cachableobject.AziendaCachable;
import it.bologna.ausl.entities.cache.cachableobject.UtenteCachable;
import it.bologna.ausl.entities.gipi.Procedimento;
import it.bologna.ausl.entities.gipi.QProcedimento;
import it.bologna.ausl.entities.utilities.FunctionImportSorting;
import it.nextsw.olingo.edmextension.EdmFunctionImportClassBase;
import it.nextsw.olingo.edmextension.annotation.EdmFunctionImportClass;
import java.io.IOException;
import java.util.Date;
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
import org.springframework.util.StringUtils;

/**
 * Dato un idUtente restituisco tutti i tipi di procedimento delle Strutture di afferenza, dirette e funzionali,
 * e delle strutture cugine e di tutti i loro figli
 *
 * @param idUtente
 * @return
 * @throws IOException
 *
 * @author Giuseppe Russo <g.russo@nsi.it>
 */

@EdmFunctionImportClass
@Component
public class GetTipiProcedimento extends EdmFunctionImportClassBase implements FunctionImportSorting{
    
    private static final Logger log = LoggerFactory.getLogger(GetTipiProcedimento.class);

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private CacheableFunctions ca;
    
    @EdmFunctionImport(
            name = "GetTipiProcedimento",
            entitySet = "Procedimentos",
            returnType = @EdmFunctionImport.ReturnType(
                    type = EdmFunctionImport.ReturnType.Type.ENTITY,
                    formatResult = EdmFunctionImport.FormatResult.PAGINATED_COLLECTION,
                    EdmEntityTypeName = "Procedimento"),
            httpMethod = EdmFunctionImport.HttpMethod.GET
    )
    public JPAQueryInfo getTipiProcedimento(
        @EdmFunctionImportParameter(name = "ufficio", facets = @EdmFacets(nullable = true)) final String ufficio,
        @EdmFunctionImportParameter(name = "idStruttura_sep_nome", facets = @EdmFacets(nullable = true)) final String nomeStruttura,
        @EdmFunctionImportParameter(name = "idAziendaTipoProcedimento_sep_idTipoProcedimento_sep_nome", facets = @EdmFacets(nullable = true)) final String nomeProcedimento,
        @EdmFunctionImportParameter(name = "idTitolarePotereSostitutivo_sep_idPersona_sep_descrizione", facets = @EdmFacets(nullable = true)) final String titolarePotereSostitutivo,
        @EdmFunctionImportParameter(name = "sort", facets = @EdmFacets(nullable = true)) final String sort
        ) throws IOException {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UtenteCachable userInfo = (UtenteCachable) authentication.getPrincipal();
        int idUtente = (int) userInfo.get(UtenteCachable.KEYS.ID);
        AziendaCachable aziendaInfo = (AziendaCachable) userInfo.get(UtenteCachable.KEYS.AZIENDA_LOGIN);
        int idAzienda = (int) aziendaInfo.get(AziendaCachable.KEYS.ID);
        
        List<Integer> listaStrutture = ca.getMieStruttureEcugine(idUtente);
        
        Date now = new Date();
        JPAQuery queryDSL = new JPAQuery(em);
        queryDSL.select(QProcedimento.procedimento)
                .from(QProcedimento.procedimento)
                .where(QProcedimento.procedimento.idStruttura.id.in(listaStrutture)
                .and(QProcedimento.procedimento.completo.eq(Boolean.TRUE))
                .and(QProcedimento.procedimento.idAziendaTipoProcedimento.idAzienda.id.eq(idAzienda))
                .and(QProcedimento.procedimento.dataInizio.loe(now))
                .and((QProcedimento.procedimento.dataFine.goe(now)).or(QProcedimento.procedimento.dataFine.isNull()))
        );
        
        if (StringUtils.hasText(nomeStruttura)) {
            queryDSL.where(QProcedimento.procedimento.idStruttura.nome.likeIgnoreCase("%" + nomeStruttura + "%"));
        }
        
        if (StringUtils.hasText(nomeProcedimento)) {
            queryDSL.where(QProcedimento.procedimento.idAziendaTipoProcedimento.idTipoProcedimento.nome.likeIgnoreCase("%" + nomeProcedimento + "%"));
        }
        
        if (StringUtils.hasText(titolarePotereSostitutivo)) {
            queryDSL.where(QProcedimento.procedimento.idTitolarePotereSostitutivo.idPersona.descrizione.likeIgnoreCase("%" + titolarePotereSostitutivo + "%"));
        }
        
         if (sort != null && !sort.isEmpty()) {
            addSorting(queryDSL, sort, Procedimento.class);
        } else {
            queryDSL.orderBy(QProcedimento.procedimento.idStruttura.nome.asc(),
                    QProcedimento.procedimento.idAziendaTipoProcedimento.idTipoProcedimento.nome.asc());
         }
        
        return createQueryInfo(queryDSL, QProcedimento.procedimento.id.count(), em);
    }
    
}
