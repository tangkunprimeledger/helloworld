package com.higgs.trust.slave.core.service.consensus.cluster;

import com.higgs.trust.slave.common.SingleNodeConditional;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.model.bo.BlockHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary @Service @Conditional(SingleNodeConditional.class) @Slf4j public class SingleNodeClusterService
    implements ClusterService {

    @Autowired private BlockRepository blockRepository;

    @Autowired private BlockService blockService;

    @Override public Long getClusterHeight(int size, long timeout) {
        return blockRepository.getMaxHeight();
    }

    @Override public Long getClusterHeight(String requestId, int size, long timeout) {
        return blockRepository.getMaxHeight();
    }

    @Override public Boolean validatingHeader(BlockHeader header, long timeout) {
        BlockHeader blockHeader = blockRepository.getBlockHeader(header.getHeight());
        return blockHeader != null && blockService.compareBlockHeader(header, blockHeader);
    }
}
