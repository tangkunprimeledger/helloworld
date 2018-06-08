package com.higgs.trust.slave.core.service.datahandler.ca;

import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.core.repository.ca.CaRepository;
import com.higgs.trust.slave.core.repository.config.ClusterConfigRepository;
import com.higgs.trust.slave.core.repository.config.ClusterNodeRepository;
import com.higgs.trust.slave.core.service.merkle.MerkleService;
import com.higgs.trust.slave.core.service.snapshot.agent.MerkleTreeSnapshotAgent;
import com.higgs.trust.slave.model.bo.ca.Ca;
import com.higgs.trust.slave.model.bo.config.ClusterConfig;
import com.higgs.trust.slave.model.bo.config.ClusterNode;
import com.higgs.trust.slave.model.bo.merkle.MerkleTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/6 10:54
 */
@Service public class CaDBHandler implements CaHandler {
    @Autowired private CaRepository caRepository;
    @Autowired private ClusterNodeRepository clusterNodeRepository;
    @Autowired private ClusterConfigRepository clusterConfigRepository;
    @Autowired MerkleTreeSnapshotAgent merkleTreeSnapshotAgent;
    @Autowired MerkleService merkleService;

    /**
     * @param ca
     * @return
     * @desc insert CA into db
     */
    @Override public void saveCa(Ca ca) {

        //TODO  下面的DB操作应该放在一个事务内完成
        // insert ca information
        caRepository.insertCa(ca);

        // insert cluster_node
        insertClusterNode(ca);

        // update cluster_config
        // TODO 如何实时发计算出容错节点书F
        updateClusterConfig();

        // operation merkle tree
        MerkleTree merkleTree = merkleService.build(MerkleTypeEnum.CA, Arrays.asList(new Object[] {ca}));
        //flush
        merkleService.flush(merkleTree);

    }

    /**
     * @param ca
     * @return
     * @desc update CA information
     */
    @Override public void updateCa(Ca ca) {

    }

    /**
     * @param nodeName
     * @return Ca
     * @desc get CA information by nodeName
     */
    @Override public Ca getCa(String nodeName) {
        return caRepository.getCa(nodeName);
    }

    private void insertClusterNode(Ca ca) {
        ClusterNode clusterNode = new ClusterNode();
        clusterNode.setRsStatus("FALSE");
        clusterNode.setRsStatus("TRUE");
        clusterNode.setNodeName(ca.getUser());
        clusterNodeRepository.insertClusterNode(clusterNode);
    }

    private void updateClusterConfig() {
        ClusterConfig clusterConfig = new ClusterConfig();
        // TODO 如何获得集群的名称？？
        clusterConfig.setClusterName("TRUST");
        clusterConfig.setNodeNum(acquireNodeNum());
        clusterConfig.setFaultNum(calculateFaultNum());
        clusterConfigRepository.updateClusterConfig(clusterConfig);
    }

    private int calculateFaultNum() {
        int totalNodeNum = clusterNodeRepository.getNodeNum();
        return (totalNodeNum - 1) / 3;
    }

    private int acquireNodeNum() {
        return clusterNodeRepository.getNodeNum();
    }
}
