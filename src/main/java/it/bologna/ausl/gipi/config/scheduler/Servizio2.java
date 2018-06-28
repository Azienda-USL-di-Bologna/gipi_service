package it.bologna.ausl.gipi.config.scheduler;

import it.bologna.ausl.entities.gipi.Servizio;
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
        Servizio service = serviceManager.getService(getJobName());

        if (service != null && service.getActive()) {
            System.out.println(getJobName() + " attivo!");
        } else {
            System.out.println(getJobName() + " non presente o non attivo");
        }
    }

}
