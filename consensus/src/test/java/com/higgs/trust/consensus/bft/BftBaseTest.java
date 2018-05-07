package com.higgs.trust.consensus.bft;

import com.higgs.trust.consensus.bft.config.ClientConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;

@SpringBootTest(classes = BftTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Import(ClientConfig.class)
public abstract class BftBaseTest
        extends AbstractTestNGSpringContextTests {

    @BeforeSuite
    public void beforeClass() throws Exception {
        System.setProperty("spring.config.location", "classpath:test-application.json");
    }

    @BeforeTest
    public void runBefore() {
        return;
    }

    @BeforeTest
    public void runAfter() {
        runLast();
    }

    protected void runLast() {
    }
}