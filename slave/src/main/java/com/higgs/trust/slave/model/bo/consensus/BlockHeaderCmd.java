/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.model.bo.consensus;

import com.higgs.trust.slave.common.constant.Constant;
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

    private static final String VALID_HEADER_ID = "valid_block_header";

    public BlockHeaderCmd(String nodeName, BlockHeader value) {
        super(VALID_HEADER_ID + Constant.SPLIT_SLASH + value.getHeight() + Constant.SPLIT_SLASH + System
            .currentTimeMillis(), nodeName, value);
    }
}
