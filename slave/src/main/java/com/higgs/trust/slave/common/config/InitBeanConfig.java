package com.higgs.trust.slave.common.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.higgs.trust.slave.common.util.asynctosync.HashBlockingMap;
import com.higgs.trust.common.constant.Constant;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.cloud.sleuth.instrument.async.LazyTraceExecutor;
import org.springframework.cloud.sleuth.instrument.async.LazyTraceThreadPoolTaskExecutor;
import org.springframework.cloud.sleuth.instrument.async.TraceableExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.support.NamedDaemonThreadFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @Description:
 * @author: pengdi
 **/
@Configuration public class InitBeanConfig {
    @Autowired BeanFactory beanFactory;

    @Bean(name = "txRequired")
    public TransactionTemplate txRequired(PlatformTransactionManager platformTransactionManager) {
        return new TransactionTemplate(platformTransactionManager);
    }

    @Bean(name = "txNested")
    public TransactionTemplate txNested(PlatformTransactionManager platformTransactionManager) {
        TransactionTemplate tx = new TransactionTemplate(platformTransactionManager);
        tx.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_NESTED);
        return tx;
    }

    @Bean(name = "txRequiresNew")
    public TransactionTemplate txRequiresNew(PlatformTransactionManager platformTransactionManager) {
        TransactionTemplate tx = new TransactionTemplate(platformTransactionManager);
        tx.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return tx;
    }
    @Bean(name = "packageThreadPool")
    public ExecutorService packageThreadPool() {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("package-pool-%d").build();
        ExecutorService packageExecutor =
            new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1024), namedThreadFactory,
                new ThreadPoolExecutor.AbortPolicy());
        return new TraceableExecutorService(beanFactory, packageExecutor);
    }

    @Bean public HttpMessageConverters HttpMessageConverters() {
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(SerializerFeature.DisableCircularReferenceDetect,
            SerializerFeature.WriteMapNullValue, SerializerFeature.SortField, SerializerFeature.MapSortField);
        fastConverter.setFastJsonConfig(fastJsonConfig);
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        fastConverter.setSupportedMediaTypes(supportedMediaTypes);
        return new HttpMessageConverters(fastConverter);
    }

    @Bean public HashBlockingMap persistedResultMap() {
        return new HashBlockingMap<>(Constant.MAX_BLOCKING_QUEUE_SIZE);
    }

    @Bean public HashBlockingMap clusterPersistedResultMap() {
        return new HashBlockingMap<>(Constant.MAX_BLOCKING_QUEUE_SIZE);
    }

    @Bean (name = "txProcessExecutorPool") public ThreadPoolTaskExecutor txProcessExecutorPool() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(10);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        threadPoolTaskExecutor.setQueueCapacity(5000);
        threadPoolTaskExecutor.setThreadNamePrefix("txProcessExecutor-");
        threadPoolTaskExecutor.initialize();
        return new LazyTraceThreadPoolTaskExecutor(beanFactory, threadPoolTaskExecutor);
    }

    @Bean (name = "txSubmitExecutorPool")public ThreadPoolTaskExecutor txSubmitExecutorPool() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(10);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        threadPoolTaskExecutor.setQueueCapacity(5000);
        threadPoolTaskExecutor.setThreadNamePrefix("txSubmitExecutor-");
        threadPoolTaskExecutor.initialize();
        return new LazyTraceThreadPoolTaskExecutor(beanFactory, threadPoolTaskExecutor);
    }

    @Bean (name = "syncVotingExecutorPool") public ThreadPoolTaskExecutor syncVotingExecutorPool() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(5);
        threadPoolTaskExecutor.setMaxPoolSize(20);
        threadPoolTaskExecutor.setQueueCapacity(5000);
        threadPoolTaskExecutor.setThreadNamePrefix("syncVotingExecutor-");
        threadPoolTaskExecutor.initialize();
        return new LazyTraceThreadPoolTaskExecutor(beanFactory, threadPoolTaskExecutor);
    }

    @Bean (name = "asyncVotingExecutorPool")  public ThreadPoolTaskExecutor asyncVotingExecutorPool() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(5);
        threadPoolTaskExecutor.setMaxPoolSize(20);
        threadPoolTaskExecutor.setQueueCapacity(5000);
        threadPoolTaskExecutor.setThreadNamePrefix("asyncVotingExecutorPool-");
        threadPoolTaskExecutor.initialize();
        return new LazyTraceThreadPoolTaskExecutor(beanFactory, threadPoolTaskExecutor);
    }

    @Bean (name = "txConsumerExecutor")  public Executor txConsumerExecutor() {
        NamedDaemonThreadFactory namedDaemonThreadFactory = new NamedDaemonThreadFactory("txConsumerExecutor-");
        ExecutorService service = Executors.newSingleThreadExecutor(namedDaemonThreadFactory);
        return new LazyTraceExecutor(beanFactory, service);
    }

    @Bean (name = "p2pSendExecutor")  public ThreadPoolTaskExecutor p2pSendExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(10);
        threadPoolTaskExecutor.setMaxPoolSize(50);
        threadPoolTaskExecutor.setQueueCapacity(5000);
        threadPoolTaskExecutor.setKeepAliveSeconds(3600);
        threadPoolTaskExecutor.setThreadNamePrefix("p2pSendExecutor-");
        threadPoolTaskExecutor.initialize();
        return new LazyTraceThreadPoolTaskExecutor(beanFactory, threadPoolTaskExecutor);
    }

    @Bean (name = "p2pReceiveExecutor")  public ThreadPoolTaskExecutor p2pReceiveExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(5);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        threadPoolTaskExecutor.setQueueCapacity(1024);
        threadPoolTaskExecutor.setKeepAliveSeconds(3600);
        threadPoolTaskExecutor.setThreadNamePrefix("p2pReceivedExecutor-");
        threadPoolTaskExecutor.initialize();
        return new LazyTraceThreadPoolTaskExecutor(beanFactory, threadPoolTaskExecutor);
    }
}
