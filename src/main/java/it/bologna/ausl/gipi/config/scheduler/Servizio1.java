package it.bologna.ausl.gipi.config.scheduler;

import it.bologna.ausl.entities.gipi.Servizio;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author spritz
 */
@Component
public class Servizio1 implements BaseScheduledJob {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Autowired
    ServiceManager serviceManager;

    @Override
    public String getJobName() {
        return "servizio1";
    }

    @Override
    public void run() {
        Servizio service = serviceManager.getService(getJobName());

        if (service != null && service.getActive()) {
            System.out.println(getJobName() + " attivo! ");
            System.out.println("AAA inizio" + String.format("Current Thread : {%s: %s}", Thread.currentThread().getName(), dateTimeFormatter.format(LocalDateTime.now())));
            int min = 1;
            int max = 10;

            long tempo = (min + (long) (Math.random() * ((max - min))));
            System.out.println("time: " + tempo);
            System.out.println("AAA fine" + String.format("Current Thread : {%s: %s}", Thread.currentThread().getName(), dateTimeFormatter.format(LocalDateTime.now())));

        } else {
            System.out.println(getJobName() + " non presente o non attivo");
        }
    }

}
