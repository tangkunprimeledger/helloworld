package com.higgs.trust.slave.core.service.consensus.p2p;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.higgs.trust.consensus.p2pvalid.core.spi.ClusterInfo;
import com.higgs.trust.slave.common.config.PropertiesConfig;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.repository.RsNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Description: the p2p cluster info maintained by slave
 * @author: pengdi
 **/
@Service public class P2pClusterInfo implements ClusterInfo {

    private Map<String, String> p2pCluster;

    private Integer faultNodeNum;

    @Autowired NodeState nodeState;

    @Autowired RsNodeRepository rsNodeRepository;

    @Autowired P2pClusterInfo(PropertiesConfig propertiesConfig) {
        p2pCluster = JSON.parseObject(propertiesConfig.getP2pClusterJson(), new TypeReference<Map<String, String>>() {
        });
        faultNodeNum = propertiesConfig.getP2pFaultNodeNum();
    }

    /**
     * get faultNode num
     *
     * @return
     */
    @Override public Integer faultNodeNum() {
        return faultNodeNum;
    }

    @Override public String myNodeName() {
        return nodeState.getNodeName();
    }

    @Override public List<String> clusterNodeNames() {
        return Lists.newArrayList(p2pCluster.keySet());
    }

    @Override public String pubKey(String nodeName) {
        return p2pCluster.get(nodeName);
    }

    @Override public String privateKey() {
        return nodeState.getPrivateKey();
    }
}
