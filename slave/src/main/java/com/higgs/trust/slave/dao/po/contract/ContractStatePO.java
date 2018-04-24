package com.higgs.trust.slave.dao.po.contract;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContractStatePO {
    private long id;
    private String address;
    private String state;
}
