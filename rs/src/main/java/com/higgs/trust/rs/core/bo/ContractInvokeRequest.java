package com.higgs.trust.rs.core.bo;

import com.higgs.trust.rs.common.BaseBO;
import lombok.Getter;
import lombok.Setter;

/**
 * @author duhongming
 * @date 2018/5/18
 */
@Getter @Setter public class ContractInvokeRequest extends BaseBO {
    private String address;
    private Object[] bizArgs;
}
