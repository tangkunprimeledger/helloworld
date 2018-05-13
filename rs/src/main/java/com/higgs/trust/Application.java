package com.higgs.trust;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author young001
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableAspectJAutoProxy
@Slf4j
@EnableFeignClients
public class Application {
    //异常推进任务定时器线程池
    public static final ScheduledExecutorService COMMON_THREAD_POOL = Executors
        .newScheduledThreadPool(8, new BasicThreadFactory.Builder().namingPattern("issue-act-schedule-pool-%d").daemon(true).build());
    public static final long INITIAL_DELAY = 60;//线程第一次运行初始间隔时间
    public static final long PERIOD = 30;//间隔时间

    /**
     * 启动入口。<br> 需要通过启动参数设置配置文件路径，例如：-Dspring.config.location=file:/data/home/admin/prime_demo/conf/dev_config.json<br>
     * mybatis代码生成工具：https://tower.im/projects/cc46ccaf6b1f4f398d7d2277fab3f67d/docs/52c7297b64a94da690191a891862939b/
     */
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
        log.info("higgs.trust rs is running...");
    }
}