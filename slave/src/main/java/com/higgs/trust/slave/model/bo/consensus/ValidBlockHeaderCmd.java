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
@Getter @Setter @NoArgsConstructor public class ValidBlockHeaderCmd extends IdValidCommand<Boolean> {

    private static final long serialVersionUID = 1644770444682750035L;

    private BlockHeader header;

    public ValidBlockHeaderCmd(BlockHeader header, Boolean valid) {
        super(header.getBlockHash(), valid);
        this.header = header;
    }

    @Override public String messageDigest() {
        return header.getBlockHash() + this.get();
    }
}
