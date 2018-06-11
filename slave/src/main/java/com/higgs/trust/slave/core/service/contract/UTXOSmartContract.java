package com.higgs.trust.slave.core.service.contract;

import com.higgs.trust.contract.ExecuteContextData;

public interface UTXOSmartContract {
    boolean isExist(String address);
    boolean execute(String address, ExecuteContextData data);
}