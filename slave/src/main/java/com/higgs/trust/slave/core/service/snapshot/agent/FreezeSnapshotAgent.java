package com.higgs.trust.slave.core.service.snapshot.agent;

import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.core.repository.account.FreezeRepository;
import com.higgs.trust.slave.core.service.snapshot.CacheLoader;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.model.bo.BaseBO;
import com.higgs.trust.slave.model.bo.account.AccountFreezeRecord;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author liuyu
 * @description an agent for account freeze snapshot
 * @date 2018-04-09
 */
@Slf4j @Component public class FreezeSnapshotAgent implements CacheLoader {
    @Autowired SnapshotService snapshot;
    @Autowired FreezeRepository freezeRepository;

    private <T> T get(Object key) {
        return (T)snapshot.get(SnapshotBizKeyEnum.FREEZE, key);
    }

    private void insert(Object key, Object object) {
        snapshot.insert(SnapshotBizKeyEnum.FREEZE, key, object);
    }

    private void update(Object key, Object object) {
        snapshot.update(SnapshotBizKeyEnum.FREEZE, key, object);
    }

    /**
     * get account freeze record from cache or db
     *
     * @param accountNo
     * @param bizFlowNo
     * @return
     */
    public AccountFreezeRecord getAccountFreezeRecord(String bizFlowNo, String accountNo) {
        return get(new FreezeCacheKey(bizFlowNo, accountNo));
    }

    /**
     * create account freeze record from snapshot
     *
     * @param accountFreezeRecord
     */
    public void createAccountFreezeRecord(AccountFreezeRecord accountFreezeRecord) {
        insert(new FreezeCacheKey(accountFreezeRecord.getBizFlowNo(), accountFreezeRecord.getAccountNo()),
            accountFreezeRecord);
    }

    /**
     * update account freeze record from snapshot
     *
     * @param accountFreezeRecord
     */
    public void updateAccountFreezeRecord(AccountFreezeRecord accountFreezeRecord) {
        update(new FreezeCacheKey(accountFreezeRecord.getBizFlowNo(), accountFreezeRecord.getAccountNo()),
            accountFreezeRecord);
    }

    /**
     * when cache is not exists,load from db
     */
    @Override public Object query(Object object) {
        if (object instanceof FreezeCacheKey) {
            FreezeCacheKey key = (FreezeCacheKey)object;
            return freezeRepository.queryByFlowNoAndAccountNo(key.getBizFlowNo(), key.getAccountNo());
        }
        log.error("not found load function for cache key:{}", object);
        return null;
    }

    /**
     * the method to batchInsert data into db
     *
     * @param insertMap
     * @return
     */
    @Override public boolean batchInsert(Map<Object, Object> insertMap) {
        if (insertMap == null || insertMap.isEmpty()) {
            return true;
        }
        List<AccountFreezeRecord> accountFreezeRecords = getFromMap(insertMap);
        if (!CollectionUtils.isEmpty(accountFreezeRecords)) {
            freezeRepository.batchInsert(accountFreezeRecords);
        }
        return true;
    }

    /**
     * the method to batchUpdate data into db
     *
     * @param updateMap
     * @return
     */
    @Override public boolean batchUpdate(Map<Object, Object> updateMap) {
        if (updateMap == null || updateMap.isEmpty()) {
            return true;
        }
        List<AccountFreezeRecord> accountFreezeRecords = getFromMap(updateMap);
        if (!CollectionUtils.isEmpty(accountFreezeRecords)) {
            freezeRepository.batchUpdate(accountFreezeRecords);
        }
        return true;
    }

    /**
     * get list from map
     *
     * @param map
     * @return
     */
    private List<AccountFreezeRecord> getFromMap(Map<Object, Object> map){
        List<AccountFreezeRecord> accountFreezeRecords = new ArrayList<>();
        for (Object key : map.keySet()) {
            if (key instanceof FreezeCacheKey) {
                accountFreezeRecords.add((AccountFreezeRecord)map.get(key));
            }
        }
        return accountFreezeRecords;
    }
    /**
     * the cache key of freeze info
     */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor public static class FreezeCacheKey extends BaseBO {
        private String bizFlowNo;
        private String accountNo;
    }
}
