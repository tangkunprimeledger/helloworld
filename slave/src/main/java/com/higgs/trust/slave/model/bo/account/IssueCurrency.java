package com.higgs.trust.slave.model.bo.account;

import com.higgs.trust.slave.model.bo.action.Action;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author liuyu
 * @description
 * @date 2018-04-19
 */
@Getter @Setter public class IssueCurrency extends Action {

    @NotNull @Length(min = 1, max = 24) private String currencyName;
    private String remark;
}
