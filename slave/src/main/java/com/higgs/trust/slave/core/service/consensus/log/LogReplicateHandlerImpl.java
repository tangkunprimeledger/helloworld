package com.higgs.trust.slave.core.service.consensus.log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.Labels;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.consensus.bft.core.ConsensusClient;
import com.higgs.trust.consensus.bft.core.ConsensusCommit;
import com.higgs.trust.consensus.bft.core.template.AbstractConsensusStateMachine;
import com.higgs.trust.slave.api.vo.PackageVO;
import com.higgs.trust.slave.common.config.PropertiesConfig;
import com.higgs.trust.slave.common.enums.NodeStateEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidateResult;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.repository.RsNodeRepository;
import com.higgs.trust.slave.core.service.failover.SyncService;
import com.higgs.trust.slave.core.service.pack.PackageProcess;
import com.higgs.trust.slave.core.service.pack.PackageService;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.consensus.PackageCommand;
import com.higgs.trust.slave.model.convert.PackageConvert;
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
@Slf4j @Service public class LogReplicateHandlerImpl extends AbstractConsensusStateMachine
    implements LogReplicateHandler {
    @Autowired PropertiesConfig propertiesConfig;
    /**
     * client from the log replicate consensus layer
     */
    @Autowired ConsensusClient consensusClient;

    @Autowired PackageService packageService;

    @Autowired ExecutorService packageThreadPool;

    @Autowired PackageProcess packageProcess;

    @Autowired RsNodeRepository rsNodeRepository;

    @Autowired private NodeState nodeState;

    @Autowired private SyncService syncService;

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

        PackageCommand packageCommand = buildPackageCommand(packageVO);

        Object obj = null;
        while (obj == null) {

            CompletableFuture future = consensusClient.submit(packageCommand);
            try {
                obj = future.get(800, TimeUnit.MILLISECONDS);
            } catch (Throwable e) {
                log.error("replicate log failed!", e);
                //TODO 添加告警
            }
        }
        log.info("package has been sent to consensus layer");
    }

    private PackageCommand buildPackageCommand(PackageVO packageVO) {
        PackageCommand packageCommand = new PackageCommand(packageVO);
        packageCommand.setMasterName(nodeState.getMasterName());
        packageCommand.setTerm(nodeState.getTerm());

        // set signature
        while (null == packageCommand.getSign()) {
            try {
                String dataString = JSON.toJSONString(packageCommand, Labels.excludes("sign"));
                packageCommand.setSign(SignUtils.sign(dataString, nodeState.getPrivateKey()));
            } catch (Exception e) {
                log.error("packageCommand sign exception. ", e);
                //TODO 添加告警
            }
        }

        return packageCommand;
    }

    /**
     * package has been replicated by raft/bft-smart/pbft/etc
     *
     * @param commit
     * @return
     */
    public void packageReplicated(ConsensusCommit<PackageCommand> commit) {
        // validate param
        if (null == commit) {
            log.error(
                "[LogReplicateHandler.packageReplicated]param validate failed, cause package command commit is null ");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        PackageVO packageVO = commit.operation().get();
        log.info("package reached consensus, log: {}", packageVO);

        // validate param
        if (null == packageVO) {
            log.error("[LogReplicateHandler.packageReplicated]param validate failed, cause package is null ");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        BeanValidateResult result = BeanValidator.validate(packageVO);
        if (!result.isSuccess()) {
            log.error("[LogReplicateHandler.packageReplicated]param validate failed, cause: " + result.getFirstMsg());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        // receive package
        Package pack = PackageConvert.convertPackVOToPack(packageVO);
        boolean isRunning = nodeState.isState(NodeStateEnum.Running);
        try {
            packageService.receive(pack);
            if (nodeState.isState(NodeStateEnum.AutoSync)) {
                syncService.receivePackHeight(pack.getHeight());
            }
            commit.close();
        } catch (SlaveException e) {
            //idempotent as success, other exceptions make the consensus layer retry
            if (e.getCode() != SlaveErrorEnum.SLAVE_IDEMPOTENT) {
                throw e;
            }
        }

        if (isRunning) {
            //async start package process
            try {
                packageThreadPool.execute(new AsyncPackageProcess(pack.getHeight()));
            } catch (Throwable e) {
                log.error("package's async process failed after package replicated", e);
            }
        }
    }

    /**
     * thread for async package process
     */
    private class AsyncPackageProcess implements Runnable {
        private Long height;

        public AsyncPackageProcess(Long height) {
            this.height = height;
        }

        @Override public void run() {
            /**
             * if the header satisfy the following conditions, just async start process
             * 1.header.height == max(blockHeight) + 1
             * 2.package.status is RECEIVED
             */
            packageProcess.process(height);
        }
    }
}
