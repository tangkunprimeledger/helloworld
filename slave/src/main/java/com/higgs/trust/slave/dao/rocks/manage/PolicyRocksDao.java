package com.higgs.trust.slave.dao.rocks.manage;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.slave.dao.po.manage.PolicyPO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author tangfashuang
 */
@Service
@Slf4j
public class PolicyRocksDao extends RocksBaseDao<String, PolicyPO> {
    @Override protected String getColumnFamilyName() {
        return "policy";
    }

    public int batchInsert(List<PolicyPO> policyPOList) {
        if (CollectionUtils.isEmpty(policyPOList)) {
            log.info("[PolicyRocksDao.batchInsert] policyPOList is empty");
            return 0;
        }

        WriteBatch batch = new WriteBatch();
        for (PolicyPO po : policyPOList) {
            po.setCreateTime(new Date());
            batchPut(batch, po.getPolicyId(), po);
        }

        batchCommit(new WriteOptions(), batch);
        return policyPOList.size();
    }
}
