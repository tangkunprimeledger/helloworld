package com.higgs.trust.slave.api.vo;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

/**
 * @author tangfashuang
 * @date 2018/05/12 18:38
 * @desc query transaction request
 */
@Getter
@Setter
public class QueryTransactionVO extends BaseBO{

    private Long blockHeight;

    private String txId;

    private String sender;

    private Integer pageNo;

    private Integer pageSize;
}
