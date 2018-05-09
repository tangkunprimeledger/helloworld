package com.higgs.trust.slave.core.service.consensus.p2p;

import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommit;
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
import com.higgs.trust.slave.common.config.PropertiesConfig;
import com.higgs.trust.slave.common.enums.NodeStateEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidateResult;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.repository.PackageRepository;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.core.service.pack.PackageLock;
import com.higgs.trust.slave.core.service.pack.PackageProcess;
import com.higgs.trust.slave.core.service.pack.PackageService;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.consensus.PersistCommand;
import com.higgs.trust.slave.model.bo.consensus.ValidateCommand;
import com.higgs.trust.slave.model.enums.BlockHeaderTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

/**
 * @Description: handle p2p message sending and p2p message receiving
 * @author: pengdi
 **/
@Slf4j
@Service
public class P2pHandlerImpl extends ValidConsensus implements P2pHandler {

    PropertiesConfig propertiesConfig;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private PackageService packageService;

    @Autowired
    private PackageProcess packageProcess;

    @Autowired
    private ExecutorService packageThreadPool;

    @Autowired
    private BlockService blockService;

    @Autowired
    private NodeState nodeState;


    @Autowired
    private PackageLock packageLock;

    @Autowired
    public P2pHandlerImpl(P2pClusterInfo p2pClusterInfo, P2pConsensusClient p2pConsensusClient, PropertiesConfig propertiesConfig) {
        super(p2pClusterInfo, p2pConsensusClient, propertiesConfig.getP2pDataDir());
        this.propertiesConfig = propertiesConfig;
    }

    /**
     * send validating result to p2p consensus layer
     *
     * @param header
     */
    @Override
    public void sendValidating(BlockHeader header) {
        // validate param
        if (null == header) {
            log.error("[P2pReceiver.sendValidating]param validate failed, cause block header is null ");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        BeanValidateResult result = BeanValidator.validate(header);
        if (!result.isSuccess()) {
            log.error("[P2pReceiver.sendValidating]param validate failed, cause: " + result.getFirstMsg());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        // send header to p2p consensus
        ValidateCommand validateCommand = new ValidateCommand(header.getHeight(), header);
        log.info("start send validating command to p2p consensus layer, validateCommand : {}", validateCommand);
        this.submit(validateCommand);
        log.info("end send validating command to p2p consensus layer");
    }

    /**
     * send validating result to p2p consensus layer
     *
     * @param header
     */
    @Override
    public void sendPersisting(BlockHeader header) {
        // validate param
        if (null == header) {
            log.error("[P2pReceiver.sendPersisting]param validate failed, cause block header is null ");
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        BeanValidateResult result = BeanValidator.validate(header);
        if (!result.isSuccess()) {
            log.error("[P2pReceiver.sendPersisting]param validate failed, cause: {}", result.getFirstMsg());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        // send header to p2p consensus
        PersistCommand persistCommand = new PersistCommand(header.getHeight(), header);
        log.info("start send persisting command to p2p consensus layer, persistCommand : {}", persistCommand);
        this.submit(persistCommand);
        log.info("end send persisting command to p2p consensus layer");
    }

    /**
     * majority of this cluster has validated this command
     *
     * @param commit
     */
    public void receiveValidated(ValidCommit<ValidateCommand> commit) {
        // validate param
        BeanValidateResult result = BeanValidator.validate(commit);
        if (!result.isSuccess()) {
            log.error("[P2pReceiver.validated]param validate failed, cause: " + result.getFirstMsg());
            throw new SlaveException(SlaveErrorEnum.SLAVE_PARAM_VALIDATE_ERROR);
        }

        // get validated block p2p and package
        BlockHeader header = commit.operation().get();
        log.info("the validated p2p result is {}", header);

        doReceive(commit, header, BlockHeaderTypeEnum.CONSENSUS_VALIDATE_TYPE);
    }

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

        doReceive(commit, header, BlockHeaderTypeEnum.CONSENSUS_PERSIST_TYPE);
    }

    /**
     * async start package process, maybe the right package is waiting for consensus
     *
     * @param commit
     * @param header
     * @param headerType
     */
    private void doReceive(ValidCommit commit, BlockHeader header, BlockHeaderTypeEnum headerType) {
        try {
            // store the validated header result
            blockService.storeTempHeader(header, headerType);
        } catch (SlaveException e) {
            //idempotent as success, other exceptions make the consensus layer retry
            if (e.getCode() != SlaveErrorEnum.SLAVE_IDEMPOTENT) {
                throw e;
            }
        }
        commit.close();

        if (!nodeState.isState(NodeStateEnum.Running)) {
            return;
        }

        //async start process,when headerType = CONSENSUS_VALIDATE_TYPE, multi thread and lock
        if (headerType == BlockHeaderTypeEnum.CONSENSUS_VALIDATE_TYPE) {
            try {
                packageThreadPool.execute(new AsyncPackageProcess(header.getHeight()));
            } catch (Throwable e) {
                log.error("package's async  validated failed after receive {}, header: {}", headerType, header, e);
            }
        }

        //async start AsyncPersistingToConsensus,when headerType = CONSENSUS_PERSIST_TYPE, multi thread and lock
        if (headerType == BlockHeaderTypeEnum.CONSENSUS_PERSIST_TYPE) {
            try {
                packageThreadPool.execute(new AsyncPackagePersisted(header.getHeight()));
            } catch (Throwable e) {
                log.error("package's async persistingToConsensus failed after receive {}, header: {}", headerType, header, e);
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

        @Override
        public void run() {
            /**
             * if the header satisfy the following conditions, just async start process
             * 1.header.height == max(blockHeight) + 1
             * 2.package.status is WAIT_VALIDATE_CONSENSUS or WAIT_PERSIST_CONSENSUS
             */
            packageProcess.process(height);
        }
    }


    /**
     * thread for async package Persisted
     */
    private class AsyncPackagePersisted implements Runnable {
        private Long height;

        public AsyncPackagePersisted(Long height) {
            this.height = height;
        }

        @Override
        public void run() {
            packageLock.lockAndPersisted(height);
        }
    }
}
