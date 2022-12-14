package it.bologna.ausl.gipi.config.scheduler;

import it.bologna.ausl.gipi.config.scheduler.jobs.JobAggiornaCampiIter;
import it.bologna.ausl.gipi.config.scheduler.jobs.JobInviaNotificheTerminiSospensioneIterScaduti;
import it.bologna.ausl.gipi.config.scheduler.jobs.JobNotificheChiusuraIter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author spritz
 */
@Component
@PropertySource("classpath:cron_expressions.properties")
@PropertySource(value="file:config/cron_expressions.properties",ignoreResourceNotFound=true)
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

//    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    @Autowired
    ServiceManager serviceManager;

//    @Autowired
//    Servizio1 servizio1;
//
//    @Autowired
//    Servizio2 servizio2;
    @Autowired
    JobAggiornaCampiIter jobAggiornaCampiIter;

    @Autowired
    JobInviaNotificheTerminiSospensioneIterScaduti notificheSospensioneIter;

    @Autowired
    JobNotificheChiusuraIter jobNotificheChiusuraIter;
    
    @Value("${service.scheduler-active:false}")
    Boolean serviceActive;

//    @Scheduled(fixedRate = 1000)
//    public void esempioServizio1() throws InterruptedException {
//        log.debug("servizio1");
//        servizio1.run();
////        Servizio service = serviceManager.getService("simple_service");
////
////        if (service != null && service.getActive()) {
////            System.out.println("servizio_1, attivo! ");
////            System.out.println("AAA inizio" + String.format("Current Thread : {%s: %s}", Thread.currentThread().getName(), dateTimeFormatter.format(LocalDateTime.now())));
////            int min = 1;
////            int max = 10;
////
////            long tempo = (min + (long) (Math.random() * ((max - min))));
////            System.out.println("time: " + tempo);
////            System.out.println("AAA fine" + String.format("Current Thread : {%s: %s}", Thread.currentThread().getName(), dateTimeFormatter.format(LocalDateTime.now())));
////
////        } else {
////            System.out.println("servizio_1 non presente o non attivo");
////        }
//
//    }
//    @Scheduled(fixedRate = 500)
//    public void esempioServizio2() throws InterruptedException {
//        log.debug("servizio2");
//        servizio2.run();
////        Servizio service = serviceManager.getService("simple_service2");
////
////        if (service != null && service.getActive()) {
////            System.out.println("servizio_2, attivo!");
////        } else {
////            System.out.println("servizio_2 non presente o non attivo");
////        }
//    }
    @Scheduled(cron = "${service.job-aggiorna-campi-iter}")
    public void jobAggiornaCampiIterAndInviaNotifiche() throws InterruptedException {
        log.info("schedulatore-active: " + serviceActive);
        if (serviceActive) {
            jobAggiornaCampiIter.run();
            jobNotificheChiusuraIter.run();
            notificheSospensioneIter.run();
        }
    }
}
