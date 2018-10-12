package com.higgs.trust.consensus.bftsmartcustom.started.custom;

import bftsmart.reconfiguration.util.RSAKeyLoader;
import com.higgs.trust.consensus.bftsmartcustom.started.custom.client.Client;
import com.higgs.trust.consensus.bftsmartcustom.started.custom.config.SmartConfig;
import com.higgs.trust.consensus.bftsmartcustom.started.custom.server.Server;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.consensus.config.listener.StateChangeListener;
import com.higgs.trust.consensus.config.listener.StateListener;
import com.higgs.trust.consensus.core.ConsensusStateMachine;
import com.higgs.trust.consensus.core.IConsensusSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import java.security.PublicKey;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

@StateListener
@Configuration public class SmartStart implements ConsensusStateMachine {

    private static final Logger log = LoggerFactory.getLogger(SmartStart.class);

    @Value("${bftSmart.systemConfigs.myId}") private String myId;

    @Value("${bftSmart.systemConfigs.configs.system.ttp.id}") private String ttpId;

    @Value("${higgs.trust.nodeName}") private String nodeName;

    @Autowired private RSAKeyLoader rsaKeyLoader;

    @Autowired private Client client;

    @Autowired private SmartConfig smartConfig;

    @Autowired private IConsensusSnapshot consensusSnapshot;

    @Autowired private SmartCommitReplicateComposite machine;

    @Autowired private NumberNameMapping numberNameMapping;

    private Server server;

    private String ttpIp;
    private int ttpPort;

    private String ip;
    private int port;

    @Override public void leaveConsensus() {
        log.info("leave replica :" + myId);
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
        sendRCMessage.remove(Integer.valueOf(myId), nodeName);
        sendRCMessage.sendToTTP(ttpIp, ttpPort, Integer.valueOf(ttpId), rsaKeyLoader);
    }

    @Override public void joinConsensus() {
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
        sendRCMessage.add(Integer.valueOf(myId), ip, port, nodeName);
        sendRCMessage.sendToTTP(ttpIp, ttpPort, Integer.valueOf(ttpId), rsaKeyLoader);
    }

    @Override @StateChangeListener(NodeStateEnum.StartingConsensus) @Order public synchronized void start() {
        log.info("smart server starting,myid={}", myId);
        if (server != null) {
            log.warn("The service has started");
            return;
        }
        if (!StringUtils.isEmpty(myId)) {

            Map<String, String> map = smartConfig.getIdNodeNameMap();
            if (map != null) {
                boolean addRet = numberNameMapping.addMapping(map);
                if (addRet) {
                    log.info("number-name mapping add success,{}", map.toString());
                } else {
                    log.info("number-name mapping add fail,{}", map.toString());
                }
            } else {
                log.warn("can not read number-name mapping config");
                return;
            }
            while (true) {
                try {
                    PublicKey publicKey = rsaKeyLoader.loadPublicKey(Integer.valueOf(myId));
                    if (!Objects.isNull(publicKey)) {
                        break;
                    }
                } catch (Exception e) {
                    log.error("CA还没准备好", e);
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(2000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
            log.info("smart server initializing...");

            server = new Server(Integer.valueOf(myId), consensusSnapshot, machine);
            log.info("smart server Initialization complete");

            while (true) {
                if (server.getServiceReplica().getServerCommunicationSystem().getServersConn().getConnections().size()
                    >= (server.getServiceReplica().getReplicaContext().getStaticConfiguration().getN() - 1)) {
                    client.init();
                    break;
                } else {
                    log.info("connection count : " + server.getServiceReplica().getServerCommunicationSystem()
                        .getServersConn().getConnections().size());
                    log.info(
                        "view N : " + server.getServiceReplica().getReplicaContext().getStaticConfiguration().getN());
                    log.warn("server connection fail");
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(500L);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        } else {
            log.info("The myId is not found,myid={}", myId);
            throw new RuntimeException("The myId is not found");
        }
    }

}
