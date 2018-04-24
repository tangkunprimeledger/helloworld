package com.higgs.trust.slave;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * @author young001
 */
@SpringBootApplication @EnableTransactionManagement @EnableAspectJAutoProxy @Slf4j @EnableFeignClients
@ComponentScan({"com.higgs.trust.slave", "com.higgs.trust.consensus"}) public class Application
    extends WebMvcConfigurerAdapter {

    @Override public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        super.configureMessageConverters(converters);
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(SerializerFeature.DisableCircularReferenceDetect,
            SerializerFeature.WriteMapNullValue, SerializerFeature.SortField, SerializerFeature.MapSortField,
            SerializerFeature.WriteClassName);
        fastConverter.setFastJsonConfig(fastJsonConfig);
        converters.add(fastConverter);
    }

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

    /**
     * 启动入口。<br> 需要通过启动参数设置配置文件路径，例如：-Dspring.config.location=file:/data/home/admin/prime_demo/conf/dev_config.json<br>
     * mybatis代码生成工具：https://tower.im/projects/cc46ccaf6b1f4f398d7d2277fab3f67d/docs/52c7297b64a94da690191a891862939b/
     */
    public static void main(String[] args) throws Exception {
        //JSON auto detect class type
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        //JSON不做循环引用检测
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.DisableCircularReferenceDetect.getMask();
        //JSON输出NULL属性
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteMapNullValue.getMask();
        //toJSONString的时候对一级key进行按照字母排序
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.SortField.getMask();
        //toJSONString的时候对嵌套结果进行按照字母排序
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.MapSortField.getMask();
        //toJSONString的时候记录Class的name
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteClassName.getMask();

        ApplicationContext context = SpringApplication.run(Application.class, args);

        //init all snapshot service
        context.getBean(SnapshotService.class).init();

        log.info("higgs.trust slave is running...");
    }

}