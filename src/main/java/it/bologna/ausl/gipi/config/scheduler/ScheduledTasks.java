package it.bologna.ausl.gipi.config.scheduler;

import it.bologna.ausl.entities.gipi.Servizio;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author spritz
 */
@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

//    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    @Autowired
    ServiceManager serviceManager;

    @Autowired
    Servizio1 servizio1;

    @Autowired
    Servizio2 servizio2;

    @Scheduled(fixedRate = 1000)
    public void esempioServizio1() throws InterruptedException {

        servizio1.run();
//        Servizio service = serviceManager.getService("simple_service");
//
//        if (service != null && service.getActive()) {
//            System.out.println("servizio_1, attivo! ");
//            System.out.println("AAA inizio" + String.format("Current Thread : {%s: %s}", Thread.currentThread().getName(), dateTimeFormatter.format(LocalDateTime.now())));
//            int min = 1;
//            int max = 10;
//
//            long tempo = (min + (long) (Math.random() * ((max - min))));
//            System.out.println("time: " + tempo);
//            System.out.println("AAA fine" + String.format("Current Thread : {%s: %s}", Thread.currentThread().getName(), dateTimeFormatter.format(LocalDateTime.now())));
//
//        } else {
//            System.out.println("servizio_1 non presente o non attivo");
//        }

    }

    @Scheduled(fixedRate = 500)
    public void esempioServizio2() throws InterruptedException {
        servizio2.run();
//        Servizio service = serviceManager.getService("simple_service2");
//
//        if (service != null && service.getActive()) {
//            System.out.println("servizio_2, attivo!");
//        } else {
//            System.out.println("servizio_2 non presente o non attivo");
//        }
    }
}
