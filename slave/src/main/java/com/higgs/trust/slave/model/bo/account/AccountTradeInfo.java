package com.higgs.trust.slave.model.bo.account;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import java.math.BigDecimal;

/**
 * @author liuyu
 * @description Account transaction information
 * @date 2018-03-28
 */
@Getter @Setter public class AccountTradeInfo extends BaseBO {
    /**
     * number of account
     */
    @NotEmpty @Length(min = 1, max = 64) private String accountNo;
    /**
     * happen amount,allow the negative
     */
    @NotEmpty private BigDecimal amount;

    public AccountTradeInfo(String accountNo, BigDecimal amount) {
        this.accountNo = accountNo;
        this.amount = amount;
    }
}
