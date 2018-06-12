package com.higgs.trust.slave.core.service.consensus.cluster;

import com.higgs.trust.slave.common.SingleNodeConditional;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.model.bo.BlockHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Primary @Service @Conditional(SingleNodeConditional.class) @Slf4j public class SingleNodeClusterService
    implements IClusterService {

    @Autowired private BlockRepository blockRepository;

    @Autowired private BlockService blockService;

    @Autowired private NodeState nodeState;

    @Override public Long getClusterHeight(int size) {
        return blockRepository.getMaxHeight();
    }

    @Override public Long getClusterHeight(String requestId, int size) {
        return blockRepository.getMaxHeight();
    }

    @Override public Boolean validatingHeader(BlockHeader header) {
        BlockHeader blockHeader = blockRepository.getBlockHeader(header.getHeight());
        return blockHeader != null && blockService.compareBlockHeader(header, blockHeader);
    }

    @Override public Map<String, Long> getAllClusterHeight() {
        Map<String, Long> result = new HashMap<>();
        result.put(nodeState.getNodeName(), blockRepository.getMaxHeight());
        return result;
    }
}
