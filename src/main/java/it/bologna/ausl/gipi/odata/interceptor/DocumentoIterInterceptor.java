package it.bologna.ausl.gipi.odata.interceptor;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import it.bologna.ausl.entities.baborg.Ruolo;
import it.bologna.ausl.entities.cache.cachableobject.RuoloCachable;
import it.bologna.ausl.entities.cache.cachableobject.UtenteCachable;
import it.bologna.ausl.entities.gipi.DocumentoIter;
import it.bologna.ausl.entities.gipi.Procedimento;
import it.bologna.ausl.entities.gipi.QProcedimento;
import it.nextsw.olingo.interceptor.OlingoInterceptorOperation;
import it.nextsw.olingo.interceptor.bean.BinaryGrantExpansionValue;
import it.nextsw.olingo.interceptor.bean.OlingoQueryObject;
import it.nextsw.olingo.interceptor.exception.OlingoRequestRollbackException;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 *
 * @author gdm
 */
@Component
public class DocumentoIterInterceptor extends OlingoRequestInterceptorBase {

    private UtenteCachable geUtenteConnesso () {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UtenteCachable userInfo = (UtenteCachable) authentication.getPrincipal();
        return userInfo;
    }
    
    @Override
    public Predicate onQueryInterceptor(OlingoQueryObject olingoQueryObject) {
        return null;
    }

    @Override
    public Object onChangeInterceptor(OlingoInterceptorOperation olingoInterceptorOperation, Object object, EntityManager entityManager, Map<String, Object> contextAdditionalData) throws OlingoRequestRollbackException {
        return object;
    }

    @Override
    public void onDeleteInterceptor(Object object, EntityManager entityManager, Map<String, Object> contextAdditionalData) throws OlingoRequestRollbackException {
        System.out.println("delete interceptor");
        DocumentoIter documentoIter = (DocumentoIter) object;
        if (!documentoIter.getParziale())
            throw new OlingoRequestRollbackException("si possono eliminare solo le associazioni parziali");
    }

    @Override
    public Class<?> getReferenceEntity() {
        return DocumentoIter.class;
    }

    @Override
    public void onGrantExpandsAuthorization(List<BinaryGrantExpansionValue> binaryGrantExpansionValues) {
    }

}
