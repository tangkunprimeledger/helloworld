package com.higgs.trust.slave.core.service.snapshot.agent;

import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.core.repository.account.AccountRepository;
import com.higgs.trust.slave.core.repository.account.CurrencyRepository;
import com.higgs.trust.slave.core.service.snapshot.CacheLoader;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.model.bo.DataIdentity;
import com.higgs.trust.slave.model.bo.account.AccountInfo;
import com.higgs.trust.slave.model.bo.account.CurrencyInfo;
import com.higgs.trust.slave.model.bo.account.IssueCurrency;
import com.higgs.trust.slave.model.bo.account.OpenAccount;
import com.higgs.trust.slave.model.bo.snapshot.CacheKey;
import com.higgs.trust.slave.model.convert.DataIdentityConvert;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author liuyu
 * @description an agent for account snapshot
 * @date 2018-04-09
 */
@Slf4j @Component public class AccountSnapshotAgent implements CacheLoader {
    @Autowired SnapshotService snapshot;
    @Autowired AccountRepository accountRepository;
    @Autowired CurrencyRepository currencyRepository;
    @Autowired DataIdentitySnapshotAgent dataIdentitySnapshotAgent;

    private <T> T get(Object key) {
        return (T)snapshot.get(SnapshotBizKeyEnum.ACCOUNT, key);
    }

    private void put(Object key, Object object) {
        snapshot.put(SnapshotBizKeyEnum.ACCOUNT, key, object);
    }

    /**
     * get account info from cache or db
     *
     * @param accountNo
     * @return
     */
    public AccountInfo getAccountInfo(String accountNo) {
        return get(new AccountCacheKey(accountNo));
    }

    /**
     * query currency info
     *
     * @param currency
     * @return
     */
    public CurrencyInfo queryCurrency(String currency) {
        return get(new CurrencyInfoCacheKey(currency));
    }

    /**
     * open account info,return a new account
     *
     * @param bo
     */
    public AccountInfo openAccount(OpenAccount bo) {
        // account info
        AccountInfo accountInfo = accountRepository.buildAccountInfo(bo);
        //save account info to snapshot
        put(new AccountCacheKey(accountInfo.getAccountNo()), accountInfo);
        // data identity
        DataIdentity dataIdentity =
            DataIdentityConvert.buildDataIdentity(bo.getAccountNo(), bo.getChainOwner(), bo.getDataOwner());
        // save snapshot
        dataIdentitySnapshotAgent.saveDataIdentity(dataIdentity);
        return accountInfo;
    }

    /**
     * update account info from snapshot
     *
     * @param accountInfo
     */
    public void updateAccountInfo(AccountInfo accountInfo) {
        put(new AccountCacheKey(accountInfo.getAccountNo()), accountInfo);
    }

    /**
     * isssue currency by snapshot
     *
     * @param bo
     */
    public void issueCurrency(IssueCurrency bo) {
        CurrencyInfo currencyInfo = currencyRepository.buildCurrencyInfo(bo.getCurrencyName(), bo.getRemark());
        put(new CurrencyInfoCacheKey(bo.getCurrencyName()), currencyInfo);
    }

    /**
     * when cache is not exists,load from db
     */
    @Override public Object query(Object object) {
        //query account info
        if (object instanceof AccountCacheKey) {
            AccountCacheKey key = (AccountCacheKey)object;
            return accountRepository.queryAccountInfo(String.valueOf(key.getAccountNo()), false);
            //query currency info
        } else if (object instanceof CurrencyInfoCacheKey) {
            CurrencyInfoCacheKey key = (CurrencyInfoCacheKey)object;
            return currencyRepository.queryByCurrency(key.getCurrency());
        }
        log.error("not found load function for cache key:{}", object);
        return null;
    }

    /**
     * the cache key of account info
     */
    @Getter @Setter @AllArgsConstructor public class AccountCacheKey extends CacheKey {
        private String accountNo;
    }

    /**
     * the cache key of currency info
     */
    @Getter @Setter @AllArgsConstructor public class CurrencyInfoCacheKey extends CacheKey {
        private String currency;
    }
}
