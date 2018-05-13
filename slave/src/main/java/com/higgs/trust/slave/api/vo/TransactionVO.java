package com.higgs.trust.slave.api.vo;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

/**
 * @author tangfashuang
 * @date 2018/04/09 19:49
 * @desc return for rs
 */
@Setter @Getter public class TransactionVO extends BaseBO {
    /**
     * transaction id
     */
    private String txId;

    /**
     * error code
     */
    private String errCode;

    /**
     * error message
     */
    private String errMsg;

    /**
     * if retry or not
     */
    private Boolean retry;
}
