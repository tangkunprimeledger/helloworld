package com.higgs.trust.rs;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author young001
 */
@SpringBootApplication @EnableTransactionManagement @EnableAspectJAutoProxy @Slf4j public class Application
    extends WebMvcConfigurerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    /**
     * 启动入口。<br> 需要通过启动参数设置配置文件路径，例如：-Dspring.config.location=file:/data/home/admin/prime_demo/conf/dev_config.json<br>
     * mybatis代码生成工具：https://tower.im/projects/cc46ccaf6b1f4f398d7d2277fab3f67d/docs/52c7297b64a94da690191a891862939b/
     */
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
        LOGGER.info("higgs.trust rs is running...");

        // log testw
        LOGGER.info("test..");
    }

}