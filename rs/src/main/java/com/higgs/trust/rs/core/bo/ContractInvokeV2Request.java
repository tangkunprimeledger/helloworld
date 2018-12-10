package com.higgs.trust.rs.core.bo;

import com.higgs.trust.rs.common.BaseBO;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author kongyu
 * @date 2018/12/10
 */
@Getter
@Setter
public class ContractInvokeV2Request extends BaseBO {
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
     * 调用方法签名(方法名+参数列表+返回值，例如：(uint) balanceOf(address))
     */
    private String methodSignature;

    /**
     * 智能合约调用传入参数列表
     */
    private Object[] args;
}
