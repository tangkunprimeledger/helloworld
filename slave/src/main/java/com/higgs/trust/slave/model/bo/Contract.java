package com.higgs.trust.slave.model.bo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author duhongming
 * @description the modal of Contract
 * @date 2018-04-08
 */
@Getter @Setter public class Contract extends BaseBO {
    private String address;
    private String language;
    private String code;
    private Date createTime;
}
