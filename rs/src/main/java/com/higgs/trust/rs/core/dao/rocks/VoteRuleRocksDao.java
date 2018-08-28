package com.higgs.trust.rs.core.dao.rocks;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.dao.po.VoteRulePO;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.rocksdb.WriteBatch;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author tangfashuang
 */
@Service
@Slf4j
public class VoteRuleRocksDao extends RocksBaseDao<String, VoteRulePO>{

    @Override protected String getColumnFamilyName() {
        return "voteRule";
    }

    public void add(VoteRulePO voteRulePO) {
        String policyId = voteRulePO.getPolicyId();
        if (null != get(policyId)) {
            log.error("[VoteRuleRocksDao.add] vote rule  is already exist, policyId={}", policyId);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_ALREADY_EXIST);
        }
        voteRulePO.setCreateTime(new Date());
        put(policyId, voteRulePO);
    }

    public void batchInsert(List<VoteRulePO> voteRulePOs) {
        if (CollectionUtils.isEmpty(voteRulePOs)) {
            return;
        }

        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[VoteRuleRocksDao.batchInsert] write batch is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        for (VoteRulePO po : voteRulePOs) {
            po.setCreateTime(new Date());
            batchPut(batch, po.getPolicyId(), po);
        }
    }
}
