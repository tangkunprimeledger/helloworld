package com.higgs.trust.slave.core.scheduler;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.higgs.trust.common.utils.TraceUtils;
import com.higgs.trust.common.enums.MonitorTargetEnum;
import com.higgs.trust.common.utils.MonitorLogUtils;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
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
import org.springframework.cloud.sleuth.Span;
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

    /**
     * master node create package
     */
    @Scheduled(fixedRateString = "${trust.schedule.package.create}") public void createPackage() {

        if (nodeState.isState(NodeStateEnum.Running) && nodeState.isMaster()
            && packageCache.getPendingPackSize() < Constant.MAX_BLOCKING_QUEUE_SIZE) {

            List<SignedTransaction> signedTransactions = pendingState.getPendingTransactions(TX_PENDING_COUNT);

            if (CollectionUtils.isEmpty(signedTransactions)) {
                return;
            }
            // remove dup transactions
            Set<SignedTransaction> txSet = Sets.newHashSet();
            CollectionUtils.addAll(txSet, signedTransactions);
            signedTransactions = Lists.newArrayList(txSet);

            log.debug("[PackageScheduler.createPackage] start create package, currentPackHeight={}",
                packageCache.getPackHeight());
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
        while (nodeState.isState(NodeStateEnum.Running) && nodeState.isMaster()
            && packageCache.getPendingPackSize() > 0) {
            Span span = TraceUtils.createSpan();
            try {
                packageService.submitConsensus(packageCache.getPackages());
            } finally {
                TraceUtils.closeSpan(span);
            }
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
        Long currentHeight = blockRepository.getMaxHeight();

        if (null == currentHeight) {
            log.error("please initial Genesis block.");
            MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_GENESIS_BLOCK_NOT_EXISTS.getMonitorTarget(), 1);
            return;
        }

        while (true) {
            //check if the next package to be process is exited
            List<Long> heights = packageRepository.loadHeightList(currentHeight);

            if (CollectionUtils.isEmpty(heights)) {
                return;
            }

            for (Long height : heights) {
                // process next block as height = maxBlockHeight + 1
                if (height != currentHeight + 1) {
                    log.warn("package height is not continuous!");
                    return;
                }

                Span span = TraceUtils.createSpan();
                try {
                    packageProcess.process(height);
                    currentHeight++;
                } catch (Throwable e) {
                    log.error("package process scheduled execute failed. ", e);
                    return;
                } finally {
                    TraceUtils.closeSpan(span);
                }
            }
        }
    }
}
