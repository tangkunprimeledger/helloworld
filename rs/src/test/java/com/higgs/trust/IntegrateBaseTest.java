package com.higgs.trust;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

//import org.junit.After;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.runner.RunWith;

//@RunWith(SpringRunner.class)
@SpringBootTest
@EnableFeignClients
public class IntegrateBaseTest extends AbstractTestNGSpringContextTests {

    public static String DB_URL = "jdbc:mysql://localhost:3306/trust?user=root&password=root";
    @BeforeSuite public static void beforeClass() {
        System.setProperty("spring.config.location", "classpath:test-application.json");
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
    }

    @BeforeClass public void runBefore() {
        initMock();
    }

    @AfterClass public void runAfter() {
        runLast();
    }

    private void initMock() {
        MockitoAnnotations.initMocks(this);
    }

    protected void runLast() {
    }
}