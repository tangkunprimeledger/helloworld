package com.higgs.trust.slave.model.bo.contract;

import com.higgs.trust.slave.model.bo.action.Action;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author kongyu
 * @description the action of contract invoke
 * @date 2018-11-30
 */
@Getter
@Setter
public class ContractInvokeV2Action extends Action {
    /**
     * 合约地址
     */
    private String address;
    /**
     * 若是转账，为交易的nonce
     */
    private Long nonce;
    /**
     * 若是转账，为转账金额
     */
    private BigDecimal value;
    /**
     * 调用方法签名
     */
    private String method;
}
