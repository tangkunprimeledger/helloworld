/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.core.service.consensus.cluster;

import com.higgs.trust.consensus.bft.core.template.AbstractConsensusCommand;
import com.higgs.trust.slave.model.bo.BlockHeader;
import lombok.Getter;
import lombok.Setter;

/**
 * @author suimi
 * @date 2018/4/17
 */
@Getter @Setter public class BlockHeaderCmd extends IdConsensusCommand<BlockHeader> {

    public BlockHeaderCmd(String nodeName, BlockHeader value) {
        super(value.getBlockHash(), nodeName, value);
    }
}
