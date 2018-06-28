package it.bologna.ausl.gipi.config.scheduler;

/**
 *
 * @author spritz
 */
public interface BaseScheduledJob {

    public String getJobName();

    public void run();

}
