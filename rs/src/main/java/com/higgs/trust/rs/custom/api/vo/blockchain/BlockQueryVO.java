package com.higgs.trust.rs.custom.api.vo.blockchain;

import com.higgs.trust.rs.custom.api.vo.BaseVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlockQueryVO extends BaseVO {

    private Long height;

    private String blockHash;

    private Integer pageNo;

    private Integer pageSize;
}
