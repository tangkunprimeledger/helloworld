package com.higgs.trust.slave.core.service.datahandler.ca;

import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.core.service.snapshot.agent.CaSnapshotAgent;
import com.higgs.trust.slave.core.service.snapshot.agent.MerkleTreeSnapshotAgent;
import com.higgs.trust.slave.dao.po.ca.CaPO;
import com.higgs.trust.slave.dao.po.config.ClusterConfigPO;
import com.higgs.trust.slave.model.bo.ca.Ca;
import com.higgs.trust.slave.model.bo.config.ClusterConfig;
import com.higgs.trust.slave.model.bo.config.ClusterNode;
import com.higgs.trust.slave.model.bo.merkle.MerkleTree;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/6 10:54
 */
@Service @Slf4j public class CaSnapshotHandler implements CaHandler {

    @Autowired private CaSnapshotAgent caSnapshotAgent;
    @Autowired MerkleTreeSnapshotAgent merkleTreeSnapshotAgent;
    @Autowired NodeState nodeState;

    /**
     * @param ca
     * @return
     * @desc insert CA into cache
     */
    @Override public void authCa(Ca ca) {
        // operate merkle tree
        check(ca);
        if (null != caSnapshotAgent.getCa(ca.getUser())) {
            caSnapshotAgent.updateCa(ca);
        } else {
            caSnapshotAgent.saveCa(ca);
        }

        /*ClusterNode clusterNode = new ClusterNode();
        clusterNode.setNodeName(ca.getUser());
        clusterNode.setP2pStatus(true);
        clusterNode.setRsStatus(false);
        if (null == caSnapshotAgent.getClusterNode(ca.getUser())) {
            caSnapshotAgent.saveClusterNode(clusterNode);
        } else {
            caSnapshotAgent.updateClusterNode(clusterNode);
        }

        ClusterConfigPO clusterConfigPO = caSnapshotAgent.getClusterConfig(nodeState.getClusterName());
        ClusterConfig clusterConfig = new ClusterConfig();
        clusterConfig.setClusterName(clusterConfigPO.getClusterName());
        clusterConfig.setNodeNum(clusterConfigPO.getNodeNum() + 1);
        clusterConfig.setFaultNum(clusterConfigPO.getNodeNum() / 3);
        caSnapshotAgent.updateClusterConfig(clusterConfig);*/

    }

    /**
     * @param ca
     * @return
     * @desc update CA information
     */
    @Override public void updateCa(Ca ca) {
        // operate merkle tree
        check(ca);

        caSnapshotAgent.updateCa(ca);
    }

    /**
     * @param ca
     * @return
     * @desc cancel CA information
     */
    @Override public void cancelCa(Ca ca) {
        log.info("[cancelCa] start to cancel CA, user={}", ca.getUser());

        // operate merkle tree
        check(ca);

        log.info("[cancelCa] start to update CA to invalid, user={}", ca.getUser());
        caSnapshotAgent.updateCa(ca);

        /*ClusterNode clusterNode = new ClusterNode();
        clusterNode.setNodeName(ca.getUser());
        clusterNode.setP2pStatus(false);
        clusterNode.setRsStatus(false);
        log.info("[cancelCa] start to update clusterNodeInfo, user={}", ca.getUser());
        caSnapshotAgent.updateClusterNode(clusterNode);

        ClusterConfigPO clusterConfigPO = caSnapshotAgent.getClusterConfig(nodeState.getClusterName());
        ClusterConfig clusterConfig = new ClusterConfig();
        clusterConfig.setClusterName(clusterConfigPO.getClusterName());
        clusterConfig.setNodeNum(clusterConfigPO.getNodeNum() - 1);
        clusterConfig.setFaultNum((clusterConfig.getNodeNum() - 1) / 3);
        log.info("[cancelCa] start to update clusterConfigInfo, user={}", ca.getUser());
        caSnapshotAgent.updateClusterConfig(clusterConfig);*/
    }

    /**
     * @param nodeName
     * @return Ca
     * @desc get CA information by nodeName
     */
    @Override public CaPO getCa(String nodeName) {
        return caSnapshotAgent.getCa(nodeName);
    }

    private void check(Ca ca) {
        merkleTreeSnapshotAgent.addNode(MerkleTypeEnum.CA, ca);
    }
}
