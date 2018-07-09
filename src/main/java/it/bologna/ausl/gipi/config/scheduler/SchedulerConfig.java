package it.bologna.ausl.gipi.config.scheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 *
 * @author spritz
 */
@Configuration
public class SchedulerConfig implements SchedulingConfigurer {

    private final int POOL_SIZE = 1;

    private volatile ScheduledTaskRegistrar scheduledTaskRegistrar;

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        this.scheduledTaskRegistrar = scheduledTaskRegistrar;
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();

        threadPoolTaskScheduler.setPoolSize(POOL_SIZE);
        threadPoolTaskScheduler.setThreadNamePrefix("gipi-scheduled-task-pool-");
        threadPoolTaskScheduler.initialize();

        this.scheduledTaskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
    }

    @Bean(destroyMethod = "shutdown")
    public Executor taskExecutor() {
//        System.out.println("destroyed method");
        // parametro: numero di thread da tenere nel pool
        return Executors.newScheduledThreadPool(1);
    }

    @PreDestroy
    public void destroy() {
//        System.out.println("destroyed method");
        this.scheduledTaskRegistrar.destroy();
    }

    public void start() {
        configureTasks(scheduledTaskRegistrar);
    }

}
