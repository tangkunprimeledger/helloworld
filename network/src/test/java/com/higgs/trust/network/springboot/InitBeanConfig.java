package com.higgs.trust.network.springboot;

import com.higgs.trust.network.Address;
import com.higgs.trust.network.AuthenticationImp;
import com.higgs.trust.network.NetworkConfig;
import com.higgs.trust.network.NetworkManage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author duhongming
 * @date 2018/9/11
 */
@Configuration
public class InitBeanConfig {
    public InitBeanConfig() {
        System.out.println("InitBeanConfig ..." + host);
    }

    @Value("${network.host}")
    public String host;

    @Value("${network.port}")
    public int port;

    @Value("${network.peers}")
    public String[] peers;

    @Value("${server.port}")
    public int httpPort;

    @Bean
    public NetworkManage getNetworkManage() {
        System.out.println("getNetworkManage ... " + peers);
        if (peers == null ||peers.length == 0) {
            throw new IllegalArgumentException("Network peers is empty");
        }
        Address[] seeds = new Address[peers.length];
        for(int i = 0; i < peers.length; i++) {
            String[] host_port = peers[i].split(":");
            seeds[i] = new Address(host_port[0].trim(), Integer.parseInt(host_port[1].trim()));
        }

        NetworkConfig networkConfig = NetworkConfig.builder()
                .host("127.0.0.1")
                .port(port)
                .httpPort(httpPort)
                .nodeName("test")
                .publicKey("kddkdkk")
                .seed(seeds)
                .authentication(new AuthenticationImp())
                .singleton()
                .build();
        NetworkManage networkManage = new NetworkManage(networkConfig);
//        networkManage.start();
        return networkManage;
    }

    @Configuration
    public static class AutoConfig {
        public AutoConfig() {
            System.out.println("AutoConfig ...");
        }
    }
}
