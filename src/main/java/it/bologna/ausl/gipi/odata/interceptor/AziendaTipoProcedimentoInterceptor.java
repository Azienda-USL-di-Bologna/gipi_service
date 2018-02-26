package it.bologna.ausl.gipi.odata.interceptor;

import com.querydsl.core.types.Predicate;
import it.bologna.ausl.entities.baborg.Ruolo;
import it.bologna.ausl.entities.cache.cachableobject.RuoloCachable;
import it.bologna.ausl.entities.cache.cachableobject.UtenteCachable;
import it.bologna.ausl.entities.gipi.AziendaTipoProcedimento;
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
public class AziendaTipoProcedimentoInterceptor extends OlingoRequestInterceptorBase {

    @Override
    public Predicate onQueryInterceptor(OlingoQueryObject olingoQueryObject) {
        System.out.println("GDMGDMGDMGMDGMDGMDMGDMGM AH! PAPAPISHU!");
        return null;
    }

    @Override
    public Object onChangeInterceptor(OlingoInterceptorOperation olingoInterceptorOperation, Object object, EntityManager entityManager, Map<String, Object> contextAdditionalData) throws OlingoRequestRollbackException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UtenteCachable userInfo = (UtenteCachable) authentication.getPrincipal();
        
        List<RuoloCachable> ruoliCachable = userInfo.getRuoliUtente();

//        ruoliCachable.stream().forEach(a -> {System.out.println(a.getNomeBreve() + ": " + a.getNomeBreve().getClass().getName());});
        
        if (olingoInterceptorOperation == OlingoInterceptorOperation.CREATE && ruoliCachable.stream().anyMatch(
                ruolo -> ruolo.getNomeBreve() == Ruolo.CodiciRuolo.CI
        )) {
            return object;
        } else {
            throw new OlingoRequestRollbackException();
        }
    }

    @Override
    public void onDeleteInterceptor(Object object, EntityManager entityManager, Map<String, Object> contextAdditionalData) throws OlingoRequestRollbackException {

    }

    @Override
    public Class<?> getReferenceEntity() {
        return AziendaTipoProcedimento.class;
    }

    @Override
    public void onGrantExpandsAuthorization(List<BinaryGrantExpansionValue> binaryGrantExpansionValues) {
    }

}
