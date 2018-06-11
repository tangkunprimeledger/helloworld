/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.managment.master;

import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.consensus.p2pvalid.annotation.P2pvalidReplicator;
import com.higgs.trust.consensus.p2pvalid.core.ValidSyncCommit;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.repository.PackageRepository;
import com.higgs.trust.slave.model.bo.consensus.master.ChangeMasterVerify;
import com.higgs.trust.slave.model.bo.consensus.master.ChangeMasterVerifyCmd;
import com.higgs.trust.slave.model.bo.consensus.master.ChangeMasterVerifyResponse;
import com.higgs.trust.slave.model.bo.consensus.master.ChangeMasterVerifyResponseCmd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author suimi
 * @date 2018/6/11
 */
@P2pvalidReplicator @Component public class ChangeMasterReplicate {

    @Autowired NodeState nodeState;

    @Autowired PackageRepository packageRepository;

    /**
     * handle the consensus result of validating block header
     *
     * @param commit
     */
    public ChangeMasterVerifyResponseCmd handleChangeMasterVerify(ValidSyncCommit<ChangeMasterVerifyCmd> commit) {
        ChangeMasterVerifyCmd operation = commit.operation();
        ChangeMasterVerify verify = operation.get();
        boolean changeMaster = false;
        if (!nodeState.getMasterHeartbeat().get() && verify.getTerm() == nodeState.getCurrentTerm() + 1) {
            Long maxHeight = packageRepository.getMaxHeight();
            maxHeight = maxHeight == null ? 0 : maxHeight;
            if (verify.getPackageHeight() >= maxHeight) {
                changeMaster = true;
            }
        }
        ChangeMasterVerifyResponse response =
            new ChangeMasterVerifyResponse(verify.getTerm(), nodeState.getNodeName(), verify.getProposer(),
                verify.getPackageHeight(), changeMaster);
        String sign = SignUtils.sign(response.getSignValue(), nodeState.getPrivateKey());
        response.setSign(sign);
        return new ChangeMasterVerifyResponseCmd(operation.messageDigest(), response);
    }
}
