package com.higgs.trust.slave.dao.rocks.transaction;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.transaction.TransactionPO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.rocksdb.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author tangfashuang
 */
@Service
@Slf4j
public class TxRocksDao extends RocksBaseDao<TransactionPO> {
    @Override protected String getColumnFamilyName() {
        return "tx";
    }

    public void batchInsert(List<TransactionPO> pos) {
        if (CollectionUtils.isEmpty(pos)) {
            log.info("[TxRocksDao.batchInsert] pos is empty");
            return;
        }

        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[TxRocksDao.batchInsert] transaction is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_TRANSACTION_IS_NULL);
        }
        for (TransactionPO po : pos) {
            txPut(tx, po.getTxId(), po);
        }
    }
}
