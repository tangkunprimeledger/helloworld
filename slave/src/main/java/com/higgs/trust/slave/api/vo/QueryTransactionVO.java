package com.higgs.trust.slave.api.vo;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author tangfashuang
 * @date 2018/05/12 18:38
 * @desc query transaction request
 */
@Getter
@Setter
public class QueryTransactionVO extends BaseBO {

    private Long blockHeight;

    @Size(max = 64)
    private String txId;

    @Size(max = 32)
    private String sender;

    @NotNull
    private Integer pageNo;
    @NotNull
    private Integer pageSize;
}
