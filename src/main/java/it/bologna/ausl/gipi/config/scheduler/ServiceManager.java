package it.bologna.ausl.gipi.config.scheduler;

import it.bologna.ausl.entities.gipi.Servizio;
import it.bologna.ausl.entities.repository.ServizioRepository;
import static java.time.LocalDateTime.now;
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

    private volatile ConcurrentHashMap<ServiceKey, Object> serviceMap;

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

    public synchronized ConcurrentHashMap<ServiceKey, Object> getServiceMap() {
        return serviceMap;
    }

    public synchronized void setServiceMap(ConcurrentHashMap<ServiceKey, Object> serviceMap) {
        this.serviceMap = serviceMap;
    }

    public Servizio getService(ServiceKey key) {

        return (Servizio) serviceMap.get(key);
    }

    public synchronized void setService(ServiceKey key, Object obj) {
        serviceMap.put(key, obj);
    }

    public synchronized void startService(ServiceKey key) {
        Servizio servizio = ((Servizio) serviceMap.get(key));
        servizio.setActive(Boolean.TRUE);
        serviceMap.put(key, servizio);
        servizioRepository.save(servizio);
    }

    public synchronized void stopService(ServiceKey key) {
        Servizio servizio = ((Servizio) serviceMap.get(key));
        servizio.setActive(Boolean.FALSE);
        serviceMap.put(key, servizio);
        servizioRepository.save(servizio);
    }

    public synchronized void stopAllService() {
        for (ConcurrentHashMap.Entry<ServiceKey, Object> entry : serviceMap.entrySet()) {
            Servizio servizio = (Servizio) entry.getValue();
            servizio.setActive(Boolean.FALSE);
            serviceMap.put(entry.getKey(), servizio);
            servizioRepository.save(servizio);
        }
    }

    public synchronized void reloadFromDb() {
        // lettura di dati da database
        serviceMap.clear();
        List<Servizio> servizi = servizioRepository.findAll();
        for (Iterator<Servizio> iterator = servizi.iterator(); iterator.hasNext();) {
            Servizio servizio = (Servizio) iterator.next();
            if (servizio.getIdAzienda() != null) {
                setService(new ServiceKey(servizio.getNome(), servizio.getIdAzienda().getId()), servizio);
            } else {
                setService(new ServiceKey(servizio.getNome(), null), servizio);
            }
        }
    }

    public void setDataInizioRun(ServiceKey key) {
        Servizio servizio = ((Servizio) serviceMap.get(key));
        servizio.setDataInizio(now());
        servizioRepository.save(servizio);
    }

    public void setDataFineRun(ServiceKey key) {
        Servizio servizio = ((Servizio) serviceMap.get(key));
        servizio.setDataFine(now());
        servizioRepository.save(servizio);
    }

}
