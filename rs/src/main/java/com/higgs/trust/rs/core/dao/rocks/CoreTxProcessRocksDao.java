package com.higgs.trust.rs.core.dao.rocks;

import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
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
public class CoreTxProcessRocksDao extends RocksBaseDao<CoreTransactionPO>{
    @Override protected String getColumnFamilyName() {
        return "coreTransactionProcess";
    }

    public void saveWithTransaction(CoreTransactionPO po, String index) {
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
        CoreTransactionPO po = get(key);
        if (null == po) {
            log.error("[CoreTxProcessRocksDao.updateStatus] core transaction process is not exist, key={}", key);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_IS_NOT_EXIST);
        }

        po.setUpdateTime(new Date());
        String newKey = to.getIndex() + Constant.SPLIT_SLASH + txId;
        //second delete
        txDelete(tx, key);
        //last update(put)
        txPut(tx, newKey, po);
    }

    public void batchInsert(List<CoreTransactionPO> poList, String index) {
        if (CollectionUtils.isEmpty(poList)) {
            return;
        }

        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[CoreTxProcessRocksDao.batchInsert] transaction is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_TRANSACTION_IS_NULL);
        }

        for (CoreTransactionPO po : poList) {
            String key = index + Constant.SPLIT_SLASH + po.getTxId();
            po.setCreateTime(new Date());
            txPut(tx, key, po);
        }
    }

    public void batchUpdate(List<CoreTransactionPO> poList, CoreTxStatusEnum from, CoreTxStatusEnum to) {
        if (CollectionUtils.isEmpty(poList)){
            return;
        }

        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[CoreTxProcessRocksDao.batchUpdate] transaction is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_TRANSACTION_IS_NULL);
        }

        for (CoreTransactionPO po : poList) {
            String key = from.getIndex() + Constant.SPLIT_SLASH + po.getTxId();
            txDelete(tx, key);
            String newKey = to.getIndex() + Constant.SPLIT_SLASH + po.getTxId();
            txPut(tx, newKey, po);
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

    public CoreTxStatusEnum queryByTxIdAndStatus(String txId, String index) {
        if (!StringUtils.isEmpty(index)) {
            String key = index + Constant.SPLIT_SLASH + txId;
            if (keyMayExist(key) && null != get(key)) {
                return CoreTxStatusEnum.formIndex(index);
            }
        }

        List<String> indexList = CoreTxStatusEnum.getIndexList(null);
        for (String i : indexList) {
            String key = i + Constant.SPLIT_SLASH + txId;
            if (keyMayExist(key) && null != get(key)) {
                return CoreTxStatusEnum.formIndex(i);
            }
        }
        return null;
    }

    public void failoverBatchDelete(List<CoreTransactionPO> coreTransactionPOList) {
        if (CollectionUtils.isEmpty(coreTransactionPOList)) {
            return;
        }

        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[CoreTxProcessRocksDao.failoverBatchDelete] transaction is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_TRANSACTION_IS_NULL);
        }

        for (CoreTransactionPO po : coreTransactionPOList) {
            String key = CoreTxStatusEnum.WAIT.getIndex() + Constant.SPLIT_SLASH + po.getTxId();
            txDelete(tx, key);
        }
    }
}
