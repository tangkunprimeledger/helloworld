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
import org.rocksdb.Transaction;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author tangfashuang
 * @desc key: status_index + "_" + txId, value: CoreTransactionProcessPO
 */
@Service
@Slf4j
public class CoreTxProcessRocksDao extends RocksBaseDao<CoreTransactionProcessPO>{
    @Override protected String getColumnFamilyName() {
        return "coreTransactionProcess";
    }

    public void saveWithTransaction(CoreTransactionProcessPO po, String index) {
        String key = index + Constant.SPLIT_SLASH + po.getTxId();
        if (keyMayExist(key) && null != get(key)) {
            log.error("[CoreTxProcessRocksDao.save] core transaction process is exist, key={}", key);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_ALREADY_EXIST);
        }

        po.setCreateTime(new Date());

        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[CoreTxProcessRocksDao.saveWithTransaction] transaction is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_TRANSACTION_IS_NULL);
        }

        txPut(tx, key, po);
    }

    public void updateStatus(String txId, CoreTxStatusEnum from, CoreTxStatusEnum to) {

        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[CoreTxProcessRocksDao.updateStatus] transaction is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_TRANSACTION_IS_NULL);
        }

        String key = from.getIndex() + Constant.SPLIT_SLASH + txId;
        //first query
        CoreTransactionProcessPO po = get(key);
        if (null == po) {
            log.error("[CoreTxProcessRocksDao.updateStatus] core transaction process is not exist, key={}", key);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_IS_NOT_EXIST);
        }

        po.setUpdateTime(new Date());
        po.setStatus(to.getCode());
        String newKey = to.getIndex() + Constant.SPLIT_SLASH + txId;
        //second delete
        txDelete(tx, key);
        //last update(put)
        txPut(tx, newKey, po);
    }

    public void batchInsert(List<CoreTransactionProcessPO> poList, String index) {
        if (CollectionUtils.isEmpty(poList)) {
            return;
        }

        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[CoreTxProcessRocksDao.batchInsert] transaction is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_TRANSACTION_IS_NULL);
        }

        for (CoreTransactionProcessPO po : poList) {
            String key = index + Constant.SPLIT_SLASH + po.getTxId();
            po.setCreateTime(new Date());
            txPut(tx, key, po);
        }
    }

    public void batchUpdate(List<CoreTransactionProcessPO> poList, CoreTxStatusEnum from, CoreTxStatusEnum to) {
        if (CollectionUtils.isEmpty(poList)){
            return;
        }

        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[CoreTxProcessRocksDao.batchUpdate] transaction is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_TRANSACTION_IS_NULL);
        }

        for (CoreTransactionProcessPO po : poList) {
            String key = from.getIndex() + Constant.SPLIT_SLASH + po.getTxId();
            CoreTransactionProcessPO oldPO = get(key);
            if (null == oldPO) {
                log.error("[CoreTxProcessRocksDao.batchUpdate] core transaction is not exist, key={}", key);
                throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_IS_NOT_EXIST);
            }
            if (!StringUtils.equals(from.getCode(), oldPO.getStatus())) {
                log.error("[CoreTxProcessRocksDao.batchUpdate] status is not equal, key={}, po.status={}, from={}", key, oldPO.getStatus(), from);
                throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_UPDATE_STATUS_FAILED);
            }

            delete(key);
            oldPO.setStatus(to.getCode());
            oldPO.setUpdateTime(new Date());
            String newKey = to.getIndex() + Constant.SPLIT_SLASH + po.getTxId();
            txPut(tx, newKey, oldPO);
        }
    }

    public void deleteEND(String status) {
       String beginKey = queryFirstKey(status);
       List<String> indexList = CoreTxStatusEnum.getIndexList(status);

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

    public CoreTransactionProcessPO queryByTxIdAndStatus(String txId, String index) {
        if (!StringUtils.isEmpty(index)) {
            return get(index + Constant.SPLIT_SLASH + txId);

        }

        List<String> indexList = CoreTxStatusEnum.getIndexList(null);
        for (String i : indexList) {
            String key = i + Constant.SPLIT_SLASH + txId;
            CoreTransactionProcessPO po = get(key);
            if (null != po) {
                return get(key);
            }
        }
        return null;
    }
}
