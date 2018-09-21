package com.higgs.trust.rs.core.dao.rocks;

import com.higgs.trust.common.dao.RocksBaseDao;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import com.higgs.trust.rs.core.vo.RsCoreTxVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.rocksdb.Transaction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author tangfashuang
 */
@Service
@Slf4j
public class CoreTxRocksDao extends RocksBaseDao<CoreTransactionPO>{
    @Override protected String getColumnFamilyName() {
        return "coreTransaction";
    }

    /**
     * with transaction or no transaction
     *
     * @param po
     */
    public void save(CoreTransactionPO po) {
        String key = po.getTxId();
        if (keyMayExist(key) && null != get(key)) {
            log.error("[CoreTxRocksDao.save] core transaction is already exist, txId={}", key);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_ALREADY_EXIST);
        }
        po.setCreateTime(new Date());

        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            put(key, po);
        } else {
            txPut(tx, key, po);
        }
    }

    /**
     * db transaction
     * @param po
     */
    public void saveWithTransaction(CoreTransactionPO po) {
        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[CoreTxRocksDao.saveWithTransaction] transaction is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_TRANSACTION_IS_NULL);
        }

        String key = po.getTxId();
        if (keyMayExist(key) && null != get(key)) {
            log.error("[CoreTxRocksDao.saveWithTransaction] core transaction is already exist, key={}", key);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_ALREADY_EXIST);
        }
        po.setCreateTime(new Date());
        txPut(tx, key, po);
    }

    public List<CoreTransactionPO> queryByTxIds(List<String> txIdList) {
        if (CollectionUtils.isEmpty(txIdList)) {
            return null;
        }

        Map<String, CoreTransactionPO> resultMap = multiGet(txIdList);
        if (MapUtils.isEmpty(resultMap)) {
            return null;
        }

        List<CoreTransactionPO> pos = new ArrayList<>(resultMap.size());
        for (String key : resultMap.keySet()) {
            pos.add(resultMap.get(key));
        }
        return pos;
    }

    public void updateSignDatas(String txId, String signJSON) {
        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[CoreTxRocksDao.updateSignDatas] transaction is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_TRANSACTION_IS_NULL);
        }

        CoreTransactionPO po = get(txId);
        if (null == po) {
            log.error("[CoreTxRocksDao.updateSignDatas] core transaction is not exist, txId={}", txId);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_IS_NOT_EXIST);
        }
        po.setUpdateTime(new Date());
        po.setSignDatas(signJSON);
        txPut(tx, txId, po);
    }

    public void saveExecuteResultAndHeight(String txId, String result, String respCode, String respMsg,
        Long blockHeight) {
        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[CoreTxRocksDao.saveExecuteResultAndHeight] transaction is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_TRANSACTION_IS_NULL);
        }

        CoreTransactionPO po = get(txId);
        if (null == po) {
            log.error("[CoreTxRocksDao.saveExecuteResultAndHeight] core transaction is not exist, txId={}", txId);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_IS_NOT_EXIST);
        }
        po.setUpdateTime(new Date());
        po.setExecuteResult(result);
        po.setErrorCode(respCode);
        po.setErrorMsg(respMsg);
        po.setBlockHeight(blockHeight);
        txPut(tx, txId, po);
    }

    public void updateWithTransaction(CoreTransactionPO po) {
        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[CoreTxRocksDao.updateWithTransaction] transaction is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_TRANSACTION_IS_NULL);
        }
        txPut(tx, po.getTxId(), po);
    }

    public void batchInsert(List<CoreTransactionPO> coreTransactionPOList) {
        if (CollectionUtils.isEmpty(coreTransactionPOList)) {
            return;
        }

        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[CoreTxRocksDao.batchInsert] transaction is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_TRANSACTION_IS_NULL);
        }

        for (CoreTransactionPO po : coreTransactionPOList) {
            po.setCreateTime(new Date());
            txPut(tx, po.getTxId(), po);
        }
    }

    public void batchUpdate(List<RsCoreTxVO> txs, Long blockHeight) {
        if (CollectionUtils.isEmpty(txs)) {
            return;
        }

        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[CoreTxRocksDao.batchUpdate] transaction is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_TRANSACTION_IS_NULL);
        }

        for (RsCoreTxVO vo : txs) {
            CoreTransactionPO po = get(vo.getTxId());
            if (null == po) {
                log.error("[CoreTxRocksDao.batchUpdate] core transaction is not exist");
                throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_IS_NOT_EXIST);
            }
            po.setErrorMsg(vo.getErrorMsg());
            po.setErrorCode(vo.getErrorCode());
            po.setBlockHeight(blockHeight);
            po.setExecuteResult(vo.getExecuteResult().getCode());
            po.setUpdateTime(new Date());
            txPut(tx, po.getTxId(), po);
        }
    }

    public void failoverBatchInsert(List<CoreTransactionPO> coreTransactionPOList) {
        if (CollectionUtils.isEmpty(coreTransactionPOList)) {
            return;
        }

        Transaction tx = ThreadLocalUtils.getRocksTx();
        if (null == tx) {
            log.error("[CoreTxRocksDao.failoverBatchInsert] transaction is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_TRANSACTION_IS_NULL);
        }

        for (CoreTransactionPO po : coreTransactionPOList) {
            String txId = po.getTxId();
            CoreTransactionPO oldPo = get(txId);
            if (null == oldPo) {
                txPut(tx, txId, po);
            } else {
                oldPo.setUpdateTime(new Date());
                oldPo.setBlockHeight(po.getBlockHeight());
                oldPo.setErrorCode(po.getErrorCode());
                oldPo.setErrorMsg(po.getErrorMsg());
                oldPo.setExecuteResult(po.getExecuteResult());
                txPut(tx, txId, oldPo);
            }
        }
    }
}
