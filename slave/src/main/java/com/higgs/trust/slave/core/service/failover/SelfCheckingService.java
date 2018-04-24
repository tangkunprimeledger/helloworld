package com.higgs.trust.slave.core.service.failover;

import com.higgs.trust.slave.common.enums.NodeStateEnum;
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
        boolean selfChecked = selfCheck();
        if (!selfChecked) {
            nodeState.changeState(NodeStateEnum.SelfChecking, NodeStateEnum.Offline);
            return false;
        }
        if (nodeState.isMaster()) {
            boolean masterChecked = masterCheck();
            if (masterChecked) {
                nodeState.changeState(NodeStateEnum.SelfChecking, NodeStateEnum.Running);
            } else {
                log.error("Node master check not pass");
                nodeState.changeState(NodeStateEnum.SelfChecking, NodeStateEnum.Offline);
            }
        }
        return true;
    }

    /**
     * 检查自身最高区块是否正确
     *
     * @return
     */
    public boolean selfCheck() {
        Long maxHeight = blockService.getMaxHeight();
        Block block = blockRepository.getBlock(maxHeight);
        if (blockSyncService.validating(block)) {
            return blockSyncService.bftValidating(block.getBlockHeader());
        }
        return false;
    }

    /**
     * 检查是否能胜任master
     *
     * @return
     */
    public boolean masterCheck() {
        //todo:suimi 发送空包，获取最新高度比较
        return false;
    }
}
