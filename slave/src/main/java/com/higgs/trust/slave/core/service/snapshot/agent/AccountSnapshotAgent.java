package com.higgs.trust.slave.core.service.snapshot.agent;

import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.core.repository.account.AccountRepository;
import com.higgs.trust.slave.core.repository.account.CurrencyRepository;
import com.higgs.trust.slave.core.service.snapshot.CacheLoader;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.model.bo.BaseBO;
import com.higgs.trust.slave.model.bo.DataIdentity;
import com.higgs.trust.slave.model.bo.account.AccountInfo;
import com.higgs.trust.slave.model.bo.account.CurrencyInfo;
import com.higgs.trust.slave.model.bo.account.IssueCurrency;
import com.higgs.trust.slave.model.bo.account.OpenAccount;
import com.higgs.trust.slave.model.convert.DataIdentityConvert;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author liuyu
 * @description an agent for account snapshot
 * @date 2018-04-09
 */
@Slf4j
@Component
public class AccountSnapshotAgent implements CacheLoader {
    @Autowired
    SnapshotService snapshot;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    CurrencyRepository currencyRepository;
    @Autowired
    DataIdentitySnapshotAgent dataIdentitySnapshotAgent;

    private <T> T get(Object key) {
        return (T) snapshot.get(SnapshotBizKeyEnum.ACCOUNT, key);
    }

    private void insert(Object key, Object object) {
        snapshot.insert(SnapshotBizKeyEnum.ACCOUNT, key, object);
    }

    private void update(Object key, Object object) {
        snapshot.update(SnapshotBizKeyEnum.ACCOUNT, key, object);
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
        insert(new AccountCacheKey(accountInfo.getAccountNo()), accountInfo);
        // data identity
        DataIdentity dataIdentity = DataIdentityConvert.buildDataIdentity(bo.getAccountNo(), bo.getChainOwner(), bo.getDataOwner());
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
        update(new AccountCacheKey(accountInfo.getAccountNo()), accountInfo);
    }

    /**
     * isssue currency by snapshot
     *
     * @param bo
     */
    public void issueCurrency(IssueCurrency bo) {
        CurrencyInfo currencyInfo = currencyRepository.buildCurrencyInfo(bo.getCurrencyName(), bo.getRemark());
        insert(new CurrencyInfoCacheKey(bo.getCurrencyName()), currencyInfo);
    }

    /**
     * when cache is not exists,load from db
     */
    @Override
    public Object query(Object object) {
        //query account info
        if (object instanceof AccountCacheKey) {
            AccountCacheKey key = (AccountCacheKey) object;
            return accountRepository.queryAccountInfo(String.valueOf(key.getAccountNo()), false);
            //query currency info
        } else if (object instanceof CurrencyInfoCacheKey) {
            CurrencyInfoCacheKey key = (CurrencyInfoCacheKey) object;
            return currencyRepository.queryByCurrency(key.getCurrency());
        }
        log.error("not found load function for cache key:{}", object);
        return null;
    }


    /**
     * the method to batchInsert data into db
     *
     * @param insertList
     * @return
     */
    @Override
    public boolean batchInsert(List<Pair<Object, Object>> insertList) {
        if (CollectionUtils.isEmpty(insertList)) {
            return true;
        }
        List<AccountInfo> accountInfos = new ArrayList<>();
        List<CurrencyInfo> currencyInfos = new ArrayList<>();
        for (Pair<Object, Object> pair : insertList) {
            if (pair.getLeft() instanceof AccountCacheKey) {
                accountInfos.add((AccountInfo) pair.getRight());
            } else if (pair.getLeft() instanceof CurrencyInfoCacheKey) {
                currencyInfos.add((CurrencyInfo) pair.getRight());
            }
        }
        if (!CollectionUtils.isEmpty(accountInfos)) {
            accountRepository.batchInsert(accountInfos);
        }
        if (!CollectionUtils.isEmpty(currencyInfos)) {
            currencyRepository.batchInsert(currencyInfos);
        }
        return true;
    }

    /**
     * the method to batchUpdate data into db
     *
     * @param updateList
     * @return
     */
    @Override
    public boolean batchUpdate(List<Pair<Object, Object>> updateList) {
        if (CollectionUtils.isEmpty(updateList)) {
            log.info("[updateMap]updateMap is empty");
            return true;
        }
        List<AccountInfo> accountInfos = new ArrayList<>();
        for (Pair<Object, Object> pair : updateList) {
            if (pair.getLeft() instanceof AccountCacheKey) {
                accountInfos.add((AccountInfo) pair.getRight());
            }
        }
        if (!CollectionUtils.isEmpty(accountInfos)) {
            accountRepository.batchUpdate(accountInfos);
        }
        return true;
    }

    /**
     * the cache key of account info
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountCacheKey extends BaseBO {
        private String accountNo;
    }

    /**
     * the cache key of currency info
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrencyInfoCacheKey extends BaseBO {
        private String currency;
    }
}
