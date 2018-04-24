package com.higgs.trust.slave.api;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.integration.usercenter.ServiceConsumer;
import com.higgs.trust.slave.integration.usercenter.ServiceProviderClient;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class ServiceConsumerTest extends BaseTest {

    @Mock ServiceProviderClient serviceProviderClient;

    @InjectMocks private ServiceConsumer serviceConsumer;

    @Test public void sayHi() throws Exception {
        when(serviceProviderClient.consumeServiceProviderHome(anyString())).thenReturn("demo");
        assertEquals("demo", serviceConsumer.sayHi("hello"));
    }

}