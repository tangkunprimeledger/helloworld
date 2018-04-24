package com.higgs.trust.slave.model.bo.account;

import com.higgs.trust.slave.model.bo.action.Action;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author liuyu
 * @description account unfreeze action BO
 * @date 2018-03-30
 */
@Getter @Setter public class AccountUnFreeze extends Action {
    /**
     * core flow no
     */
    @NotNull @Length(min = 1, max = 64) private String bizFlowNo;
    /**
     * account no
     */
    @NotNull @Length(min = 1, max = 64) private String accountNo;
    /**
     * unfreeze amount
     */
    @NotNull private BigDecimal amount;
    /**
     * remark
     */
    private String remark;
}
