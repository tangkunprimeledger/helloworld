package com.higgs.trust.slave.model.bo.account;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author liuyu
 * @description
 * @date 2018-04-10
 */
@Getter @Setter public class AccountFreezeRecord extends BaseBO {
    /**
     * id
     */
    private Long id;
    /**
     * business flow number
     */
    private String bizFlowNo;
    /**
     * accountNo
     */
    private String accountNo;
    /**
     * block height
     */
    private Long blockHeight;
    /**
     * the contract address
     */
    private String contractAddr;
    /**
     * the amount for freeze
     */
    private BigDecimal amount;
    /**
     * create time
     */
    private Date createTime;
    /**
     * update time
     */
    private Date updateTime;
}
