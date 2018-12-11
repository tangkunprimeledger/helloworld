package com.higgs.trust.slave.api.vo;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
    @Size(max = 64)
    private String accountNo;

    /**
     * data owner
     */
    @Size(max = 24)
    private String dataOwner;

    /**
     * page number
     */
    @NotNull
    private Integer pageNo;

    /**
     * page size
     */
    @NotNull
    private Integer pageSize;
}
