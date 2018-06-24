package com.higgs.trust.rs.core.bo;

import lombok.Getter;
import lombok.Setter;

/**
 * @author duhongming
 * @date 2018/6/24
 */
@Getter
@Setter
public class ContractCreateRequest {
    private String code;
    private Object[] initArgs;
}
