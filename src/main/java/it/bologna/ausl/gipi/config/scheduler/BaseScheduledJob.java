package it.bologna.ausl.gipi.config.scheduler;

/**
 *
 * @author spritz
 */
public interface BaseScheduledJob {

    /**
     * nome del servizio su database
     *
     * @return
     */
    public String getJobName();

    /**
     * cosa deve fare il job
     */
    public void run();

}
