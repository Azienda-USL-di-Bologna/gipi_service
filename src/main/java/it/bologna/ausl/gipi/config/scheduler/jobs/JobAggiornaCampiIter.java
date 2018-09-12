package it.bologna.ausl.gipi.config.scheduler.jobs;

import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.gipi.Evento;
import it.bologna.ausl.entities.gipi.EventoIter;
import it.bologna.ausl.entities.gipi.Iter;
import it.bologna.ausl.entities.gipi.QEventoIter;
import it.bologna.ausl.entities.gipi.QIter;
import it.bologna.ausl.entities.gipi.Servizio;
import it.bologna.ausl.entities.gipi.Stato;
import it.bologna.ausl.entities.repository.IterRepository;
import it.bologna.ausl.gipi.config.scheduler.BaseScheduledJob;
import it.bologna.ausl.gipi.config.scheduler.ServiceKey;
import it.bologna.ausl.gipi.config.scheduler.ServiceManager;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author spritz
 */
@Component
public class JobAggiornaCampiIter implements BaseScheduledJob {

    private static final Logger log = LoggerFactory.getLogger(JobAggiornaCampiIter.class);
//    private static final Logger log2 = LoggerFactory.getLogger("otherlog");

    private static final String strElaborazione = " [elaborazione] ";
    private static final String strPreElaborazione = " [pre-elaborazione] ";
    private static final String strPostElaborazione = " [post-elaborazione] ";

    @PersistenceContext
    EntityManager em;

    @Autowired
    IterRepository iterRepository;

    @Autowired
    ServiceManager serviceManager;

    @Override
    public String getJobName() {
        return "aggiorna_campi_iter";
    }

    @Override
    public void run() {
        ServiceKey serviceKey = new ServiceKey(getJobName(), null);
        Servizio service = serviceManager.getService(serviceKey);

        String s = "-";
        String delimiter = IntStream.range(0, 30).mapToObj(i -> s).collect(Collectors.joining(""));

        if (service != null && service.getActive()) {
            log.info(delimiter + "START: " + getJobName() + delimiter);
            serviceManager.setDataInizioRun(serviceKey);

            QIter qIter = QIter.iter;

            JPQLQuery<Iter> queryIter = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
            List<Iter> iters = queryIter.from(qIter)
                    .where(qIter.idStato.codice.eq(Stato.CodiciStato.SOSPESO.toString()))
                    //.where(qIter.numero.eq(565))
                    .fetch();

            for (Iter iter : iters) {
                log.info(getJobName() + strPreElaborazione + "id_ter: " + iter.getId() + " data_chiusura_prevista: " + iter.getDataChiusuraPrevista() + " giorni_sospensione_trascorsi: " + iter.getGiorniSospensioneTrascorsi());

//                Integer giorniDeroga = iter.getDerogaDurata() == null ? 0 : iter.getDerogaDurata();
                if (iter.getDataChiusuraPrevista() != null) {
                    // convert date to calendar
                    Calendar c = Calendar.getInstance();
                    c.setTime(iter.getDataChiusuraPrevista());

                    // manipulate date
                    c.add(Calendar.DATE, 1);

                    // convert calendar to date
                    iter.setDataChiusuraPrevista(c.getTime());
                    log.info(getJobName() + strElaborazione + "id_ter: " + iter.getId() + " SET data_chiusura_prevista: " + iter.getDataChiusuraPrevista());
                }

                iter.setGiorniSospensioneTrascorsi(calcolaGiorniSospensioneTrascorsi(iter));
                log.info(getJobName() + strElaborazione + "id_ter: " + iter.getId() + " SET giorni_sospensione_trascorsi: " + iter.getGiorniSospensioneTrascorsi());

            }

            iterRepository.saveAll(iters);
            log.info(getJobName() + strPostElaborazione + "salvataggio degli iter modificati andata a buon fine");
            serviceManager.setDataFineRun(serviceKey);
            log.info(delimiter + "STOP: " + getJobName() + delimiter);
        } else {
            log.info(getJobName() + ": servizio non attivo");
        }

    }

    public int calcolaGiorniSospensioneTrascorsi(Iter iter) {
        log.info(getJobName() + " Calcolo giorni di sospensione");
        int giorniSospensioneTrascorsi = 0;
        Date startDate = null;
        Date stopDate = null;
        boolean ancoraSospeso = false;

        JPQLQuery<EventoIter> queryEventiIter = new JPAQuery(this.em, EclipseLinkTemplates.DEFAULT);
        QEventoIter qEventoIter = QEventoIter.eventoIter;
        List<EventoIter> eventiIter = queryEventiIter.from(qEventoIter).where(qEventoIter.idIter.id.eq(iter.getId()).and(qEventoIter.idEvento.codice.in(Evento.CodiciEvento.apertura_sospensione.toString(), Evento.CodiciEvento.chiusura_sospensione.toString()))).orderBy(qEventoIter.id.asc()).fetch();
        Calendar cal = Calendar.getInstance();

        if (eventiIter.size() > 0) {
            for (EventoIter eventoIter : eventiIter) {

                if (eventoIter.getIdEvento().getCodice().equals(Evento.CodiciEvento.apertura_sospensione.toString())) {

                    cal.setTime(eventoIter.getDataOraEvento());
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    startDate = cal.getTime();
                    log.info(getJobName() + " start: " + startDate.toString());
                    ancoraSospeso = true;
                }

                if (eventoIter.getIdEvento().getCodice().equals(Evento.CodiciEvento.chiusura_sospensione.toString())) {
                    cal.setTime(eventoIter.getDataOraEvento());
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    stopDate = cal.getTime();
                    long diff = TimeUnit.DAYS.convert(Math.abs(stopDate.getTime() - startDate.getTime()), TimeUnit.MILLISECONDS);
                    giorniSospensioneTrascorsi += diff;
                    log.info(getJobName() + " stop: " + stopDate.toString());
                    log.info(getJobName() + " giorni_calcolati: " + diff);
                    ancoraSospeso = false;
                }
            }

            if (ancoraSospeso) {
                long diff = TimeUnit.DAYS.convert(Math.abs(new Date().getTime() - startDate.getTime()), TimeUnit.MILLISECONDS);
                giorniSospensioneTrascorsi += diff;
                log.info(getJobName() + "ANCORA SOSPESO, giorni_calcolati: " + diff);
            }
        }

        //log.info("giorni " + giorniSospensioneTrascorsi);
        log.info(getJobName() + "ritorno giorni: " + (int) (long) giorniSospensioneTrascorsi);
        return (int) (long) giorniSospensioneTrascorsi;
    }
}
