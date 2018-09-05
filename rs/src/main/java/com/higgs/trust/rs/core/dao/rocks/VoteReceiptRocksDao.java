package com.higgs.trust.rs.core.dao.rocks;

import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.dao.po.VoteReceiptPO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.rocksdb.WriteBatch;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author tangfashuang
 */
@Slf4j
@Service
public class VoteReceiptRocksDao extends RocksBaseDao<VoteReceiptPO> {
    @Override protected String getColumnFamilyName() {
        return "voteReceipt";
    }

    public void save(VoteReceiptPO voteReceiptPO) {
        String key = voteReceiptPO.getTxId() + Constant.SPLIT_SLASH + voteReceiptPO .getVoter();
        if (keyMayExist(key) && null != get(key)) {
            log.error("[VoteReceiptRocksDao.save] vote receipt is exist, key={}", key);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_ALREADY_EXIST);
        }
        voteReceiptPO.setCreateTime(new Date());
        put(key, voteReceiptPO);
    }

    public void batchInsert(List<VoteReceiptPO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[VoteReceiptRocksDao.batchInsert] write batch is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        for (VoteReceiptPO po : list) {
            String key = po.getTxId() + Constant.SPLIT_SLASH + po.getVoter();
            po.setCreateTime(new Date());
            batchPut(batch, key, po);
        }
    }

    public List<VoteReceiptPO> queryByTxId(String txId) {
        return queryByPrefix(txId);
    }
}
