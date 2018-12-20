package com.higgs.trust.slave.model.bo.contract;

import com.higgs.trust.slave.model.bo.action.Action;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
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
     * if transfer，which is transfering amount
     */
    private BigDecimal value;
    /**
     * 调用方法(返回值类型+方法名+参数类型，例如：(uint) balanceOf(address))
     */
    private String methodSignature;

    /**
     * 智能合约调用传入参数列表
     */
    private Object[] args;

    /**
     * tx create address
     */
    private String from;

    /**
     * contract address
     */
    private String to;
}
