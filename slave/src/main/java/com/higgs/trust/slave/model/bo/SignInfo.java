package com.higgs.trust.slave.model.bo;

import lombok.Getter;
import lombok.Setter;

/**
 * @author liuyu
 * @description
 * @date 2018-06-06
 */
@Getter @Setter public class SignInfo extends BaseBO {
    /**
     * who`s sign,rs-name
     */
    private String owner;
    /**
     * the sign data
     */
    private String sign;
}
