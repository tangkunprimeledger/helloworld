package com.higgs.trust.rs.custom.api.vo.blockchain;

import com.higgs.trust.rs.custom.api.vo.BaseVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountQueryVO extends BaseVO{
    /**
     * account no
     */
    private String accountNo;

    /**
     * data owner
     */
    private String dataOwner;

    /**
     * page number
     */
    private Integer pageNo;

    /**
     * page size
     */
    private Integer pageSize;
}
