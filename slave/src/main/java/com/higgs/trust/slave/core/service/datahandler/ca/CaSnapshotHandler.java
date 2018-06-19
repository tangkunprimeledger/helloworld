package com.higgs.trust.slave.core.service.datahandler.ca;

import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.core.service.snapshot.agent.CaSnapshotAgent;
import com.higgs.trust.slave.core.service.snapshot.agent.MerkleTreeSnapshotAgent;
import com.higgs.trust.slave.dao.po.ca.CaPO;
import com.higgs.trust.slave.model.bo.ca.Ca;
import com.higgs.trust.slave.model.bo.config.ClusterConfig;
import com.higgs.trust.slave.model.bo.config.ClusterNode;
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

    /**
     * @param ca
     * @return
     * @desc insert CA into cache
     */
    @Override public void authCa(Ca ca) {
        // operation merkle tree
        merkleTreeSnapshotAgent.buildMerleTree(MerkleTypeEnum.CA, new Object[] {ca});

        caSnapshotAgent.saveCa(ca);

        ClusterNode clusterNode = new ClusterNode();
        clusterNode.setNodeName(ca.getUser());
        clusterNode.setP2pStatus(true);
        clusterNode.setRsStatus(false);
        caSnapshotAgent.saveClusterNode(clusterNode);

        ClusterConfig oldClusterConfig = caSnapshotAgent.getClusterConfig("TRUST");
        ClusterConfig clusterConfig = new ClusterConfig();
        // TODO 集群名称应该从配置动态读取
        clusterConfig.setClusterName(oldClusterConfig.getClusterName());
        clusterConfig.setNodeNum(oldClusterConfig.getNodeNum() + 1);
        clusterConfig.setFaultNum(oldClusterConfig.getNodeNum() / 3);
        caSnapshotAgent.updateClusterConfig(clusterConfig);

    }

    /**
     * @param ca
     * @return
     * @desc update CA information
     */
    @Override public void updateCa(Ca ca) {
        merkleTreeSnapshotAgent.buildMerleTree(MerkleTypeEnum.CA, new Object[] {ca});

        caSnapshotAgent.updateCa(ca);

    }

    /**
     * @param ca
     * @return
     * @desc cancel CA information
     */
    @Override public void cancelCa(Ca ca) {
        log.info("[cancelCa] start to cancel CA, user={}",ca.getUser());

        // operation merkle tree
        merkleTreeSnapshotAgent.buildMerleTree(MerkleTypeEnum.CA, new Object[] {ca});

        log.info("[cancelCa] start to update CA to invalid, user={}",ca.getUser());
        caSnapshotAgent.updateCa(ca);

        ClusterNode clusterNode = new ClusterNode();
        clusterNode.setNodeName(ca.getUser());
        clusterNode.setP2pStatus(false);
        clusterNode.setRsStatus(false);
        log.info("[cancelCa] start to update clusterNodeInfo, user={}",ca.getUser());
        caSnapshotAgent.updateClusterNode(clusterNode);

        ClusterConfig oldClusterConfig = caSnapshotAgent.getClusterConfig("TRUST");
        ClusterConfig clusterConfig = new ClusterConfig();
        // TODO 集群名称应该从配置动态读取
        clusterConfig.setClusterName(oldClusterConfig.getClusterName());
        clusterConfig.setNodeNum(oldClusterConfig.getNodeNum() - 1);
        clusterConfig.setFaultNum((clusterConfig.getNodeNum() - 1) / 3);
        log.info("[cancelCa] start to update clusterConfigInfo, user={}",ca.getUser());
        caSnapshotAgent.updateClusterConfig(clusterConfig);
    }

    /**
     * @param nodeName
     * @return Ca
     * @desc get CA information by nodeName
     */
    @Override public CaPO getCa(String nodeName) {
        return caSnapshotAgent.getCa(nodeName);
    }
}
