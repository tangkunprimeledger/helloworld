package com.higgs.trust.consensus.bftsmartcustom.started.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration()
@ConfigurationProperties(prefix = "bftSmart.systemConfigs")
public class SmartConfig {

    private Map<String, String> configs;

    private String hostsConfig;

    private String ttpPubKey;

    private String myId;

    private String myClientId;

    public Map<String, String> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, String> configs) {
        this.configs = configs;
    }

    public String getHostsConfig() {
        return hostsConfig;
    }

    public void setHostsConfig(String hostsConfig) {
        this.hostsConfig = hostsConfig;
    }

    public String getMyId() {
        return myId;
    }

    public void setMyId(String myId) {
        this.myId = myId;
    }

    public String getMyClientId() {
        return myClientId;
    }

    public void setMyClientId(String myClientId) {
        this.myClientId = myClientId;
    }

    public String getTtpPubKey() {
        return ttpPubKey;
    }

    public void setTtpPubKey(String ttpPubKey) {
        this.ttpPubKey = ttpPubKey;
    }

    public SmartConfig() {
        System.out.println("----------------------- smart config---------------------");
    }
}
