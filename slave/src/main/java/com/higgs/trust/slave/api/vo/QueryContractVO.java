package com.higgs.trust.slave.api.vo;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

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
    @NotNull
    private Long height;

    /**
     * tx Id
     */
    private String txId;

    /**
     * page number
     */
    private Integer pageNo;

    /**
     * page size
     */
    private Integer pageSize;
}
