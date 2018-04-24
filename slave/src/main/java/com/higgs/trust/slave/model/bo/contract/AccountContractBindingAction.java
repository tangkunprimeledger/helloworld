package com.higgs.trust.slave.model.bo.contract;

import com.higgs.trust.slave.model.bo.action.Action;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * the action of  account contract binding
 * @author duhongming
 * @date 2018-04-18
 */
@Getter @Setter public class AccountContractBindingAction extends Action {


    /**
     * number of account,max len 64
     */
    @NotEmpty @Length(min = 1, max = 64) private String accountNo;

    /**
     * the address of contract to bind, length 64
     */
    @NotEmpty @Length(min = 64, max = 64) private String contractAddress;

    /**
     * the biz args
     */
    private String args;
}
