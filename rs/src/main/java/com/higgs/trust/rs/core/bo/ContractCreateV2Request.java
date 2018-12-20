package com.higgs.trust.rs.core.bo;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author duhongming
 * @date 2018/6/24
 */
@Getter @Setter public class ContractCreateV2Request {
    @NotBlank
    @Length(max =64)
    private String txId;

    @NotBlank
    @Length(max =64)
    private String fromAddr;
    /**
     * 合约地址
     */
    @NotBlank
    @Length(max =64)
    private String contractAddress;
    /**
     * 合约构造器
     */
    @NotBlank
    private String contractor;
    /**
     * 合约代码
     */
    @NotBlank
    private String sourceCode;
    /**
     * 合约构造入参
     */
    private Object[] initArgs;
}
