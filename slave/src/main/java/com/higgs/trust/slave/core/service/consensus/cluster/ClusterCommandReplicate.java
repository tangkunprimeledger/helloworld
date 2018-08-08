/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.service.consensus.cluster;

/**
 * @author suimi
 * @date 2018/6/11
 */

import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.p2pvalid.annotation.P2pvalidReplicator;
import com.higgs.trust.consensus.p2pvalid.core.ValidSyncCommit;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.consensus.BlockHeaderCmd;
import com.higgs.trust.slave.model.bo.consensus.ClusterHeightCmd;
import com.higgs.trust.slave.model.bo.consensus.ValidBlockHeaderCmd;
import com.higgs.trust.slave.model.bo.consensus.ValidClusterHeightCmd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j @P2pvalidReplicator @Component public class ClusterCommandReplicate {

    @Autowired private BlockRepository blockRepository;

    @Autowired private BlockService blockService;

    @Autowired private NodeState nodeState;

    /**
     * handle the consensus result of cluster height
     *
     * @param commit
     */
    public List<ValidClusterHeightCmd> handleClusterHeight(ValidSyncCommit<ClusterHeightCmd> commit) {
        ClusterHeightCmd operation = commit.operation();
        List<Long> maxHeights = blockRepository.getMaxHeight(operation.get());
        log.info("node={}, maxHeights={}", nodeState.getNodeName(), maxHeights);
        List<ValidClusterHeightCmd> cmds = new ArrayList<>();
        maxHeights.forEach(height -> cmds.add(new ValidClusterHeightCmd(operation.getRequestId(), height)));
        return cmds;
    }

    /**
     * handle the consensus result of validating block header
     *
     * @param commit
     */
    public ValidBlockHeaderCmd handleValidHeader(ValidSyncCommit<BlockHeaderCmd> commit) {
        BlockHeaderCmd operation = commit.operation();
        BlockHeader header = operation.get();
        BlockHeader blockHeader = blockRepository.getBlockHeader(header.getHeight());
        boolean result = blockHeader != null && blockService.compareBlockHeader(header, blockHeader);
        log.info("node ={}, valid header result={}", nodeState.getNodeName(), result);
        return new ValidBlockHeaderCmd(operation.getRequestId(), header, result);
    }
}
