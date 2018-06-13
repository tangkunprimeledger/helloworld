package com.higgs.trust.management.failover.service;

import com.higgs.trust.config.node.NodeProperties;
import com.higgs.trust.config.node.NodeStateEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.management.exception.FailoverExecption;
import com.higgs.trust.config.node.NodeState;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.model.bo.Block;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service @Slf4j public class SelfCheckingService {

    @Autowired private BlockSyncService blockSyncService;
    @Autowired private BlockService blockService;
    @Autowired private BlockRepository blockRepository;
    @Autowired private NodeState nodeState;
    @Autowired private NodeProperties properties;

    /**
     * auto check
     *
     * @return check result
     */
    public boolean check() {
        nodeState.changeState(NodeStateEnum.Starting, NodeStateEnum.SelfChecking);
        boolean selfChecked = false;
        try {
            selfChecked = selfCheck(properties.getSelfCheckTimes());
            log.info("self checked result:{}", selfChecked);
        } catch (FailoverExecption e) {
            log.error("self check failed:", e);
        }
        if (!selfChecked) {
            nodeState.changeState(NodeStateEnum.SelfChecking, NodeStateEnum.Offline);
            return false;
        }
        nodeState.changeState(NodeStateEnum.SelfChecking, NodeStateEnum.AutoSync);
        return true;
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
        Block block = blockRepository.getBlock(maxHeight);
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
