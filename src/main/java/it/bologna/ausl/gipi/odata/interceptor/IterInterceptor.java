package it.bologna.ausl.gipi.odata.interceptor;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import it.bologna.ausl.entities.baborg.Ruolo;
import it.bologna.ausl.entities.cache.cachableobject.RuoloCachable;
import it.bologna.ausl.entities.cache.cachableobject.UtenteCachable;
import it.bologna.ausl.entities.gipi.Iter;
import it.bologna.ausl.entities.gipi.QIter;
import it.nextsw.olingo.interceptor.OlingoInterceptorOperation;
import it.nextsw.olingo.interceptor.bean.BinaryGrantExpansionValue;
import it.nextsw.olingo.interceptor.bean.OlingoQueryObject;
import it.nextsw.olingo.interceptor.exception.OlingoRequestRollbackException;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 *
 * @author gus
 */
@Component
public class IterInterceptor extends OlingoRequestInterceptorBase {

    private UtenteCachable geUtenteConnesso () {
        Authentication authentication = super.getAuthentication();
        UtenteCachable userInfo = (UtenteCachable) authentication.getPrincipal();
        return userInfo;
    }
    
    @Override
    public Predicate onQueryInterceptor(OlingoQueryObject olingoQueryObject) {
        Predicate p = Expressions.FALSE;
        UtenteCachable utente = geUtenteConnesso();
        List<RuoloCachable> ruoli = utente.getRuoliUtente();
        if ( // se sono CI, AS o SD  non applico nessun filtro perché devo vedere tutti gli iter
                ruoli.stream().anyMatch(ruolo -> ( ruolo.getNomeBreve() == Ruolo.CodiciRuolo.CI)) ||
                ruoli.stream().anyMatch(ruolo -> ( ruolo.getNomeBreve() == Ruolo.CodiciRuolo.AS)) ||
                ruoli.stream().anyMatch(ruolo -> ( ruolo.getNomeBreve() == Ruolo.CodiciRuolo.SD))
            ) {
            p = null;
        } else {
            p = QIter.iter.idProcedimento.idAziendaTipoProcedimento.idAzienda.id.in(utente.getIdAziende());
        }
        
        return p;
    }

    @Override
    public Object onChangeInterceptor(OlingoInterceptorOperation olingoInterceptorOperation, Object object, EntityManager entityManager, Map<String, Object> contextAdditionalData) throws OlingoRequestRollbackException {
            return object;
    }

    @Override
    public void onDeleteInterceptor(Object object, EntityManager entityManager, Map<String, Object> contextAdditionalData) throws OlingoRequestRollbackException {

    }
    
    @Override
    public Class<?> getReferenceEntity() {
        return Iter.class;
    }

    @Override
    public void onGrantExpandsAuthorization(List<BinaryGrantExpansionValue> binaryGrantExpansionValues) {
    }

}
