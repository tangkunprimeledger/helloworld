package com.higgs.trust.slave.core.service.consensus.log;


import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.consensus.core.ConsensusClient;
import com.higgs.trust.slave.api.vo.PackageVO;
import com.higgs.trust.slave.common.config.NodeProperties;
import com.higgs.trust.slave.common.config.PropertiesConfig;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidateResult;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.repository.RsPubKeyRepository;
import com.higgs.trust.slave.core.service.pack.PackageProcess;
import com.higgs.trust.slave.core.service.pack.PackageService;
import com.higgs.trust.slave.model.bo.consensus.PackageCommand;
import com.higgs.trust.slave.model.bo.consensus.master.ChangeMasterCommand;
import com.higgs.trust.slave.model.bo.consensus.master.ChangeMasterVerifyResponse;
import com.higgs.trust.slave.model.bo.consensus.master.MasterHeartbeatCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @Description: replicate the sorted package to cluster
 * @author: pengdi
 **/

@Slf4j @Service public class LogReplicateHandlerImpl implements LogReplicateHandler {
    @Autowired PropertiesConfig propertiesConfig;
    /**
     * client from the log replicate consensus layer
     */
    @Autowired ConsensusClient consensusClient;

    @Autowired PackageService packageService;

    @Autowired ExecutorService packageThreadPool;

    @Autowired PackageProcess packageProcess;

    @Autowired RsPubKeyRepository rsPubKeyRepository;

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

        BeanValidateResult result = BeanValidator.validate(packageVO);
        if (!result.isSuccess()) {
            log.error("[LogReplicateHandler.replicatePackage]param validate failed, cause: " + result.getFirstMsg());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        // replicate package to all nodes
        log.info("package starts to distribute to each node through consensus layer");
        PackageCommand packageCommand =
            new PackageCommand(nodeState.getCurrentTerm(), nodeState.getMasterName(), packageVO);
        String signValue = packageCommand.getSignValue();
        packageCommand.setSign(SignUtils.sign(signValue, nodeState.getPrivateKey()));

        CompletableFuture future = consensusClient.submit(packageCommand);
        try {
            future.get(properties.getConsensusWaitTime(), TimeUnit.MILLISECONDS);
        } catch (Throwable e) {
            log.error("replicate log failed!");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_REPLICATE_FAILED, e);
        }

        log.info("package has been sent to consensus layer");
    }

    @Override public void changeMaster(long term, Map<String, ChangeMasterVerifyResponse> verifies) {
        log.info("change master, term:{}", term);
        ChangeMasterCommand command = new ChangeMasterCommand(term, nodeState.getNodeName(), verifies);
        command.setSign(SignUtils.sign(command.getSignValue(), nodeState.getPrivateKey()));
        CompletableFuture<Map<String, ChangeMasterVerifyResponse>> future = consensusClient.submit(command);
        try {
            future.get(properties.getConsensusWaitTime(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("change master failed!", e);
        }
    }

    @Override public void masterHeartbeat() {
        MasterHeartbeatCommand command =
            new MasterHeartbeatCommand(nodeState.getCurrentTerm(), nodeState.getNodeName());
        command.setSign(SignUtils.sign(command.getSignValue(), nodeState.getPrivateKey()));
        CompletableFuture future = consensusClient.submit(command);
        try {
            future.get(properties.getConsensusWaitTime(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("master heartbeat failed!", e);
        }
    }
}
