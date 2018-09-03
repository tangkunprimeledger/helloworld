package com.higgs.trust.rs.core.dao.rocks;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.dao.po.VoteRequestRecordPO;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.WriteBatch;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author tangfashuang
 */
@Service
@Slf4j
public class VoteRequestRecordRocksDao extends RocksBaseDao<VoteRequestRecordPO> {
    @Override protected String getColumnFamilyName() {
        return "voteRequestRecord";
    }

    public void saveWithTransaction(VoteRequestRecordPO voteRequestRecordPO) {
        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[VoteRequestRecordRocksDao.saveWithTransaction] write batch is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        String key = voteRequestRecordPO.getTxId();
        if (null != get(key)) {
            log.error("[VoteRequestRecordRocksDao.save] vote request record is exist, txId={}", key);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_ALREADY_EXIST);
        }

        voteRequestRecordPO.setCreateTime(new Date());
        batchPut(batch, key, voteRequestRecordPO);
    }

    public void setVoteResult(String txId, String sign, String code) {
        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[VoteRequestRecordRocksDao.setVoteResult] write batch is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        VoteRequestRecordPO po = get(txId);
        if (null == po) {
            log.error("[VoteRequestRecordRocksDao.setVoteResult] vote request record is null, txId={}", txId);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_IS_NOT_EXIST);
        }

        po.setUpdateTime(new Date());
        po.setVoteResult(code);
        po.setSign(sign);

        batchPut(batch, txId, po);
    }
}
