package com.higgs.trust.slave.common.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.instrument.async.TraceableExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * @Description:
 * @author: pengdi
 **/
@Configuration public class InitBeanConfig {
    @Autowired BeanFactory beanFactory;

    @Bean public ExecutorService packageThreadPool() {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("package-pool-$d").build();
        ExecutorService packageExecutor =
            new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1024), namedThreadFactory,
                new ThreadPoolExecutor.AbortPolicy());
        return new TraceableExecutorService(beanFactory, packageExecutor);
    }
}
