package it.bologna.ausl.gipi.entities.functionimports;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 *
 * @author f.gusella
 */
@Component
public class CacheableFunctions {

    @PersistenceContext
    private EntityManager em;

    @Cacheable(value = "functionImportCacheableDataGetGerarchiaStruttura", key = "{#idStruttura}")
    public List<Integer> getGerarchiaStruttura(Integer idStruttura) {
        // Recupero la lista delle strutture figlie/nipoti etc della mia struttura
        String query = "select * from organigramma.get_strutture_figlie(?);";
        Query query1 = em.createNativeQuery(query);
        query1.setParameter(1, idStruttura);
        List<Integer> lista = query1.getResultList();
        lista.add(idStruttura); // Aggiungo anche la struttura padre

        return lista;
    }
}
