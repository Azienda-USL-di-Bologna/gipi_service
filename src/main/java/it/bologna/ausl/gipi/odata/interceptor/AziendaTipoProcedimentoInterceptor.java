package it.bologna.ausl.gipi.odata.interceptor;

import com.querydsl.core.types.Predicate;
import it.bologna.ausl.entities.gipi.AziendaTipoProcedimento;
import it.bologna.ausl.gipi.security.jwt.CustomUserDetailsService;
import it.nextsw.olingo.interceptor.OlingoInterceptorOperation;
import it.nextsw.olingo.interceptor.bean.BinaryGrantExpansionValue;
import it.nextsw.olingo.interceptor.bean.OlingoQueryObject;
import it.nextsw.olingo.interceptor.exception.OlingoRequestRollbackException;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 *
 * @author gdm
 */
@Configuration
public class AziendaTipoProcedimentoInterceptor extends OlingoRequestInterceptorBase {

    @Autowired
    private CustomUserDetailsService userDetails;
    
    @Override
    public Predicate onQueryInterceptor(OlingoQueryObject olingoQueryObject) {
        System.out.println("GDMGDMGDMGMDGMDGMDMGDMGM");
        return null;
    }

    @Override
    public Object onChangeInterceptor(OlingoInterceptorOperation olingoInterceptorOperation, Object object, EntityManager entityManager, Map<String, Object> contextAdditionalData) throws OlingoRequestRollbackException {
        org.springframework.web.context.request.ServletRequestAttributes attr = (
                org.springframework.web.context.request.ServletRequestAttributes) 
                org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes();
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        System.out.println("il nome è: " + currentPrincipalName);
        return null;
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
