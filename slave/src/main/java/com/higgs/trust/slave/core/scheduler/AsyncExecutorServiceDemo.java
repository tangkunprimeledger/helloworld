package com.higgs.trust.slave.core.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.instrument.async.LazyTraceThreadPoolTaskExecutor;
import org.springframework.cloud.sleuth.instrument.async.TraceableExecutorService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

/**
 * 线程池执行异步任务时需要构造TraceableExecutorService对象以支持traceid功能
 *
 * @author yuguojia
 * @create 2018-01-04 10:55
 */
@Service @Profile("scheduler") @Slf4j public class AsyncExecutorServiceDemo {

    @Autowired private BeanFactory beanFactory;

    private static final ExecutorService executorService =
        new ThreadPoolExecutor(1, 1, 5000, TimeUnit.MICROSECONDS, new LinkedBlockingDeque<>(5000));

    private static ThreadPoolTaskExecutor threadPoolTaskExecutor;

    {
        threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(10);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        threadPoolTaskExecutor.setQueueCapacity(10);
        threadPoolTaskExecutor.setThreadNamePrefix("MyExecutor-");
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.initialize();
    }

    /**
     * 使用Executors方式创建的线程池，使用execute或submit等方式
     */
    @Scheduled(cron = "0/5 * *  * * ? ") protected void execute1() throws Exception {
        TraceableExecutorService executor = new TraceableExecutorService(beanFactory, executorService);
        executor.execute(this::scheduledJob);
        Future future = executor.submit(this::futureJob);
        String result = (String)future.get();
        log.info("TraceableExecutorService:" + result);
    }

    /**
     * 需要使用spring线程池，并使用future方式的
     */
    @Scheduled(cron = "0/5 * *  * * ? ")   //每5秒执行一次
    protected void execute2() throws Exception {
        LazyTraceThreadPoolTaskExecutor lazyTraceExecutor =
            new LazyTraceThreadPoolTaskExecutor(beanFactory, threadPoolTaskExecutor);
        Future future = lazyTraceExecutor.submit(this::futureJob);
        String result = (String)future.get();
        log.info("LazyTraceThreadPoolTaskExecutor:" + result);
    }

    private void scheduledJob() {
        log.info("scheduledJob: hello world");
    }

    private String futureJob() {
        return "futureJob : hello world";
    }
}