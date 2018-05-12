package com.higgs.trust.slave.api.vo;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author liuyu
 * @description
 * @date 2018-05-09
 */
@Getter @Setter public class AccountInfoVO extends BaseBO {
    /**
     * 账号
     */
    private String accountNo;
    /**
     * 余额(含冻结金额)
     */
    private BigDecimal balance;
    /**
     * freeze amount of account
     */
    private BigDecimal freezeAmount;
    /**
     * fund direction-DEBIT,CREDIT
     */
    private String fundDirection;
    /**
     * status,NORMAL,DESTROY
     */
    private String status;
    /**
     * create time
     */
    private Date createTime;
}
