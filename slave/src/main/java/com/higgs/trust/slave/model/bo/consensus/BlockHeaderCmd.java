/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.model.bo.consensus;

import com.higgs.trust.slave.model.bo.BlockHeader;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author suimi
 * @date 2018/4/17
 */
public class BlockHeaderCmd extends IdConsensusCommand<BlockHeader> {

    private static final long serialVersionUID = 4342796241391024431L;

    public BlockHeaderCmd(String nodeName, BlockHeader value) {
        super(value.getBlockHash(), nodeName, value);
    }
}
