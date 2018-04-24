package com.higgs.trust.slave.model.bo.account;

import com.higgs.trust.slave.model.bo.action.Action;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author liuyu
 * @description account freeze action BO
 * @date 2018-03-30
 */
@Getter @Setter public class AccountFreeze extends Action {
    /**
     * core flow no
     */
    @NotNull @Length(min = 1, max = 64) private String bizFlowNo;
    /**
     * account no
     */
    @NotNull @Length(min = 1, max = 64) private String accountNo;
    /**
     * freeze amount
     */
    @NotNull private BigDecimal amount;
    /**
     * contract address
     */
    private String contractAddr;
    /**
     * remark
     */
    private String remark;
}
