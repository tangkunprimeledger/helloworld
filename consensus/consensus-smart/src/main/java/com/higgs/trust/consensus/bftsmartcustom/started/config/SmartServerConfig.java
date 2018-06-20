package com.higgs.trust.consensus.bftsmartcustom.started.config;

import bftsmart.reconfiguration.SendRCMessage;
import bftsmart.reconfiguration.util.RSAKeyLoader;
import com.higgs.trust.consensus.bftsmartcustom.started.SpringUtil;
import com.higgs.trust.consensus.bftsmartcustom.started.server.Server;
import com.higgs.trust.consensus.core.ConsensusClient;
import com.higgs.trust.consensus.core.ConsensusStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.security.PublicKey;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

@Configuration
public class SmartServerConfig implements ConsensusStateMachine, ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(SmartServerConfig.class);

    @Value("${bftSmart.systemConfigs.myId}")
    private String myId;

    @Value("${bftSmart.systemConfigs.configs.system.ttp.id}")
    private String ttpId;

    @Autowired
    private RSAKeyLoader rsaKeyLoader;

    @Autowired
    private ConsensusClient consensusClient;

    private String ttpIp;
    private int ttpPort;

    private String ip;
    private int port;

    @Override
    public void leaveConsensus() {
        SmartConfig smartConfig = SpringUtil.getBean(SmartConfig.class);
        String hostsConfig = smartConfig.getHostsConfig();
        String[] configs = hostsConfig.split(",", -1);
        for (String config : configs) {
            StringTokenizer st = new StringTokenizer(config.trim(), " ");
            if (st.countTokens() > 2 && ttpId.equals(st.nextToken())) {
                ttpIp = st.nextToken();
                ttpPort = Integer.valueOf(st.nextToken());
                break;
            }
        }
        SendRCMessage sendRCMessage = new SendRCMessage();
        sendRCMessage.remove(Integer.valueOf(myId));
        sendRCMessage.sendToTTP(ttpIp, ttpPort, Integer.valueOf(ttpId));
    }

    @Override
    public void joinConsensus() {
        SmartConfig smartConfig = SpringUtil.getBean(SmartConfig.class);
        String hostsConfig = smartConfig.getHostsConfig();
        String[] configs = hostsConfig.split(",", -1);
        for (String config : configs) {
            String[] elements = config.trim().replaceAll(" +", " ").split(" ", -1);
            if (myId.equals(elements[0])) {
                ip = elements[1];
                port = Integer.valueOf(elements[2]);
            } else if (ttpId.equals(elements[0])) {
                ttpIp = elements[1];
                ttpPort = Integer.valueOf(elements[2]);
            }
        }
        SendRCMessage sendRCMessage = new SendRCMessage();
        sendRCMessage.add(Integer.valueOf(myId), ip, port);
        sendRCMessage.sendToTTP(ttpIp, ttpPort, Integer.valueOf(ttpId));
    }

    @Override
    public void initStart() {

    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        log.info("smart server starting,myid={}", myId);
        if (!StringUtils.isEmpty(myId)) {
            while (true) {
                try {
                    PublicKey publicKey = rsaKeyLoader.loadPublicKey(Integer.valueOf(myId));
                    if (!Objects.isNull(publicKey)) {
                        break;
                    }
                } catch (Exception e) {
                    log.error("CA还没准备好");
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.info("smart server initializing...");
            new Server(Integer.valueOf(myId));
            log.info("smart server Initialization complete");
            consensusClient.init();
        } else {
            log.info("The myId is not found,myid={}", myId);
            throw new RuntimeException("The myId is not found");
        }
    }
}
