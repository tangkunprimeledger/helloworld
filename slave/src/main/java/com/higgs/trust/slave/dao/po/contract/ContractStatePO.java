package com.higgs.trust.slave.dao.po.contract;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ContractStatePO {
    private long id;
    private String address;
    private Date updateTime;
    private String state;
    private String keyDesc;
}
