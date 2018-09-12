package com.higgs.trust.slave.dao.rocks.manage;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.manage.PolicyPO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.rocksdb.Transaction;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author tangfashuang
 */
@Service
@Slf4j
public class PolicyRocksDao extends RocksBaseDao<PolicyPO> {
    @Override protected String getColumnFamilyName() {
        return "policy";
    }

    public int batchInsert(List<PolicyPO> policyPOList) {
        if (CollectionUtils.isEmpty(policyPOList)) {
            log.info("[PolicyRocksDao.batchInsert] policyPOList is empty");
            return 0;
        }

        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[PolicyRocksDao.batchInsert] transaction is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_TRANSACTION_IS_NULL);
        }

        for (PolicyPO po : policyPOList) {
            po.setCreateTime(new Date());
            txPut(tx, po.getPolicyId(), po);
        }
        return policyPOList.size();
    }
}
