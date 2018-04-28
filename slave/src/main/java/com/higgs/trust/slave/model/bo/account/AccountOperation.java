package com.higgs.trust.slave.model.bo.account;

import com.higgs.trust.slave.model.bo.action.Action;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @author liuyu
 * @description BO of account operation
 * @date 2018-03-28
 */
@Getter @Setter public class AccountOperation extends Action {
    /**
     * the business flow number
     */
    @NotEmpty @Length(min = 1, max = 64) private String bizFlowNo;
    /**
     * debit transaction information
     */
    @NotEmpty @Valid private List<AccountTradeInfo> debitTradeInfo;
    /**
     * credit transaction information
     */
    @NotEmpty @Valid private List<AccountTradeInfo> creditTradeInfo;
    /**
     * account operation datetime
     */
    @NotNull private Date accountDate;
    /**
     * remark
     */
    private String remark;
}
