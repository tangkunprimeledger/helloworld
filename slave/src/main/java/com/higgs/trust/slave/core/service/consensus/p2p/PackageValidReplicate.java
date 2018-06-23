/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.service.consensus.p2p;

import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.consensus.p2pvalid.annotation.P2pvalidReplicator;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommit;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidateResult;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.service.pack.PackageService;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.consensus.PersistCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author suimi
 * @date 2018/6/11
 */
@Slf4j @P2pvalidReplicator @Component public class PackageValidReplicate {

    @Autowired private PackageService packageService;

    @Autowired private NodeState nodeState;

    /**
     * majority of this cluster has persisted this command
     *
     * @param commit
     */
    public void receivePersisted(ValidCommit<PersistCommand> commit) {
        // validate param
        BeanValidateResult result = BeanValidator.validate(commit);
        if (!result.isSuccess()) {
            log.error("[P2pReceiver.validated]param persist failed, cause: " + result.getFirstMsg());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        // get validated block p2p and package
        BlockHeader header = commit.operation().get();
        log.info("the persisted p2p result is {}", header);

        doReceive(commit, header);
    }

    /**
     * async start package process, maybe the right package is waiting for consensus
     *
     * @param commit
     * @param header
     */
    private void doReceive(ValidCommit commit, BlockHeader header) {
        if (!nodeState.isState(NodeStateEnum.Running)) {
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_REPLICATE_FAILED);
        }
        try {
            packageService.persisted(header);
        } catch (SlaveException e) {
            if (e.getCode() == SlaveErrorEnum.SLAVE_PACKAGE_HEADER_IS_NULL_ERROR) {
                return;
            }
            throw e;
        }

        commit.close();
    }

}
