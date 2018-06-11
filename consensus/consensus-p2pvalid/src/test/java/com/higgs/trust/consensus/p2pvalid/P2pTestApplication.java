package com.higgs.trust.consensus.p2pvalid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author cwy
 */
@Slf4j
@SpringBootApplication
@EnableAspectJAutoProxy
@EnableDiscoveryClient
@EnableFeignClients
@ComponentScan({"com.higgs.trust.consensus.p2pvalid", "com.higgs.trust.common"})
public class P2pTestApplication {
}