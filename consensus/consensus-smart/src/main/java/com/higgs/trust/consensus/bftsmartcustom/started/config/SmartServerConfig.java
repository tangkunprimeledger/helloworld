package com.higgs.trust.consensus.bftsmartcustom.started.config;

import bftsmart.reconfiguration.SendRCMessage;
import bftsmart.reconfiguration.util.RSAKeyLoader;
import com.higgs.trust.consensus.bftsmartcustom.started.SpringUtil;
import com.higgs.trust.consensus.bftsmartcustom.started.server.Server;
import com.higgs.trust.consensus.core.ConsensusStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.util.StringUtils;

import java.security.PublicKey;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

@Configuration
@ConditionalOnExpression("'${bftSmart.systemConfigs.myId}' != ''")
public class SmartServerConfig implements ConsensusStateMachine {

    private static final Logger log = LoggerFactory.getLogger(SmartServerConfig.class);

    @Value("${bftSmart.systemConfigs.myId}")
    private String myId;

    @Value("${bftSmart.systemConfigs.configs.system.ttp.id}")
    private String ttpId;

    private String ttpIp;
    private int ttpPort;

    private String ip;
    private int port;

    @Bean("server")
    @DependsOn("springUtil")
    public Server getServer() {
        log.info("smart server starting,myid={}", myId);
        if (!StringUtils.isEmpty(myId)) {
            RSAKeyLoader rsaKeyLoader = new RSAKeyLoader(Integer.valueOf(myId), "", false);
            while (true) {
                try {
                    PublicKey publicKey = rsaKeyLoader.loadPublicKey(Integer.valueOf(myId));
                    if (!Objects.isNull(publicKey)) {
                        break;
                    }
                } catch (Exception e) {
                    log.debug("CA还未准备好");
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return new Server(Integer.valueOf(myId));
        } else {
            log.info("The myId is not found,myid={}", myId);
            throw new RuntimeException("The myId is not found");
        }
    }

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

}