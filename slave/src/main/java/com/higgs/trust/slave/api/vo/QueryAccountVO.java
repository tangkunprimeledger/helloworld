package com.higgs.trust.slave.api.vo;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

/**
 * @author tangfashuang
 * @date 2018/05/12
 * @desc query account request
 */
@Setter
@Getter
public class QueryAccountVO extends BaseBO{
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
