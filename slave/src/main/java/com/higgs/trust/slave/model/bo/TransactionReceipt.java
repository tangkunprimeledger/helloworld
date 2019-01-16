package com.higgs.trust.slave.model.bo;

import lombok.Getter;
import lombok.Setter;

/**
 * @author liuyu
 * @description the execution result of the transaction
 * @date 2018-04-09
 */
@Getter @Setter public class TransactionReceipt extends BaseBO {
    /**
     * the id of the transaction
     */
    private String txId;
    /**
     * the execution result of the transaction
     */
    private boolean result;
    /**
     * error code for transaction execution
     */
    private String errorCode;

    /**
     * error message for transaction execution
     */
    private String errorMessage;
}
