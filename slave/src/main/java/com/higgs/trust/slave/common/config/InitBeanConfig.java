package com.higgs.trust.slave.common.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.higgs.trust.slave.asynctosync.HashBlockingMap;
import com.higgs.trust.slave.common.constant.Constant;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.cloud.sleuth.instrument.async.LazyTraceThreadPoolTaskExecutor;
import org.springframework.cloud.sleuth.instrument.async.TraceableExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Deque;
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

    @Bean public Deque<SignedTransaction> pendingTxQueue() {
        return new ConcurrentLinkedDeque<>();
    }

    @Bean public ConcurrentLinkedHashMap existTxMap() {
        return new ConcurrentLinkedHashMap.Builder<String, String>().maximumWeightedCapacity(Constant.MAX_EXIST_MAP_SIZE).build();
    }

    @Bean public Long packHeight() {
        return new Long(0);
    }

    @Bean public BlockingQueue<Package> pendingPack() {
        return new LinkedBlockingDeque<>();
    }

    @Bean public ExecutorService packageThreadPool() {
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
            SerializerFeature.WriteMapNullValue, SerializerFeature.SortField, SerializerFeature.MapSortField,
            SerializerFeature.WriteClassName);
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

    @Bean public ThreadPoolTaskExecutor txProcessExecutorPool() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(10);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        threadPoolTaskExecutor.setQueueCapacity(5000);
        threadPoolTaskExecutor.setThreadNamePrefix("txProcessExecutor-");
        threadPoolTaskExecutor.initialize();
        return new LazyTraceThreadPoolTaskExecutor(beanFactory, threadPoolTaskExecutor);
    }

    @Bean public ThreadPoolTaskExecutor txSubmitExecutorPool() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(10);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        threadPoolTaskExecutor.setQueueCapacity(5000);
        threadPoolTaskExecutor.setThreadNamePrefix("txSubmitExecutor-");
        threadPoolTaskExecutor.initialize();
        return new LazyTraceThreadPoolTaskExecutor(beanFactory, threadPoolTaskExecutor);
    }

    @Bean public ThreadPoolTaskExecutor syncVotingExecutorPool() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(20);
        threadPoolTaskExecutor.setMaxPoolSize(50);
        threadPoolTaskExecutor.setQueueCapacity(5000);
        threadPoolTaskExecutor.setThreadNamePrefix("syncVotingExecutor-");
        threadPoolTaskExecutor.initialize();
        return new LazyTraceThreadPoolTaskExecutor(beanFactory, threadPoolTaskExecutor);
    }

    @Bean public ThreadPoolTaskExecutor asyncVotingExecutorPool() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(20);
        threadPoolTaskExecutor.setMaxPoolSize(50);
        threadPoolTaskExecutor.setQueueCapacity(5000);
        threadPoolTaskExecutor.setThreadNamePrefix("asyncVotingExecutorPool-");
        threadPoolTaskExecutor.initialize();
        return new LazyTraceThreadPoolTaskExecutor(beanFactory, threadPoolTaskExecutor);
    }
}
