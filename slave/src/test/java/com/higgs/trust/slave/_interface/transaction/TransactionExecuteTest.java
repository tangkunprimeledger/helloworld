package com.higgs.trust.slave._interface.transaction;

import com.higgs.trust.slave._interface.InterfaceCommonTest;
import com.higgs.trust.slave.core.service.transaction.TransactionExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class TransactionExecuteTest extends InterfaceCommonTest{
    private static String PROVIDER_ROOT_PATH = "java/com/higgs/trust/slave/core/service/transaction/execute/";

    @Autowired
    private TransactionExecutor transactionExecutor;

    @Override protected String getProviderRootPath() {
        return PROVIDER_ROOT_PATH;
    }



}
