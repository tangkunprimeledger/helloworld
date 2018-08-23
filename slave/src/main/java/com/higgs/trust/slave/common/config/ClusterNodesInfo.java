package com.higgs.trust.slave.common.config;

import com.higgs.trust.config.p2p.AbstractClusterInfo;
import com.higgs.trust.config.p2p.ClusterInfoVo;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.slave.core.repository.ca.CaRepository;
import com.higgs.trust.slave.core.repository.config.ClusterConfigRepository;
import com.higgs.trust.slave.core.repository.config.ClusterNodeRepository;
import com.higgs.trust.slave.core.repository.config.ConfigRepository;
import com.higgs.trust.slave.model.bo.ca.Ca;
import com.higgs.trust.slave.model.bo.config.ClusterConfig;
import com.higgs.trust.slave.model.bo.config.ClusterNode;
import com.higgs.trust.slave.model.bo.config.Config;
import com.higgs.trust.slave.model.enums.UsageEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author WangQuanzhou
 * @desc cluster node info and refresh method
 * @date 2018/6/28 20:01
 */
@Slf4j @Configuration @Primary @ConfigurationProperties(prefix = "higgs.trust.p2p") public class ClusterNodesInfo
    extends AbstractClusterInfo {

    @Getter @Setter private int faultNodeNum = 0;

    @Getter private Map<String, String> clusters = new ConcurrentHashMap<>();

    @Getter private Map<String, String> clustersForConsensus = new ConcurrentHashMap<>();

    @Getter @Setter private List<String> clusterNodeNames = new ArrayList<>();

    @Autowired private ClusterConfigRepository clusterConfigRepository;
    @Autowired private ClusterNodeRepository clusterNodeRepository;
    @Autowired private ConfigRepository configRepository;
    @Autowired private CaRepository caRepository;
    @Autowired private NodeState nodeState;

    @Override public Integer faultNodeNum() {
        return faultNodeNum;
    }

    @Override public String nodeName() {
        return nodeState.getNodeName();
    }

    @Override public List<String> clusterNodeNames() {
        if (clusterNodeNames.isEmpty()) {
            clusterNodeNames.addAll(clusters.keySet());
        }
        return clusterNodeNames;
    }

    @Override public String pubKeyForConsensus(String nodeName) {
        return clustersForConsensus.get(nodeName);
    }

    /**
     * get public key create the given nodeName
     *
     * @param nodeName
     * @return
     */
    @Override public String pubKeyForBiz(String nodeName) {
        return clusters.get(nodeName);
    }

    @Override public String priKeyForConsensus() {
        return nodeState.getConsensusPrivateKey();
    }

    /**
     * get the self private key
     *
     * @return
     */
    @Override public String priKeyForBiz() {
        return nodeState.getPrivateKey();
    }

    @Override public void init(ClusterInfoVo vo) {
        faultNodeNum = vo.getFaultNodeNum();
        clusters.clear();
        clusters.putAll(vo.getClusters());
        clusterNodeNames.clear();
        clusterNodeNames.addAll(clusters.keySet());
        log.info("[init] refresh keypair for consensus layer");
        refreshConsensus();
    }

    @Override public void refreshConsensus() {
        List<Ca> list = caRepository.getAllCa();
        clustersForConsensus.clear();
        for (Ca ca : list) {
            if (ca.getUsage().equals(UsageEnum.CONSENSUS.getCode())) {
                log.info("add pubKey for user={},pubKey={}", ca.getUser(), ca.getPubKey());
                clustersForConsensus.put(ca.getUser(), ca.getPubKey());
            }
        }
        List<Config> configList =
            configRepository.getConfig(new Config(nodeState.getNodeName(), UsageEnum.CONSENSUS.getCode()));
        if (log.isDebugEnabled()) {
            log.debug("configList={}", configList);
        }
        nodeState.setConsensusPrivateKey(configList.get(0).getPriKey());
    }

    @Override public void refresh() {
        log.info("refresh cluster info");
        log.info("[refresh] before refresh, cluster nodes = {}", clusterNodeNames);
        ClusterConfig clusterConfig = clusterConfigRepository.getClusterConfig(nodeState.getClusterName());
        faultNodeNum = clusterConfig == null ? 0 : clusterConfig.getFaultNum();
        Config config = configRepository.getBizConfig(nodeState.getNodeName());
        nodeState.setPrivateKey(null != config ? config.getPriKey() : null);
        refreshPubkeys();

        log.info("refresh keypair for consensus layer");
        refreshConsensus();

        log.info("[refresh] end refresh, cluster nodes = {}", clusterNodeNames);
    }

    /**
     * get cluster nodeNames
     *
     * @return
     */
    public void refreshPubkeys() {
        Map<String, String> clusterPubkeys = new HashMap<>();
        List<ClusterNode> list = clusterNodeRepository.getAllClusterNodes();
        if (list != null) {
            list.forEach(clusterNode -> {
                if (clusterNode.isP2pStatus()) {
                    Ca ca = caRepository.getCaForBiz(clusterNode.getNodeName());
                    if (ca != null) {
                        clusterPubkeys.put(clusterNode.getNodeName(), ca.getPubKey());
                    }
                }
            });
        }
        synchronized (clusters) {
            clusters.clear();
            clusters.putAll(clusterPubkeys);
            clusterNodeNames.clear();
            clusterNodeNames.addAll(clusters.keySet());
        }
    }
}