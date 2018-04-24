package com.higgs.trust.slave.core.service.datahandler.account;

import com.higgs.trust.slave.model.bo.account.*;

/**
 * @author liuyu
 * @description
 * @date 2018-04-16
 */
public interface AccountHandler {

    /**
     * get account info from cache or db
     *
     * @param accountNo
     * @return
     */
    AccountInfo getAccountInfo(String accountNo);

    /**
     * query currency info
     *
     * @param currency
     * @return
     */
    CurrencyInfo queryCurrency(String currency);

    /**
     * @param openAccount
     */
    void openAccount(OpenAccount openAccount);

    /**
     * validate account operation
     *
     * @param accountOperation
     * @param policyId
     */
    void validateForOperation(AccountOperation accountOperation, String policyId);

    /**
     * persist account operation
     *
     * @param accountOperation
     * @param blockHeight
     */
    void persistForOperation(AccountOperation accountOperation, Long blockHeight);

    /**
     * get account freeze record
     *
     * @param bizFlowNo
     * @param accountNo
     * @return
     */
    AccountFreezeRecord getAccountFreezeRecord(String bizFlowNo, String accountNo);

    /**
     * freeze
     *
     * @param accountFreeze
     * @param blockHeight
     */
    void freeze(AccountFreeze accountFreeze, Long blockHeight);

    /**
     * unfreeze
     *
     * @param accountUnFreeze
     * @param freezeRecord
     * @param blockHeight
     */
    void unfreeze(AccountUnFreeze accountUnFreeze, AccountFreezeRecord freezeRecord, Long blockHeight);

    /**
     * issue new currency
     *
     * @param bo
     */
    void issueCurrency(IssueCurrency bo);
}
