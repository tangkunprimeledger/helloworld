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
     * contract address
     */
    private String address;
    /**
     * if transfer，which is tx's nonce
     */
    private Long nonce;
    /**
     * if transfer，which is transfering amount
     */
    private BigDecimal value;
    /**
     * 调用方法签名
     */
    private String method;
}
