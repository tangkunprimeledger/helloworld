package com.higgs.trust.slave.core.service.datahandler.ca;

import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.core.service.snapshot.agent.CaSnapshotAgent;
import com.higgs.trust.slave.core.service.snapshot.agent.MerkleTreeSnapshotAgent;
import com.higgs.trust.slave.model.bo.ca.Ca;
import com.higgs.trust.slave.model.bo.config.ClusterConfig;
import com.higgs.trust.slave.model.bo.config.ClusterNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/6 10:54
 */
@Service public class CaSnapshotHandler implements CaHandler {

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
        // operation merkle tree
        merkleTreeSnapshotAgent.buildMerleTree(MerkleTypeEnum.CA, new Object[] {ca});

        caSnapshotAgent.updateCa(ca);

        ClusterNode clusterNode = new ClusterNode();
        clusterNode.setNodeName(ca.getUser());
        clusterNode.setP2pStatus(false);
        clusterNode.setRsStatus(false);
        caSnapshotAgent.updateClusterNode(clusterNode);

        ClusterConfig oldClusterConfig = caSnapshotAgent.getClusterConfig("TRUST");
        ClusterConfig clusterConfig = new ClusterConfig();
        // TODO 集群名称应该从配置动态读取
        clusterConfig.setClusterName(oldClusterConfig.getClusterName());
        clusterConfig.setNodeNum(oldClusterConfig.getNodeNum() - 1);
        clusterConfig.setFaultNum((clusterConfig.getNodeNum() - 1) / 3);
        caSnapshotAgent.updateClusterConfig(clusterConfig);
    }

    /**
     * @param nodeName
     * @return Ca
     * @desc get CA information by nodeName
     */
    @Override public Ca getCa(String nodeName) {
        return caSnapshotAgent.getCa(nodeName);
    }
}
