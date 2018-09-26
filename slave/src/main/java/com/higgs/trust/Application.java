package com.higgs.trust;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author young001
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableAspectJAutoProxy
@Slf4j
@EnableFeignClients
//@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
public class Application {

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

        SpringApplication.run(Application.class, args);

        log.info("higgs.trust slave is running...");
    }

}