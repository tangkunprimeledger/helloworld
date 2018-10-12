package com.higgs.trust.management.failover.service;

import com.higgs.trust.common.enums.MonitorTargetEnum;
import com.higgs.trust.common.utils.MonitorLogUtils;
import com.higgs.trust.consensus.config.NodeProperties;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.consensus.config.listener.StateChangeListener;
import com.higgs.trust.consensus.config.listener.StateListener;
import com.higgs.trust.management.exception.FailoverExecption;
import com.higgs.trust.management.exception.ManagementError;
import com.higgs.trust.management.failover.config.FailoverProperties;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.model.bo.Block;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@StateListener
@Service @Slf4j public class SelfCheckingService {

    @Autowired private BlockSyncService blockSyncService;
    @Autowired private BlockService blockService;
    @Autowired private BlockRepository blockRepository;
    @Autowired private NodeProperties properties;

    @StateChangeListener(NodeStateEnum.SelfChecking) public void autoCheck() {
        boolean selfChecked = selfCheck(properties.getStartupRetryTime());
        log.info("self checked result:{}", selfChecked);
        if (!selfChecked) {
            MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SELF_CHECK_FAILED, 1);
            throw new FailoverExecption(ManagementError.MANAGEMENT_STARTUP_SELF_CHECK_FAILED);
        }
    }

    /**
     * 检查自身最高区块是否正确
     *
     * @param tryTimes bft validating retry times
     * @return
     */
    public boolean selfCheck(int tryTimes) {
        log.info("Starting self checking ...");
        Long maxHeight = blockService.getMaxHeight();
        if (maxHeight == null) {
            return true;
        }
        Long safeHeight = blockSyncService.getSafeHeight(properties.getStartupRetryTime());
        if (safeHeight == null){
            return false;
        }
        Block block = maxHeight <= safeHeight?blockRepository.getBlock(maxHeight):blockRepository.getBlock(safeHeight);
        int i = 0;
        if (blockSyncService.validating(block)) {
            do {
                Boolean result = blockSyncService.bftValidating(block.getBlockHeader());
                if (result != null) {
                    return result;
                }
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    log.warn("self check error.", e);
                }

            } while (++i < tryTimes);
            throw new FailoverExecption(SlaveErrorEnum.SLAVE_CONSENSUS_GET_RESULT_FAILED);
        }
        return false;
    }
}
