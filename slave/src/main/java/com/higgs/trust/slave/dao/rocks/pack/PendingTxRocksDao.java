package com.higgs.trust.slave.dao.rocks.pack;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.rocksdb.WriteBatch;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author tangfashuang
 */
@Slf4j @Service public class PendingTxRocksDao extends RocksBaseDao<Long> {
    @Override protected String getColumnFamilyName() {
        return "pendingTransaction";
    }

    public List<String> getTxIds(List<String> txIds) {
        if (CollectionUtils.isEmpty(txIds)) {
            log.error("[PendingTxRocksDao.getTxIds] txIds is null");
            return null;
        }
        List<String> resultList = new ArrayList<>();
        Map<String, Long> resultMap = multiGet(txIds);
        if (!MapUtils.isEmpty(resultMap)) {
            for (String key : resultMap.keySet()) {
                resultList.add(key);
            }
        }

        return resultList;
    }

    public void batchInsert(List<String> txIds, Long packHeight) {
        if (CollectionUtils.isEmpty(txIds) || packHeight == null || packHeight < 1) {
            log.error("[PendingTxRocksDao.batchInsert] txIds is empty or packHeight is null");
            return;
        }
        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[PendingTxRocksDao.batchInsert] write batch is null");
            throw new SlaveException(SlaveErrorEnum.SLAVE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        for (String txId : txIds) {
            batchPut(batch, txId, packHeight);
        }
    }

}
