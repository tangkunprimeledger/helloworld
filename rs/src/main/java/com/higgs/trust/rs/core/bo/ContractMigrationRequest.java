package com.higgs.trust.rs.core.bo;

import com.higgs.trust.rs.common.BaseBO;
import lombok.Getter;
import lombok.Setter;

/**
 * @author duhongming
 * @date 2018/6/24
 */
@Getter
@Setter
public class ContractMigrationRequest extends BaseBO {
    private String txId;
    private String fromAddress;
    private String toAddress;
}
