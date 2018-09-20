/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.model.bo.consensus;

import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.slave.model.bo.BlockHeader;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author suimi
 * @date 2018/4/17
 */
@NoArgsConstructor @Setter @Getter public class BlockHeaderCmd extends ValidCommand<BlockHeader> {

    private static final long serialVersionUID = 4342796241391024431L;

    private static final String VALID_HEADER_ID = "valid_block_header";

    private String requestId;

    public BlockHeaderCmd(BlockHeader value, long view) {
        super(value, view);
        this.requestId = VALID_HEADER_ID + Constant.SPLIT_SLASH + value.getHeight() + Constant.SPLIT_SLASH + System
            .currentTimeMillis();
    }

    @Override public String messageDigest() {
        return requestId;
    }
}
