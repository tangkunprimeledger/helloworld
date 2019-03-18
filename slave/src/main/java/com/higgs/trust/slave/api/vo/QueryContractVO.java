package com.higgs.trust.slave.api.vo;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author tangfashuang
 * @date 2018/05/12
 * @desc query Contract r
 * equest
 */
@Setter
@Getter
public class QueryContractVO extends BaseBO {
    /**
     * block height
     */
    private Long height;

    /**
     * tx Id
     */
    @Size(max = 64)
    private String txId;

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
