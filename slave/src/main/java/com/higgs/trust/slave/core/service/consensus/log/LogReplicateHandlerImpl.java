package com.higgs.trust.slave.core.service.consensus.log;


import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.consensus.config.NodeProperties;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.core.ConsensusClient;
import com.higgs.trust.slave.api.vo.PackageVO;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidateResult;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.service.pack.PackageProcess;
import com.higgs.trust.slave.core.service.pack.PackageService;
import com.higgs.trust.slave.model.bo.consensus.PackageCommand;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Description: replicate the sorted package to cluster
 * @author: pengdi
 **/

@Slf4j @Service public class LogReplicateHandlerImpl implements LogReplicateHandler {
    /**
     * client from the log replicate consensus layer
     */
    @Autowired ConsensusClient consensusClient;

    @Autowired PackageService packageService;

    @Autowired ExecutorService packageThreadPool;

    @Autowired PackageProcess packageProcess;

    @Autowired NodeState nodeState;

    @Autowired NodeProperties properties;

    /**
     * replicate sorted package to the cluster
     *
     * @param packageVO
     */
    @Override public void replicatePackage(PackageVO packageVO) {
        // validate param
        if (null == packageVO) {
            log.error("[LogReplicateHandler.replicatePackage]param validate failed, cause package is null ");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        // replicate package to all nodes
        log.info("package starts to distribute to each node through consensus layer package height = {}", packageVO.getHeight());
        PackageCommand packageCommand =
            new PackageCommand(nodeState.getCurrentTerm(), nodeState.getMasterName(), packageVO);
        String signValue = packageCommand.getSignValue();
        packageCommand.setSign(SignUtils.sign(signValue, nodeState.getPrivateKey()));

        boolean flag = false;
        while (!flag) {
            if (!nodeState.isMaster()) {
                return;
            }
            CompletableFuture future = consensusClient.submit(packageCommand);
            try {
                future.get(properties.getConsensusWaitTime(), TimeUnit.MILLISECONDS);
                flag = true;
            } catch (Throwable e) {
                log.error("replicate log failed!", e);
                //TODO 添加告警
            }
        }

        log.info("package has been sent to consensus layer package height = {}", packageVO.getHeight());
    }


}
