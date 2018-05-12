package com.higgs.trust.slave.core.service.contract;

import com.higgs.trust.contract.ExecuteContextData;
import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;

public interface UTXOSmartContract {
    boolean isExist(String address, TxProcessTypeEnum processType);
    boolean execute(String address, ExecuteContextData data, TxProcessTypeEnum processType);
}