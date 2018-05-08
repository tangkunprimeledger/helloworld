package com.higgs.trust.slave.core.scheduler;

import com.higgs.trust.slave.common.enums.NodeStateEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.repository.PackageRepository;
import com.higgs.trust.slave.core.service.pack.PackageLock;
import com.higgs.trust.slave.core.service.pack.PackageProcess;
import com.higgs.trust.slave.core.service.pack.PackageService;
import com.higgs.trust.slave.core.service.pending.PendingState;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * @author tangfashuang
 * @date 2018/04/09 15:30
 */
@Service
@Slf4j
public class PackageScheduler {

    private static final int PACKAGE_LIMIT = 20;
    @Autowired
    private PendingState pendingState;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private TransactionTemplate txNested;

    @Autowired
    private PackageService packageService;

    @Autowired
    private PackageLock packageLock;

    @Autowired
    private PackageProcess packageProcess;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private NodeState nodeState;

    @Value("${trust.batch.tx.limit}")
    private int PENDING_COUNT;

    /**
     * master node create package
     */
    @Scheduled(fixedRateString = "${trust.schedule.package.create}")
    public void createPackage() {
        if (!nodeState.isMaster()) {
            return;
        }

        List<SignedTransaction> signedTransactions = pendingState.getPendingTransactions(PENDING_COUNT);

        if (CollectionUtils.isEmpty(signedTransactions)) {
            return;
        }

        Package pack = packageService.create(signedTransactions);

        if (null == pack) {
            return;
        }

        txNested.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                packageRepository.save(pack);
                int update = pendingState.packagePendingTransactions(pack.getSignedTxList(), pack.getHeight());
                if (update != pack.getSignedTxList().size()) {
                    log.error("update transaction list failed. ");
                    throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_UPDATE_PENDING_TX_ERROR);
                }
            }
        });

        packageLock.lockAndSubmit(pack.getHeight());
    }

    /**
     * master node submit package which status equals 'INIT'
     */
    @Scheduled(fixedRateString = "${trust.schedule.package.submit}")
    public void submitPackage() {
        if (!nodeState.isMaster()) {
            return;
        }

        // sort by height asc in mysql
        List<Long> heightList = packageRepository.getHeightListByStatus(PackageStatusEnum.INIT.getCode());

        // if list is null or empty，there are no package for submit
        if (CollectionUtils.isEmpty(heightList)) {
            return;
        }

        for (Long height : heightList) {
            packageLock.lockAndSubmit(height);
        }
    }

    /**
     * process package
     */
    @Scheduled(fixedRateString = "${trust.schedule.package.process}")
    public void processPackage() {
        if (!nodeState.isState(NodeStateEnum.Running)) {
            return;
        }
        //get max block height
        Long maxBlockHeight = blockRepository.getMaxHeight();

        if (null == maxBlockHeight) {
            log.error("please initial Genesis block.");
            //TODO 添加告警
            return;
        }

        // get height list for process, height must max maxBlockHeight and sort by height asc in mysql
        // when package status equals 'PERSISTING', block already persist, but package doesn't handle finish
        List<Long> heightList = packageRepository.getHeightListForProcess(maxBlockHeight);

        // if list is null or empty，there are no package for process
        if (CollectionUtils.isEmpty(heightList)) {
            return;
        }

        for (Long height : heightList) {
            //get max block height
            maxBlockHeight = blockRepository.getMaxHeight();
            if (height.equals(maxBlockHeight) || height.equals(maxBlockHeight + 1)) {
                try {
                    packageProcess.process(height);
                } catch (Throwable e) {
                    log.error("package process scheduled execute failed", e);
                }
            }
        }
    }



    /**
     * process package
     */
    @Scheduled(fixedRateString = "${trust.schedule.package.process}")
    public void doPersistingToConsensus() {
        if (!nodeState.isState(NodeStateEnum.Running)) {
            return;
        }

        // get height list for process  persist to p2p ,package status equals 'PERSISTING and  height be sort by height asc in mysql
        List<Long> heightList = packageRepository.getHeightListByStatus(PackageStatusEnum.PERSISTING.getCode());

        // if list is null or empty，there are no package for process
        if (CollectionUtils.isEmpty(heightList)) {
            return;
        }

        for (Long height : heightList) {
            packageLock.lockPersistingAndSubmit(height);
        }
    }

    /**
     * process package
     */
    @Scheduled(fixedRateString = "${trust.schedule.package.process}")
    public void doPersisted() {
        if (!nodeState.isState(NodeStateEnum.Running)) {
            return;
        }

        // get height list for process  persist to p2p ,package status equals 'PERSISTING and  height be sort by height asc in mysql
        List<Long> heightList = packageRepository.getHeightListByStatus(PackageStatusEnum.WAIT_PERSIST_CONSENSUS.getCode());

        // if list is null or empty，there are no package for process
        if (CollectionUtils.isEmpty(heightList)) {
            return;
        }

        for (Long height : heightList) {
            packageLock.lockAndPersisted(height);
        }
    }
}
