package com.higgs.trust;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.rs.core.integration.ServiceProviderClient;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.integration.block.BlockChainClient;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mybatis.spring.annotation.MapperScan;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

//@RunWith(SpringRunner.class)
@SpringBootTest
@EnableFeignClients
public class IntegrateBaseTest extends AbstractTestNGSpringContextTests {
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
        //toJSONString的时候记录Class的name
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteClassName.getMask();
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