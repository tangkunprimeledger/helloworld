package com.higgs.trust.slave.core.service.datahandler.ca;

import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.core.service.snapshot.agent.CaSnapshotAgent;
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

    /**
     * @param ca
     * @return
     * @desc insert CA into db
     */
    @Override public void saveCa(Ca ca) {

        // operation merkle tree
        merkleTreeSnapshotAgent.buildMerleTree(MerkleTypeEnum.CA, new Object[] {ca});

        caSnapshotAgent.saveCa(ca);
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
        return caSnapshotAgent.getCa(nodeName);
    }
}
