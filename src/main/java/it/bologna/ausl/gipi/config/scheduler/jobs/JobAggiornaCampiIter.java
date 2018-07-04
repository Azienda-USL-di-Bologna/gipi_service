package it.bologna.ausl.gipi.config.scheduler.jobs;

import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import it.bologna.ausl.entities.gipi.Iter;
import it.bologna.ausl.entities.gipi.QIter;
import it.bologna.ausl.entities.gipi.Servizio;
import it.bologna.ausl.entities.gipi.Stato;
import it.bologna.ausl.entities.repository.IterRepository;
import it.bologna.ausl.gipi.config.scheduler.BaseScheduledJob;
import it.bologna.ausl.gipi.config.scheduler.ServiceKey;
import it.bologna.ausl.gipi.config.scheduler.ServiceManager;
import java.util.Calendar;
import java.util.List;
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
                    .fetch();

            for (Iter iter : iters) {

                log.info(getJobName() + strPreElaborazione + "id_ter: " + iter.getId() + " data_chiusura_prevista: " + iter.getDataChiusuraPrevista() + " giorni_sospensione_trascorsi: " + iter.getGiorniSospensioneTrascorsi());

                Integer giorniDeroga = iter.getDerogaDurata() == null ? 0 : iter.getDerogaDurata();
                Integer giorniSospensioneTrascorsi = iter.getGiorniSospensioneTrascorsi() == null ? 0 : iter.getGiorniSospensioneTrascorsi();

                if (iter.getDataChiusuraPrevista() != null) {
                    // convert date to calendar
                    Calendar c = Calendar.getInstance();
                    c.setTime(iter.getDataChiusuraPrevista());

                    // manipulate date
                    c.add(Calendar.DATE, 1);

                    // convert calendar to date
                    iter.setDataChiusuraPrevista(c.getTime());
                    log.info(getJobName() + strElaborazione + "id_ter: " + iter.getId() + " SET data_chiusura_prevista: " + iter.getDataChiusuraPrevista());

                } else {
//                Date dataAvvio = iter.getDataAvvio();
//                Integer durataMassimaProcedimento = iter.getIdProcedimento().getIdAziendaTipoProcedimento().getDurataMassimaProcedimento();
//
//                Calendar c = Calendar.getInstance();
//                c.setTime(dataAvvio);
//                c.add(Calendar.DATE, durataMassimaProcedimento);
//                if (giorniDeroga != null) {
//                    c.add(Calendar.DATE, giorniDeroga);
//                }
//                c.add(Calendar.DATE, giorniSospensioneTrascorsi);
//
//                iter.setDataChiusuraPrevista(c.getTime());
                }

                iter.setGiorniSospensioneTrascorsi(giorniSospensioneTrascorsi + 1);
                log.info(getJobName() + strElaborazione + "id_ter: " + iter.getId() + " SET giorni_sospensione_trascorsi: " + giorniSospensioneTrascorsi + 1);
            }

//            log.info("-----------------------------------------------------------------------------------------");
//            for (Iter iter : iters) {
//                log.info(getJobName() + ": id_ter: " + iter.getId() + " data_chiusura_prevista: " + iter.getDataChiusuraPrevista() + " giorni_sospensione_trascorsi: " + iter.getGiorniSospensioneTrascorsi());
//            }
            iterRepository.save(iters);
            log.info(getJobName() + strPostElaborazione + "salvataggio degli iter modificati andara a buon fine");
            serviceManager.setDataFineRun(serviceKey);
            log.info(delimiter + "STOP: " + getJobName() + delimiter);
        } else {
            log.info(getJobName() + ": servizio non attivo");
//            log2.info(getJobName() + ": servizio non attivo");
        }

    }

}
