package com.higgs.trust.slave.controller;

import com.higgs.trust.slave.IntegrateBaseTest;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.integration.usercenter.ServiceProviderClient;
import org.junit.Test;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;

import javax.sql.DataSource;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertThat;

public class ControllerDemoTest extends IntegrateBaseTest {
    @LocalServerPort private int port;

    @MockBean private ServiceProviderClient serviceProviderClient;

    @MockBean private DataSource dataSource;

    private TestRestTemplate testRestTemplate = new TestRestTemplate();

    @Test public void controllerDemo() throws Exception {

        RespData respData =
            testRestTemplate.getForObject("http://localhost:" + port + "/controller_demo", RespData.class);
        RespData<String> respDataExpect = new RespData<>();
        respDataExpect.setData("controller demo name");

        assertThat(respData, sameBeanAs(respDataExpect));
    }

}