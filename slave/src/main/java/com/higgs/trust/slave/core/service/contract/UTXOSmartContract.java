package com.higgs.trust.slave.core.service.contract;

import com.higgs.trust.contract.ExecuteContextData;
import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;

public interface UTXOSmartContract {
    boolean execute(String code, ExecuteContextData data, TxProcessTypeEnum processType);
}