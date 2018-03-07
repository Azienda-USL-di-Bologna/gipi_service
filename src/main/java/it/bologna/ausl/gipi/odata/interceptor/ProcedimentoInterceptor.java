package it.bologna.ausl.gipi.odata.interceptor;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import it.bologna.ausl.entities.baborg.Ruolo;
import it.bologna.ausl.entities.cache.cachableobject.RuoloCachable;
import it.bologna.ausl.entities.cache.cachableobject.UtenteCachable;
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
public class ProcedimentoInterceptor extends OlingoRequestInterceptorBase {

    private UtenteCachable geUtenteConnesso () {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UtenteCachable userInfo = (UtenteCachable) authentication.getPrincipal();
        return userInfo;
    }
    
    @Override
    public Predicate onQueryInterceptor(OlingoQueryObject olingoQueryObject) {
        Predicate p = Expressions.FALSE;
        UtenteCachable utente = geUtenteConnesso();
        List<RuoloCachable> ruoli = utente.getRuoliUtente();
        if ( // se sono CI non applico nessun filtro perché devo vedere tutti i procedimenti
                ruoli.stream().anyMatch(ruolo -> ( ruolo.getNomeBreve() == Ruolo.CodiciRuolo.CI))
            ) {
            p = null;
        }
        else if ( // se non sono CI, ma sono CA allora applico il filtro sulle mie anziende
                !ruoli.stream().anyMatch(ruolo -> ( ruolo.getNomeBreve() == Ruolo.CodiciRuolo.CI)) &&
                ruoli.stream().anyMatch(ruolo -> ( ruolo.getNomeBreve() == Ruolo.CodiciRuolo.CA))
            ) {
            p = QProcedimento.procedimento.idAziendaTipoProcedimento.idAzienda.id.in(utente.getIdAziende());
        }
        else if ( // se non sono né CI né CA, allora applico il filtro sulle mie strutture di afferenza (diretta o funzionale)
                !ruoli.stream().anyMatch(ruolo -> ( ruolo.getNomeBreve() == Ruolo.CodiciRuolo.CI)) &&
                !ruoli.stream().anyMatch(ruolo -> ( ruolo.getNomeBreve() == Ruolo.CodiciRuolo.CA)) &&
                ruoli.stream().anyMatch(
                        ruolo -> ( (ruolo.getNomeBreve() == Ruolo.CodiciRuolo.AS) ||
                                (ruolo.getNomeBreve() == Ruolo.CodiciRuolo.MOS) ||
                                (ruolo.getNomeBreve() == Ruolo.CodiciRuolo.OS) ||
                                (ruolo.getNomeBreve() == Ruolo.CodiciRuolo.SD) ||
                                (ruolo.getNomeBreve() == Ruolo.CodiciRuolo.UG)
                        ))
            ) {
            p = QProcedimento.procedimento.idStruttura.id.in(utente.getIdStrutture());
        }

        return p;
    }

    @Override
    public Object onChangeInterceptor(OlingoInterceptorOperation olingoInterceptorOperation, Object object, EntityManager entityManager, Map<String, Object> contextAdditionalData) throws OlingoRequestRollbackException {

        
//        ruoliCachable.stream().forEach(a -> {System.out.println(a.getNomeBreve() + ": " + a.getNomeBreve().getClass().getName());});
        
//        if (olingoInterceptorOperation == OlingoInterceptorOperation.CREATE && ruoliCachable.stream().anyMatch(
//                ruolo -> ruolo.getNomeBreve() == Ruolo.CodiciRuolo.CI
//        )) {
//            return object;
//        } else if(olingoInterceptorOperation == OlingoInterceptorOperation.UPDATE && ruoliCachable.stream().anyMatch(
//                ruolo -> ruolo.getNomeBreve() == Ruolo.CodiciRuolo.CA
//        )) {
            return object;
//        } else {
//            throw new OlingoRequestRollbackException();
//        }
    }

    @Override
    public void onDeleteInterceptor(Object object, EntityManager entityManager, Map<String, Object> contextAdditionalData) throws OlingoRequestRollbackException {

    }

    @Override
    public Class<?> getReferenceEntity() {
        return Procedimento.class;
    }

    @Override
    public void onGrantExpandsAuthorization(List<BinaryGrantExpansionValue> binaryGrantExpansionValues) {
    }

}
