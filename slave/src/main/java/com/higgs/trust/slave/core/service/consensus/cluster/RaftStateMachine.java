/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.service.consensus.cluster;

import com.higgs.trust.consensus.bft.core.ConsensusCommit;
import com.higgs.trust.consensus.bft.core.template.AbstractConsensusStateMachine;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.model.bo.BlockHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author suimi
 * @date 2018/4/17
 */
@Component @Slf4j public class RaftStateMachine extends AbstractConsensusStateMachine {

    @Autowired private ClusterServiceImpl consensus;

    @Autowired private BlockRepository blockRepository;

    @Autowired private BlockService blockService;

    /**
     * handle the commit and submit p2p consensus for cluster height
     *
     * @return
     */
    public void getClusterHeight(ConsensusCommit<ClusterHeightCmd> commit) {
        try {
            ClusterHeightCmd operation = commit.operation();
            List<Long> maxHeights = blockRepository.getMaxHeight(operation.get());
            maxHeights.forEach(height -> {
                try {
                    consensus
                        .submit(new ValidClusterHeightCmd(operation.getRequestId(), height), operation.getNodeName());
                } catch (Exception e) {
                    log.error("consensus submit error:", e);
                }
            });
        } finally {
            commit.close();
        }
    }

    /**
     * handle the commit and submit p2p consensus for the result of validating block header
     *
     * @return
     */
    public void validHeader(ConsensusCommit<BlockHeaderCmd> commit) {
        try {
            BlockHeaderCmd operation = commit.operation();
            BlockHeader header = operation.get();
            BlockHeader blockHeader = blockRepository.getBlockHeader(header.getHeight());
            boolean result = blockService.compareBlockHeader(header, blockHeader);
            try {
                consensus.submit(new ValidHeaderCmd(header, result), operation.getNodeName());
            } catch (Exception e) {
                log.error("consensus submit error:", e);
            }
        } finally {
            commit.close();
        }
    }
}
