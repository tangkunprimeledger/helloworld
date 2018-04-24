package com.higgs.trust.slave.core.service.datahandler.manage;

import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.core.service.snapshot.agent.ManageSnapshotAgent;
import com.higgs.trust.slave.core.service.snapshot.agent.MerkleTreeSnapshotAgent;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import com.higgs.trust.slave.model.bo.manage.RsPubKey;
import com.higgs.trust.slave.model.bo.merkle.MerkleTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author tangfashuang
 * @date 2018/04/17 19:25
 * @desc rs snapshot handler
 */
@Service
public class RsSnapshotHandler implements RsHandler {
    @Autowired
    private ManageSnapshotAgent manageSnapshotAgent;

    @Autowired
    private MerkleTreeSnapshotAgent merkleTreeSnapshotAgent;

    @Override public RsPubKey getRsPubKey(String rsId) {
        return manageSnapshotAgent.getRsPubKey(rsId);
    }

    @Override public void registerRsPubKey(RegisterRS registerRS) {
        RsPubKey rsPubKey = manageSnapshotAgent.registerRs(registerRS);

        MerkleTree merkleTree = merkleTreeSnapshotAgent.getMerkleTree(MerkleTypeEnum.RS);

        if (null == merkleTree) {
            merkleTreeSnapshotAgent.buildMerleTree(MerkleTypeEnum.RS, new Object[] {rsPubKey});
        } else {
            merkleTreeSnapshotAgent.appendChild(merkleTree, rsPubKey);
        }
    }
}
