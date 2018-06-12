/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.master;

import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.config.master.command.ChangeMasterVerify;
import com.higgs.trust.config.master.command.ChangeMasterVerifyCmd;
import com.higgs.trust.config.master.command.ChangeMasterVerifyResponse;
import com.higgs.trust.config.master.command.ChangeMasterVerifyResponseCmd;
import com.higgs.trust.config.term.TermManager;
import com.higgs.trust.consensus.p2pvalid.annotation.P2pvalidReplicator;
import com.higgs.trust.config.node.NodeState;

import com.higgs.trust.consensus.p2pvalid.core.ValidSyncCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author suimi
 * @date 2018/6/11
 */
@P2pvalidReplicator @Component public class ChangeMasterReplicate {

    @Autowired NodeState nodeState;

    @Autowired TermManager termManager;

    @Autowired INodeInfoService nodeInfoService;

    /**
     * handle the consensus result of validating block header
     *
     * @param commit
     */
    public ChangeMasterVerifyResponseCmd handleChangeMasterVerify(ValidSyncCommit<ChangeMasterVerifyCmd> commit) {
        ChangeMasterVerifyCmd operation = commit.operation();
        ChangeMasterVerify verify = operation.get();
        boolean changeMaster = false;
        if (!termManager.getMasterHeartbeat().get() && verify.getTerm() == nodeState.getCurrentTerm() + 1) {
            Long maxHeight = nodeInfoService.packageHeight();
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
