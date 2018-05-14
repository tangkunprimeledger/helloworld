package com.higgs.trust.rs.custom.api.vo.blockchain;

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
@Getter @Setter public class AccountVO extends BaseBO {
    /**
     * 账号
     */
    private String accountNo;

    /**
     * currency
     */
    private String currency;

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
     * dataOwner
     */
    private String dataOwner;

    /**
     * status,NORMAL,DESTROY
     */
    private String status;
    /**
     * create time
     */
    private Date createTime;
}
