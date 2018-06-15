package com.higgs.trust.slave.common.config;

import com.higgs.trust.config.node.NodeState;
import com.higgs.trust.config.node.NodeStateEnum;
import com.higgs.trust.config.node.listener.StateChangeListener;
import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.slave.core.repository.ca.CaRepository;
import com.higgs.trust.slave.core.repository.config.ClusterConfigRepository;
import com.higgs.trust.slave.core.repository.config.ClusterNodeRepository;
import com.higgs.trust.slave.core.repository.config.ConfigRepository;
import com.higgs.trust.slave.model.bo.ca.Ca;
import com.higgs.trust.slave.model.bo.config.ClusterConfig;
import com.higgs.trust.slave.model.bo.config.ClusterNode;
import com.higgs.trust.slave.model.bo.config.Config;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jerry
 */
@Configurable
@ConfigurationProperties(prefix = "higgs.trust.p2p")
public class ClusterNodesInfo implements ClusterInfo {

    @Getter @Setter
    private int faultNodeNum = 0;

    @Getter
    private Map<String, String> clusters = new ConcurrentHashMap<>();

    @Getter @Setter
    private List<String> clusterNodeNames = new ArrayList<>();

    @Autowired private ClusterConfigRepository clusterConfigRepository;
    @Autowired private ClusterNodeRepository clusterNodeRepository;
    @Autowired private ConfigRepository configRepository;
    @Autowired private CaRepository caRepository;
    @Autowired private NodeState nodeState;

    public void refresh() {
        ClusterConfig clusterConfig = clusterConfigRepository.getClusterConfig(nodeState.getNodeName());
        faultNodeNum = clusterConfig == null ? 0 : clusterConfig.getFaultNum();
        Config config = configRepository.getConfig(nodeState.getNodeName());
        nodeState.setPrivateKey(null != config ? config.getPriKey() : null);
        refreshPubkeys();
    }

    /**
     * get cluster nodeNames
     *
     * @return
     */
    public void refreshPubkeys() {
        Map<String, String> clusterPubkeys = new HashMap<>();
        List<ClusterNode> list = clusterNodeRepository.getAllClusterNodes();
        list.forEach(clusterNode -> {
            if (clusterNode.isP2pStatus()) {
                Ca ca = caRepository.getCa(clusterNode.getNodeName());
                if (ca != null) {
                    clusterPubkeys.put(clusterNode.getNodeName(), ca.getPubKey());
                }
            }
        });
        synchronized (clusters) {
            clusters.clear();
            clusters.putAll(clusterPubkeys);
            clusterNodeNames.clear();
            clusterNodeNames.addAll(clusters.keySet());
        }
    }

    @Override public Integer faultNodeNum() {
        return faultNodeNum;
    }

    @Override public String nodeName() {
        return nodeState.getNodeName();
    }

    @Override public List<String> clusterNodeNames() {
        return clusterNodeNames;
    }

    @Override public String pubKey(String nodeName) {
        return clusters.get(nodeName);
    }

    @Override public String privateKey() {
        return nodeState.getPrivateKey();
    }
}
