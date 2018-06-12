package com.higgs.trust.slave.core.service.snapshot.agent;

import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.core.repository.contract.AccountContractBindingRepository;
import com.higgs.trust.slave.core.service.snapshot.CacheLoader;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.model.bo.BaseBO;
import com.higgs.trust.slave.model.bo.contract.AccountContractBinding;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * the snapshot agent of AccountContractBinding
 *
 * @author duhongming
 * @date 2018-04-19
 */
@Slf4j
@Component
public class AccountContractBindingSnapshotAgent implements CacheLoader {

    @Autowired
    SnapshotService snapshot;
    @Autowired
    AccountContractBindingRepository repository;

    /**
     * check exist binging relationship
     *
     * @param hash
     * @return
     */
    public AccountContractBinding getBinding(String hash) {
        AccountContractBinding binding = (AccountContractBinding) snapshot.get(SnapshotBizKeyEnum.ACCOUNT_CONTRACT_BIND, new BindingItemCacheKey(hash));
        return binding;
    }

    //TODO You  should provide insert and update method for yourself to use by using snapshot insert or uodate method .
    public void putBinding(AccountContractBinding binding) {
        // snapshot.put(SnapshotBizKeyEnum.ACCOUNT_CONTRACT_BIND, new BindingItemCacheKey(binding.getHash()), binding);
    }

    public List<AccountContractBinding> getListByAccount(String accountNo) {
        return (List<AccountContractBinding>) snapshot.get(SnapshotBizKeyEnum.ACCOUNT_CONTRACT_BIND, new AccountContractBindingCacheKey(accountNo));
    }

    //TODO You  should provide insert and update method for yourself to use by using snapshot insert or uodate method .
    public void put(String accountNo, List<AccountContractBinding> bindings) {
        //  snapshot.put(SnapshotBizKeyEnum.ACCOUNT_CONTRACT_BIND, new AccountContractBindingCacheKey(accountNo), bindings);
    }

    public void put(AccountContractBinding binding) {
        this.putBinding(binding);
        List<AccountContractBinding> list = this.getListByAccount(binding.getAccountNo());
        if (null == list) {
            list = new ArrayList<>();
        }
        list.add(binding);
        this.put(binding.getAccountNo(), list);
    }

    @Override
    public Object query(Object object) {
        if (object instanceof BindingItemCacheKey) {
            return repository.queryByHash(((BindingItemCacheKey) object).getBindHash());
        }

        if (object instanceof AccountContractBindingCacheKey) {
            String accountNo = ((AccountContractBindingCacheKey) object).getAccountNo();
            List<AccountContractBinding> bindings = repository.queryListByAccountNo(accountNo);
            return bindings;
        }

        log.error("unknow CacheKey object: {}", object.getClass().getName());
        return null;
    }


    /**
     * the method to batchInsert data into db
     *
     * @param insertMap
     * @return
     */
    //TODO to implements your own bachInsert method for db
    @Override
    public boolean batchInsert(Map<Object, Object> insertMap) {
        return false;
    }

    /**
     * the method to batchUpdate data into db
     *
     * @param updateMap
     * @return
     */
    //TODO to implements your own bachUpdate method for db
    @Override
    public boolean batchUpdate(Map<Object, Object> updateMap) {
        return false;
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountContractBindingCacheKey extends BaseBO {
        private String accountNo;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BindingItemCacheKey extends BaseBO {
        private String bindHash;
    }
}
