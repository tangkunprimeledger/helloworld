package com.higgs.trust.slave.core.service.datahandler.manage;

import com.higgs.trust.slave.api.enums.MerkleTypeEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.service.snapshot.agent.ManageSnapshotAgent;
import com.higgs.trust.slave.core.service.snapshot.agent.MerkleTreeSnapshotAgent;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import com.higgs.trust.slave.model.bo.manage.RsNode;
import com.higgs.trust.slave.model.enums.biz.RsNodeStatusEnum;
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

    @Override public RsNode getRsNode(String rsId) {
        return manageSnapshotAgent.getRsNode(rsId);
    }

    @Override public void registerRsNode(RegisterRS registerRS) {
        RsNode rsNode = manageSnapshotAgent.registerRs(registerRS);
        merkleTreeSnapshotAgent.addNode(MerkleTypeEnum.RS, rsNode);
    }

    @Override public void updateRsNode(String rsId, RsNodeStatusEnum rsNodeStatusEnum) {
        RsNode rsNode = manageSnapshotAgent.getRsNode(rsId);

        if (null == rsNode) {
            throw new SlaveException(SlaveErrorEnum.SLAVE_UNKNOWN_EXCEPTION);
        }

        rsNode.setStatus(rsNodeStatusEnum);
        manageSnapshotAgent.updateRs(rsNode);
        merkleTreeSnapshotAgent.addNode(MerkleTypeEnum.RS, rsNode);
    }

}
