package com.higgs.trust.slave.core.service.datahandler.ca;

import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.core.service.snapshot.agent.CaSnapshotAgent;
import com.higgs.trust.slave.core.service.snapshot.agent.ClusterConfigAgent;
import com.higgs.trust.slave.core.service.snapshot.agent.ClusterNodeAgent;
import com.higgs.trust.slave.core.service.snapshot.agent.MerkleTreeSnapshotAgent;
import com.higgs.trust.slave.model.bo.ca.Ca;
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
    @Autowired ClusterConfigAgent clusterConfigAgent;
    @Autowired ClusterNodeAgent clusterNodeAgent;

    /**
     * @param ca
     * @return
     * @desc insert CA into db
     */
    @Override public void authCa(Ca ca) {
        // operation merkle tree
        merkleTreeSnapshotAgent.buildMerleTree(MerkleTypeEnum.CA, new Object[] {ca});

        caSnapshotAgent.saveCa(ca);

        // TODO 更新clusterConfig和clusterNode信息
    }

    /**
     * @param ca
     * @return
     * @desc update CA information
     */
    @Override public void updateCa(Ca ca) {
        // TODO 要怎么操作数据库进行clusterConfig和clusterNode信息的变更
        merkleTreeSnapshotAgent.buildMerleTree(MerkleTypeEnum.CA, new Object[] {ca});
    }

    /**
     * @param ca
     * @return
     * @desc cancel CA information
     */
    @Override public void cancelCa(Ca ca) {
        // operation merkle tree
        merkleTreeSnapshotAgent.buildMerleTree(MerkleTypeEnum.CA, new Object[] {ca});
        // TODO 要怎么操作数据库进行clusterConfig和clusterNode信息的变更
        caSnapshotAgent.saveCa(ca);
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
