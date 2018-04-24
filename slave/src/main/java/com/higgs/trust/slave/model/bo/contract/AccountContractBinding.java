package com.higgs.trust.slave.model.bo.contract;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter @Setter public class AccountContractBinding extends BaseBO {
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
