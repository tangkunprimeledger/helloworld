package com.higgs.trust.slave.dao.po.contract;

import com.higgs.trust.common.mybatis.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter @Setter public class AccountContractBindingPO extends BaseEntity<AccountContractBindingPO> {
    private Long id;
    private Long blockHeight;
    private String txId;
    private int actionIndex;
    private String accountNo;
    private String contractAddress;
    private String args;
    private String hash;
    private Date createTime;
}
