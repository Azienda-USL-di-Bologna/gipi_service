package it.bologna.ausl.gipi.thread;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@Service("threadPool")
public class ThreadPool {

    public static final String DEFAULT_SHUTDOWN_ERROR_MESSAGE = "Il thread pool non si è fermato entro il timeout";
    public static final int MIN_CORE_THREADS = 10;
    private ExecutorService threadPool;

    private static final Logger log = LoggerFactory.getLogger(ThreadPool.class);

    @PostConstruct
    protected void init() {
        threadPool = new ThreadPoolExecutor(MIN_CORE_THREADS, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new BasicThreadFactory.Builder().namingPattern("next-thread-pool-%d").build());
    }

    @PreDestroy
    protected void destroy() {
        shutdown(threadPool);
    }

    public Future<?> submit(Runnable runnable) {
        return threadPool.submit(runnable);
    }

    public <V> Future<V> submit(Callable<V> callable) {
        return threadPool.submit(callable);
    }

    public static void shutdown(ExecutorService threadPool) {
        shutdown(threadPool, DEFAULT_SHUTDOWN_ERROR_MESSAGE);
    }

    public static void shutdown(ExecutorService threadPool, String errorMessage) {
        threadPool.shutdownNow();
        try {
            threadPool.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error(errorMessage, e);
        }
    }
}
