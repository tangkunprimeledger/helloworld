package com.higgs.trust.slave.dao.po.block;

import com.higgs.trust.slave.dao.po.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BlockWithTxNumPO extends BaseEntity<BlockWithTxNumPO> {
    private Long height;

    private String blockHash;

    private String blockTime;

    private Integer txNum;

    private String version;
}
