package it.bologna.ausl.gipi.config.scheduler;

import it.bologna.ausl.entities.gipi.Servizio;
import it.bologna.ausl.entities.repository.ServizioRepository;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author spritz
 */
@Service
public class ServiceManager {

    private volatile ConcurrentHashMap<String, Object> serviceMap;

    @Autowired
    ServizioRepository servizioRepository;

    // viene eseguito una sola volta
    @PostConstruct
    public void init() {
        reloadFromDb();
    }

    public ServiceManager() {
        serviceMap = new ConcurrentHashMap<>();
    }

    public synchronized ConcurrentHashMap<String, Object> getServiceMap() {
        return serviceMap;
    }

    public synchronized void setServiceMap(ConcurrentHashMap<String, Object> serviceMap) {
        this.serviceMap = serviceMap;
    }

    public Servizio getService(String key) {

        return (Servizio) serviceMap.get(key);
    }

    public synchronized void setService(String key, Object obj) {
        serviceMap.put(key, obj);
    }

    public synchronized void startService(String key) {
        Servizio servizio = ((Servizio) serviceMap.get(key));
        servizio.setActive(Boolean.TRUE);
        serviceMap.put(key, servizio);
    }

    public synchronized void stopService(String key) {
        Servizio servizio = ((Servizio) serviceMap.get(key));
        servizio.setActive(Boolean.FALSE);
        serviceMap.put(key, servizio);
    }

    public synchronized void stopAllService() {
        for (ConcurrentHashMap.Entry<String, Object> entry : serviceMap.entrySet()) {
            Servizio servizio = (Servizio) entry.getValue();
            servizio.setActive(Boolean.FALSE);
            serviceMap.put(entry.getKey(), servizio);
        }
    }

    public synchronized void reloadFromDb() {
        // lettura di dati da database
        serviceMap.clear();
        List<Servizio> servizi = servizioRepository.findAll();
        for (Iterator<Servizio> iterator = servizi.iterator(); iterator.hasNext();) {
            Servizio servizio = (Servizio) iterator.next();
            setService(servizio.getNome(), servizio);
        }
    }

}
