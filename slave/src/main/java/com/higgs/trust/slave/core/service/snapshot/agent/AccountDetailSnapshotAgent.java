package com.higgs.trust.slave.core.service.snapshot.agent;

import com.higgs.trust.slave.api.enums.SnapshotBizKeyEnum;
import com.higgs.trust.slave.core.repository.account.AccountRepository;
import com.higgs.trust.slave.core.repository.account.FreezeRepository;
import com.higgs.trust.slave.core.service.snapshot.CacheLoader;
import com.higgs.trust.slave.core.service.snapshot.SnapshotService;
import com.higgs.trust.slave.model.bo.account.AccountDcRecord;
import com.higgs.trust.slave.model.bo.account.AccountDetail;
import com.higgs.trust.slave.model.bo.account.AccountDetailFreeze;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
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
@Slf4j @Component public class AccountDetailSnapshotAgent implements CacheLoader {
    @Autowired SnapshotService snapshot;
    @Autowired AccountRepository accountRepository;
    @Autowired FreezeRepository freezeRepository;

    private void insert(Object key, Object object) {
        snapshot.insert(SnapshotBizKeyEnum.ACCOUNT_DETAIL, key, object);
    }

    /**
     * create account detail
     *
     * @param detail
     */
    public void createAccountDetail(AccountDetail detail) {
        insert(detail, detail);
    }

    /**
     * create account DC record
     *
     * @param dcRecord
     */
    public void createAccountDCRecord(AccountDcRecord dcRecord) {
        insert(dcRecord, dcRecord);
    }

    /**
     * create account detail freeze
     *
     * @param detailFreeze
     */
    public void createAccountDetailFreeze(AccountDetailFreeze detailFreeze) {
        insert(detailFreeze, detailFreeze);
    }

    /**
     * when cache is not exists,load from db
     */
    @Override public Object query(Object object) {
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
        List<AccountDetail> accountDetails = new ArrayList<>();
        List<AccountDcRecord> dcRecords = new ArrayList<>();
        List<AccountDetailFreeze> detailFreezes = new ArrayList<>();
        for (Object key : insertMap.keySet()) {
            if (key instanceof AccountDetail) {
                accountDetails.add((AccountDetail)insertMap.get(key));
                continue;
            }
            if (key instanceof AccountDcRecord) {
                dcRecords.add((AccountDcRecord)insertMap.get(key));
                continue;
            }
            if (key instanceof AccountDetailFreeze) {
                detailFreezes.add((AccountDetailFreeze)insertMap.get(key));
                continue;
            }
        }
        if (!CollectionUtils.isEmpty(accountDetails)) {
            accountRepository.batchInsertAccountDetail(accountDetails);
        }
        if (!CollectionUtils.isEmpty(dcRecords)) {
            accountRepository.batchInsertDcRecords(dcRecords);
        }
        if (!CollectionUtils.isEmpty(detailFreezes)) {
            freezeRepository.batchInsertDetailFreezes(detailFreezes);
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
        return true;
    }
}
