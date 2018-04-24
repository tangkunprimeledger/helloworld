package com.higgs.trust.slave.core.service.snapshot.agent;

import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.core.repository.account.FreezeRepository;
import com.higgs.trust.slave.core.service.snapshot.CacheLoader;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.model.bo.account.AccountFreezeRecord;
import com.higgs.trust.slave.model.bo.snapshot.CacheKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author liuyu
 * @description an agent for account freeze snapshot
 * @date 2018-04-09
 */
@Slf4j @Component public class FreezeSnapshotAgent implements CacheLoader {
    @Autowired SnapshotService snapshot;
    @Autowired FreezeRepository freezeRepository;

    private <T> T get(Object key){
        return (T)snapshot.get(SnapshotBizKeyEnum.FREEZE,key);
    }
    private void put(Object key,Object object){
        snapshot.put(SnapshotBizKeyEnum.FREEZE,key,object);
    }
    /**
     * get account freeze record from cache or db
     *
     * @param accountNo
     * @param bizFlowNo
     * @return
     */
    public AccountFreezeRecord getAccountFreezeRecord(String bizFlowNo, String accountNo) {
        return get(new FreezeCacheKey(bizFlowNo,accountNo));
    }

    /**
     * create account freeze record from snapshot
     *
     * @param accountFreezeRecord
     */
    public void createAccountFreezeRecord(AccountFreezeRecord accountFreezeRecord) {
        put(new FreezeCacheKey(accountFreezeRecord.getBizFlowNo(),accountFreezeRecord.getAccountNo()),accountFreezeRecord);
    }

    /**
     * update account freeze record from snapshot
     *
     * @param accountFreezeRecord
     */
    public void updateAccountFreezeRecord(AccountFreezeRecord accountFreezeRecord) {
        put(new FreezeCacheKey(accountFreezeRecord.getBizFlowNo(),accountFreezeRecord.getAccountNo()),accountFreezeRecord);
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
     * the cache key of freeze info
     */
    @Getter @Setter @AllArgsConstructor public class FreezeCacheKey extends CacheKey {
        private String bizFlowNo;
        private String accountNo;
    }
}
