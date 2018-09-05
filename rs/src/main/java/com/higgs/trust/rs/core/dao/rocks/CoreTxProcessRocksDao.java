package com.higgs.trust.rs.core.dao.rocks;

import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.dao.po.CoreTransactionProcessPO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.WriteBatch;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author tangfashuang
 */
@Service
@Slf4j
public class CoreTxProcessRocksDao extends RocksBaseDao<CoreTransactionProcessPO>{
    @Override protected String getColumnFamilyName() {
        return "coreTransactionProcess";
    }

    public void saveWithTransaction(CoreTransactionProcessPO po) {
        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[CoreTxProcessRocksDao.saveWithTransaction] write batch is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        String key = po.getStatus() + Constant.SPLIT_SLASH + po.getTxId();
        if (keyMayExist(key) && null != get(key)) {
            log.error("[CoreTxProcessRocksDao.save] core transaction process is exist, key={}", key);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_ALREADY_EXIST);
        }

        po.setCreateTime(new Date());
        batchPut(batch, key, po);
    }

    public void updateStatus(String txId, String from, String to) {

        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[CoreTxProcessRocksDao.updateStatus] write batch is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        String key = from + Constant.SPLIT_SLASH + txId;
        //first query
        CoreTransactionProcessPO po = get(key);
        if (null == po) {
            log.error("[CoreTxProcessRocksDao.updateStatus] core transaction process is not exist, key={}", key);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_IS_NOT_EXIST);
        }

        po.setUpdateTime(new Date());
        po.setStatus(to);
        String newKey = to + Constant.SPLIT_SLASH + txId;
        //second delete
        batchDelete(batch, key);
        //last update(put)
        batchPut(batch, newKey, po);
    }

    public void batchInsert(List<CoreTransactionProcessPO> poList) {
        if (CollectionUtils.isEmpty(poList)) {
            return;
        }

        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[CoreTxProcessRocksDao.batchInsert] write batch is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        for (CoreTransactionProcessPO po : poList) {
            String key = po.getStatus() + Constant.SPLIT_SLASH + po.getTxId();
            po.setCreateTime(new Date());
            batchPut(batch, key, po);
        }
    }

    public void batchUpdate(List<CoreTransactionProcessPO> poList, String from, String to) {
        if (CollectionUtils.isEmpty(poList)){
            return;
        }

        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[CoreTxProcessRocksDao.batchUpdate] write batch is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        for (CoreTransactionProcessPO po : poList) {
            String key = from + Constant.SPLIT_SLASH + po.getTxId();
            CoreTransactionProcessPO oldPO = get(key);
            if (null == oldPO) {
                log.error("[CoreTxProcessRocksDao.batchUpdate] core transaction is not exist, key={}", key);
                throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_IS_NOT_EXIST);
            }
            if (!StringUtils.equals(from, oldPO.getStatus())) {
                log.error("[CoreTxProcessRocksDao.batchUpdate] status is not equal, key={}, po.status={}, from={}", key, oldPO.getStatus(), from);
                throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_UPDATE_STATUS_FAILED);
            }

            delete(key);
            oldPO.setStatus(to);
            oldPO.setUpdateTime(new Date());
            String newKey = to + Constant.SPLIT_SLASH + po.getTxId();
            batchPut(batch, newKey, oldPO);
        }
    }

    public void deleteEND(String status) {
       String beginKey = queryFirstKey(status);
       List<String> indexList = CoreTxStatusEnum.getIndexs(status);

       String endKey = null;
       boolean isEnd = false;
       for (String index : indexList) {
           String key = queryFirstKey(index);
           if (!StringUtils.isEmpty(key)) {
               endKey = key;
               isEnd = true;
               break;
           }
       }

       if (StringUtils.isEmpty(endKey)) {
           isEnd = false;
           endKey = queryLastKey();
       }

       if (StringUtils.isEmpty(beginKey) || StringUtils.isEmpty(endKey)) {
           return;
       }

       deleteRange(beginKey, endKey);
       if (!isEnd) {
           delete(endKey);
       }
    }

}
