package com.higgs.trust.rs.core.dao.rocks;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.dao.po.VoteRequestRecordPO;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.Transaction;
import org.rocksdb.WriteBatch;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author tangfashuang
 * @desc key: txId, value: voteRequestRecordPO
 */
@Service
@Slf4j
public class VoteRequestRecordRocksDao extends RocksBaseDao<VoteRequestRecordPO> {
    @Override protected String getColumnFamilyName() {
        return "voteRequestRecord";
    }

    public void saveWithTransaction(VoteRequestRecordPO voteRequestRecordPO) {
        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[VoteRequestRecordRocksDao.saveWithTransaction] transaction is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_TRANSACTION_IS_NULL);
        }

        String key = voteRequestRecordPO.getTxId();
        if (keyMayExist(key) && null != get(key)) {
            log.error("[VoteRequestRecordRocksDao.save] vote request record is exist, txId={}", key);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_ALREADY_EXIST);
        }

        voteRequestRecordPO.setCreateTime(new Date());
        txPut(tx, key, voteRequestRecordPO);
    }

    public void setVoteResult(String txId, String sign, String code) {
        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[VoteRequestRecordRocksDao.setVoteResult] transaction is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_TRANSACTION_IS_NULL);
        }

        VoteRequestRecordPO po = get(txId);
        if (null == po) {
            log.error("[VoteRequestRecordRocksDao.setVoteResult] vote request record is null, txId={}", txId);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_IS_NOT_EXIST);
        }

        po.setUpdateTime(new Date());
        po.setVoteResult(code);
        po.setSign(sign);

        txPut(tx, txId, po);
    }
}
