package com.higgs.trust.slave.core.service.failover;

import com.higgs.trust.slave.common.enums.NodeStateEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.FailoverExecption;
import com.higgs.trust.slave.core.managment.NodeState;
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

    /**
     * auto check
     *
     * @return check result
     */
    public boolean check() {
        nodeState.changeState(NodeStateEnum.Starting, NodeStateEnum.SelfChecking);
        boolean selfChecked = false;
        try {
            selfChecked = selfCheck(60);
            log.info("self checked result:{}", selfChecked);
        } catch (FailoverExecption e) {
            log.error("self check failed:", e);
        }
        if (!selfChecked) {
            nodeState.changeState(NodeStateEnum.SelfChecking, NodeStateEnum.Offline);
            return false;
        }
        if (nodeState.isMaster()) {
            boolean masterChecked = masterCheck();
            log.info("master checked result:{}", masterChecked);
            if (masterChecked) {
                nodeState.changeState(NodeStateEnum.SelfChecking, NodeStateEnum.Running);
            } else {
                log.error("Node master check not pass");
                nodeState.changeState(NodeStateEnum.SelfChecking, NodeStateEnum.Offline);
                return false;
            }
        } else {
            nodeState.changeState(NodeStateEnum.SelfChecking, NodeStateEnum.AutoSync);
        }
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
            throw new FailoverExecption(SlaveErrorEnum.SLAVE_CONSENSUS_WAIT_RESULT_TIMEOUT);
        }
        return false;
    }

    /**
     * 检查是否能胜任master
     *
     * @return
     */
    public boolean masterCheck() {
        log.info("Starting master checking ...");
        log.debug("need todo ....");
        //todo:suimi 发送空包，获取最新高度比较
        return true;
    }
}
