package it.bologna.ausl.gipi.config.scheduler.jobs;

import it.bologna.ausl.entities.gipi.Servizio;
import it.bologna.ausl.gipi.config.scheduler.BaseScheduledJob;
import it.bologna.ausl.gipi.config.scheduler.ServiceKey;
import it.bologna.ausl.gipi.config.scheduler.ServiceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author spritz
 */
@Component
public class Servizio2 implements BaseScheduledJob {

    @Autowired
    ServiceManager serviceManager;

    @Override
    public String getJobName() {
        return "servizio2";
    }

    @Override
    public void run() {
        Servizio service = serviceManager.getService(new ServiceKey(getJobName(), null));

        if (service != null && service.getActive()) {
            System.out.println(getJobName() + " attivo!");
        } else {
            System.out.println(getJobName() + " non presente o non attivo");
        }
    }

}
