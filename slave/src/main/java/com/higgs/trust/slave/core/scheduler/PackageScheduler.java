package com.higgs.trust.slave.core.scheduler;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.slave.common.constant.Constant;
import com.higgs.trust.slave.common.util.MonitorLogUtils;
import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.slave.core.managment.master.MasterPackageCache;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * @author tangfashuang
 * @date 2018/04/09 15:30
 */
@ConditionalOnProperty(name = "higgs.trust.joinConsensus", havingValue = "true", matchIfMissing = true)
@Service @Slf4j public class PackageScheduler {

    @Autowired private PendingState pendingState;

    @Autowired private PackageRepository packageRepository;

    @Autowired private PackageService packageService;

    @Autowired private PackageProcess packageProcess;

    @Autowired private BlockRepository blockRepository;

    @Autowired private NodeState nodeState;

    @Autowired private MasterPackageCache packageCache;

    @Value("${trust.batch.tx.limit:200}") private int TX_PENDING_COUNT;
    @Value("${higgs.trust.package.batchSize:100}") private int batchSize;

    /**
     * master node create package
     */
    @Scheduled(fixedRateString = "${trust.schedule.package.create}") public void createPackage() {

        if(nodeState.isMaster()
            && packageCache.getPendingPackSize() < Constant.MAX_BLOCKING_QUEUE_SIZE) {

            List<SignedTransaction> signedTransactions = pendingState.getPendingTransactions(TX_PENDING_COUNT);

            if (CollectionUtils.isEmpty(signedTransactions)) {
                return;
            }
            // remove dup transactions
            Set<SignedTransaction> txSet = Sets.newHashSet();
            CollectionUtils.addAll(txSet, signedTransactions);
            signedTransactions = Lists.newArrayList(txSet);

            log.debug("[PackageScheduler.createPackage] start create package, currentPackHeight={}", packageCache.getPackHeight());
            Package pack = packageService.create(signedTransactions, packageCache.getPackHeight());

            if (null == pack) {
                //add pending signedTransactions to pendingTxQueue
                pendingState.addPendingTxsToQueueFirst(signedTransactions);
                return;
            }

            try {
                packageCache.putPendingPack(pack);
            } catch (InterruptedException e) {
                log.warn("pending pack offer InterruptedException. ", e);
                pendingState.addPendingTxsToQueueFirst(signedTransactions);
            }

            log.debug("[PackageScheduler.createPackage] create package finished, packHeight={}", pack.getHeight());
        }
    }

    /**
     * master node submit package
     */
    @Scheduled(fixedRateString = "${trust.schedule.package.submit}") public void submitPackage() {
        int i = 0;
        while (nodeState.isMaster() && packageCache.getPendingPackSize() > 0 && ++i < batchSize) {
            packageService.submitConsensus(packageCache.getPackage());
        }
    }

    /**
     * process package
     */
    @Scheduled(fixedRateString = "${trust.schedule.package.process}") public void processPackage() {
        if (!nodeState.isState(NodeStateEnum.Running)) {
            return;
        }
        //get max block height
        Long maxBlockHeight = blockRepository.getMaxHeight();

        if (null == maxBlockHeight) {
            log.error("please initial Genesis block.");
            //TODO 添加告警
            MonitorLogUtils.logIntMonitorInfo("GENESIS_BLOCK_NOT_EXISTS", 1);
            return;
        }
        //check if the next package to be process is exited
        Long height = maxBlockHeight + 1;
        Package pack = packageRepository.load(height);
        if (null == pack) {
            return;
        }

        // process next block as height = maxBlockHeight + 1
        try {
            packageProcess.process(height);
        } catch (Throwable e) {
            log.error("package process scheduled execute failed. ", e);
        }

    }
}
