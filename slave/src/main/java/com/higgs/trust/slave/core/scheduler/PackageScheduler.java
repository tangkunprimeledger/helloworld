package com.higgs.trust.slave.core.scheduler;

import com.higgs.trust.config.node.NodeState;
import com.higgs.trust.config.node.NodeStateEnum;
import com.higgs.trust.slave.common.constant.Constant;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.repository.PackageRepository;
import com.higgs.trust.slave.core.service.pack.PackageProcess;
import com.higgs.trust.slave.core.service.pack.PackageService;
import com.higgs.trust.slave.core.service.pending.PendingState;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author tangfashuang
 * @date 2018/04/09 15:30
 */
@Service
@Slf4j
public class PackageScheduler {

    @Autowired
    private PendingState pendingState;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private PackageService packageService;

    @Autowired
    private PackageProcess packageProcess;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private NodeState nodeState;

    @Autowired
    private Long packHeight;

    /**
     * pending package blocking queue
     */
    @Autowired
    private BlockingQueue<Package> pendingPack;

    @Value("${trust.batch.tx.limit:200}")
    private int TX_PENDING_COUNT;

    /**
     * master node create package
     */
    @Scheduled(fixedRateString = "${trust.schedule.package.create}")
    public void createPackage() {
        if (!nodeState.isMaster()) {
            return;
        }

        if (pendingPack.size() > Constant.MAX_BLOCKING_QUEUE_SIZE - 1) {
            return;
        }

        List<SignedTransaction> signedTransactions = pendingState.getPendingTransactions(TX_PENDING_COUNT);

        if (CollectionUtils.isEmpty(signedTransactions)) {
            return ;
        }

        Package pack = packageService.create(signedTransactions, packHeight);

        if (null == pack) {
            //add pending signedTransactions to pendingTxQueue
            pendingState.addPendingTxsToQueueFirst(signedTransactions);
            return;
        }

        try {
            //add package to queue
            pendingPack.offer(pack, 100, TimeUnit.MILLISECONDS);
            packHeight = pack.getHeight();
        } catch (InterruptedException e) {
            log.warn("pending pack offer InterruptedException. ", e);
            pendingState.addPendingTxsToQueueFirst(signedTransactions);
        }
    }

    /**
     * master node submit package
     */
    @Scheduled(fixedRateString = "${trust.schedule.package.submit}")
    public void submitPackage() {
        if (!nodeState.isMaster()) {
            return;
        }

        if (pendingPack.size() < 1) {
            return;
        }

        packageService.submitConsensus(pendingPack.poll());
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
        //check if the next package to be process is exited
        Long height = maxBlockHeight + 1;
        Package pack = packageRepository.load(height);
        if (null == pack) {
            return;
        }

        // process  next block as height = maxBlockHeight + 1
        try {
            packageProcess.process(height);
        } catch (Throwable e) {
            log.error("package process scheduled execute failed", e);
        }

    }
}
