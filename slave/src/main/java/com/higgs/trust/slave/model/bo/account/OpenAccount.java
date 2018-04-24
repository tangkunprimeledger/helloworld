package com.higgs.trust.slave.model.bo.account;

import com.higgs.trust.slave.api.enums.account.FundDirectionEnum;
import com.higgs.trust.slave.model.bo.action.Action;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * @author liuyu
 * @description BO of open account action
 * @date 2018-03-27
 */
@Getter @Setter public class OpenAccount extends Action {
    /**
     * owner of chain
     */
    @NotEmpty @Length(min = 1, max = 24) private String chainOwner;
    /**
     * owner of data(RS)
     */
    @NotEmpty @Length(min = 1, max = 24) private String dataOwner;
    /**
     * number of account,max len 64
     */
    @NotEmpty @Length(min = 1, max = 64) private String accountNo;
    /**
     * currency of account
     */
    @NotEmpty @Length(min = 1, max = 24) private String currency;
    /**
     * fund direction DEBIT or CREDIT
     */
    @NotNull private FundDirectionEnum fundDirection;
}
