package com.higgs.trust.slave.core.network;

import com.higgs.trust.config.crypto.CryptoUtil;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.consensus.config.listener.StateChangeListener;
import com.higgs.trust.network.Address;
import com.higgs.trust.network.NetworkConfig;
import com.higgs.trust.network.NetworkManage;
import com.higgs.trust.slave.core.repository.config.ConfigRepository;
import com.higgs.trust.slave.model.bo.config.Config;
import com.higgs.trust.slave.model.enums.UsageEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author duhongming
 * @date 2018/9/12
 */
@Configuration
@Slf4j
public class NetworkConfiguration {
    @Value("${network.host}")
    public String host;

    @Value("${network.port}")
    public int port;

    @Value("${network.timeout}")
    public int timeout;

    @Value("${network.clientThreadNum}")
    public int clientThreadNum;

    @Value("${network.peers}")
    public String[] peers;

    @Value("${server.port}")
    public int httpPort;

    @Value("${higgs.trust.keys.consensusPublicKey}")
    String pubKeyForConsensus;

    @Value("${higgs.trust.keys.consensusPrivateKey}")
    String priKeyForConsensus;

    @Autowired
    private NodeState nodeState;

    @Autowired
    private NetworkAuthentication authentication;

    @Autowired
    private CryptoUtil cryptoUtil;

    @Autowired
    private ConfigRepository configRepository;

    @StateChangeListener({
            NodeStateEnum.Starting,
            NodeStateEnum.Running,
            NodeStateEnum.Offline,
            NodeStateEnum.ArtificialSync,
            NodeStateEnum.AutoSync,
            NodeStateEnum.SelfChecking,
            NodeStateEnum.StartingConsensus
    })
    public void handleStateChange() {
        if (nodeState.getState() == NodeStateEnum.Offline) {
            log.info("Start to shutdown network because of node state changed to Offline");
//            NetworkManage.getInstance().shutdown();
        } else {
            NetworkManage.getInstance().start();
        }
    }

    @Bean
    public NetworkManage getNetworkManage() {
        log.info("Init NetworkManage bean ...");
        if (peers == null || peers.length == 0) {
            throw new IllegalArgumentException("Network peers is empty");
        }
        Address[] seeds = new Address[peers.length];
        for (int i = 0; i < peers.length; i++) {
            String[] host_port = peers[i].split(":");
            seeds[i] = new Address(host_port[0].trim(), Integer.parseInt(host_port[1].trim()));
        }

        List<Config> configList = configRepository.getConfig(new Config(nodeState.getNodeName(), UsageEnum.CONSENSUS.getCode()));
        String privateKey = configList == null ? priKeyForConsensus : configList.get(0).getPriKey();
        String publicKey = configList == null ? pubKeyForConsensus : configList.get(0).getPubKey();

        NetworkConfig networkConfig = NetworkConfig.builder()
                .host(host)
                .port(port)
                .httpPort(httpPort)
                .nodeName(nodeState.getNodeName())
                .privateKey(privateKey)
                .publicKey(publicKey)
                .seed(seeds)
                .authentication(authentication)
                .timeout(timeout)
                .clientThreadNum(clientThreadNum)
                .singleton()
                .build();
        NetworkManage networkManage = new NetworkManage(networkConfig);
        return networkManage;
    }
}