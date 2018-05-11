package com.higgs.trust.slave.core.service.consensus.log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.Labels;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.consensus.bft.core.ConsensusClient;
import com.higgs.trust.consensus.bft.core.ConsensusCommit;
import com.higgs.trust.consensus.bft.core.template.AbstractConsensusStateMachine;
import com.higgs.trust.slave.api.vo.PackageVO;
import com.higgs.trust.slave.common.enums.NodeStateEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidateResult;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.repository.RsPubKeyRepository;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.core.service.consensus.p2p.P2pHandlerImpl;
import com.higgs.trust.slave.core.service.failover.SyncService;
import com.higgs.trust.slave.core.service.pack.PackageProcess;
import com.higgs.trust.slave.core.service.pack.PackageService;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.consensus.*;
import com.higgs.trust.slave.model.bo.manage.RsPubKey;
import com.higgs.trust.slave.model.convert.PackageConvert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;

/**
 * @Description: replicate the sorted package to cluster
 * @author: pengdi
 **/
@Slf4j @Service public class LogReplicateHandlerImpl extends AbstractConsensusStateMachine
    implements LogReplicateHandler {

    /**
     * client from the log replicate consensus layer
     */
    @Autowired ConsensusClient consensusClient;

    @Autowired PackageService packageService;

    @Autowired ExecutorService packageThreadPool;

    @Autowired PackageProcess packageProcess;

    @Autowired RsPubKeyRepository rsPubKeyRepository;

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
        PackageCommand packageCommand = new PackageCommand(packageVO);
        CompletableFuture future = consensusClient.submit(packageCommand);
        try {
            future.get(2, TimeUnit.SECONDS);
        } catch (Throwable e) {
            log.error("replicate log failed!");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_REPLICATE_FAILED, e);
        }
        log.info("package has been sent to consensus layer");
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

        // verify signature
        try {
            String verifyString = JSON.toJSONString(packageVO, Labels.excludes("sign"));
            //get master node public key
            RsPubKey rsPubKey = rsPubKeyRepository.queryByRsId(nodeState.getMasterName());
            if (null == rsPubKey) {
                log.error("cannot acquire rsPubKey. rsId={}", nodeState.getMasterName());
                throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_VERIFY_SIGNATURE_FAILED);
            }
            boolean verify = SignUtils.verify(verifyString, packageVO.getSign(), rsPubKey.getPubKey());
            if (!verify) {
                log.error("package verify signature failed.");
                commit.close();
                //TODO 添加告警
                return;
            }
        } catch (Throwable e) {
            log.error("verify signature exception. {}", e.getMessage());
            //TODO 添加告警
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_VERIFY_SIGNATURE_FAILED, e);
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
