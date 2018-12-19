package com.higgs.trust.rs.core.bo;

import lombok.Getter;
import lombok.Setter;

/**
 * @author duhongming
 * @date 2018/6/24
 */
@Getter
@Setter
public class ContractCreateV2Request {
    private String fromAddr;
    /**
     * 合约地址
     */
    private String contractAddress;
    /**
     * 合约构造器
     */
    private String contractor;
    /**
     * 合约代码
     */
    private String sourceCode;
    /**
     * 合约构造入参
     */
    private Object[] initArgs;
}
