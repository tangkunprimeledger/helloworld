package com.higgs.trust.rs.custom.api.vo.blockchain;

import com.higgs.trust.rs.custom.api.vo.BaseVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TxQueryVO extends BaseVO{

    private Long blockHeight;

    private String txId;

    private String sender;

    private Integer pageNo;

    private Integer pageSize;
}
