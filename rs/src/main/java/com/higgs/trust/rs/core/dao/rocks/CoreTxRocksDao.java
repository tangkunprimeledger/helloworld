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
import org.rocksdb.WriteBatch;
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
     * @param po
     */
    public void save(CoreTransactionPO po) {
        String key = po.getTxId();
        if (keyMayExist(key)) {
            log.error("[CoreTxRocksDao.save] core transaction is already exist, txId={}", key);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_ALREADY_EXIST);
        }
        po.setCreateTime(new Date());

        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            put(key, po);
        } else {
            batchPut(batch, key, po);
        }
    }

    /**
     * db transaction
     * @param po
     */
    public void saveWithTransaction(CoreTransactionPO po) {
        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[CoreTxRocksDao.saveWithTransaction] write batch is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        String key = po.getTxId();
        if (keyMayExist(key)) {
            log.error("[CoreTxRocksDao.saveWithTransaction] core transaction is already exist, key={}", key);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_ALREADY_EXIST);
        }
        po.setCreateTime(new Date());
        batchPut(batch, key, po);
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
        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[CoreTxRocksDao.updateSignDatas] write batch is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        CoreTransactionPO po = get(txId);
        if (null == po) {
            log.error("[CoreTxRocksDao.updateSignDatas] core transaction is not exist, txId={}", txId);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_KEY_IS_NOT_EXIST);
        }
        po.setUpdateTime(new Date());
        po.setSignDatas(signJSON);
        batchPut(batch, txId, po);
    }

    public void saveExecuteResultAndHeight(String txId, String result, String respCode, String respMsg, Long blockHeight) {
        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[CoreTxRocksDao.saveExecuteResultAndHeight] write batch is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_WRITE_BATCH_IS_NULL);
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
        batchPut(batch, txId, po);
    }

    public void batchInsert(List<CoreTransactionPO> coreTransactionPOList) {
        if (CollectionUtils.isEmpty(coreTransactionPOList)) {
            return;
        }

        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[CoreTxRocksDao.batchInsert] write batch is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_WRITE_BATCH_IS_NULL);
        }

        for (CoreTransactionPO po : coreTransactionPOList) {
            po.setCreateTime(new Date());
            batchPut(batch, po.getTxId(), po);
        }
    }

    public void batchUpdate(List<RsCoreTxVO> txs, Long blockHeight) {
        if (CollectionUtils.isEmpty(txs)) {
            return;
        }

        WriteBatch batch = ThreadLocalUtils.getWriteBatch();
        if (null == batch) {
            log.error("[CoreTxRocksDao.batchUpdate] write batch is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_ROCKS_WRITE_BATCH_IS_NULL);
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
            batchPut(batch, po.getTxId(), po);
        }
    }
}
