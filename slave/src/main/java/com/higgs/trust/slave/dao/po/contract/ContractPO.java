package com.higgs.trust.slave.dao.po.contract;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Contract persist object
 * @author duhongming
 * @date 2018-04-12
 */
@Getter @Setter public class ContractPO {
    private long id;
    private String address;
    private String language;
    private String code;
    private Date createTime;
}
