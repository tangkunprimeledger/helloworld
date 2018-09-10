package com.higgs.trust.rs.core.repository;

import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.dao.CoreTransactionProcessDao;
import com.higgs.trust.rs.core.dao.CoreTxProcessJDBCDao;
import com.higgs.trust.rs.core.dao.po.CoreTransactionProcessPO;
import com.higgs.trust.rs.core.dao.rocks.CoreTxProcessRocksDao;
import com.higgs.trust.rs.core.vo.RsCoreTxVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lingchao
 * @description
 * @date 2018-08-21
 */
@Slf4j
@Repository
public class CoreTxProcessRepository {
    @Autowired
    private RsConfig rsConfig;
    @Autowired
    private CoreTransactionProcessDao coreTransactionProcessDao;
    @Autowired
    private CoreTxProcessRocksDao coreTxProcessRocksDao;
    @Autowired
    private CoreTxProcessJDBCDao coreTxProcessJDBCDao;

    /**
     * create new core_transaction_process
     *
     * @param txId
     * @param statusEnum
     */
    public void add(String txId, CoreTxStatusEnum statusEnum) {
        try {
            Profiler.enter("[rs.core.addCoreTxProcess]");
            CoreTransactionProcessPO po = new CoreTransactionProcessPO();
            po.setTxId(txId);
            po.setStatus(statusEnum.getCode());
            if (rsConfig.isUseMySQL()) {
                try {
                    coreTransactionProcessDao.add(po);
                } catch (DuplicateKeyException e) {
                    log.error("[add.core_transaction_process]has idempotent error");
                    throw new RsCoreException(RsCoreErrorEnum.RS_CORE_IDEMPOTENT);
                }
            } else {
                coreTxProcessRocksDao.saveWithTransaction(po, statusEnum.getIndex());
            }
        } finally {
            Profiler.release();
        }
    }

    /**
     * query by transaction id
     *
     * @param txId
     * @param statusEnum
     * @return
     */
    public CoreTransactionProcessPO queryByTxId(String txId, CoreTxStatusEnum statusEnum) {
        if (rsConfig.isUseMySQL()) {
            return coreTransactionProcessDao.queryByTxId(txId, statusEnum.getCode());
        }
        //TODO for update
        return coreTxProcessRocksDao.queryByTxIdAndStatus(txId, statusEnum == null ? null : statusEnum.getIndex());
    }

    /**
     * query for status by page no
     *
     * @param coreTxStatusEnum
     * @param row
     * @param count
     * @return
     */
    public List<CoreTransactionProcessPO> queryByStatus(CoreTxStatusEnum coreTxStatusEnum, int row, int count) {
        if (rsConfig.isUseMySQL()) {
            return coreTransactionProcessDao.queryByStatus(coreTxStatusEnum.getCode(), row, count);
        }
        return coreTxProcessRocksDao.queryByPrefix(coreTxStatusEnum.getIndex(), count);
    }

    /**
     * update status by from to
     *
     * @param txId
     * @param from
     * @param to
     */
    public void updateStatus(String txId, CoreTxStatusEnum from, CoreTxStatusEnum to) {
        try {
            Profiler.enter("[rs.core.updateStatus]");
            if (rsConfig.isUseMySQL()) {
                int r = coreTransactionProcessDao.updateStatus(txId, from.getCode(), to.getCode());
                if (r != 1) {
                    log.error("[updateStatus]from {} to {} is fail txId:{}", from, to, txId);
                    throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_UPDATE_STATUS_FAILED);
                }
            } else {
                coreTxProcessRocksDao.updateStatus(txId, from, to);
            }
        } finally {
            Profiler.release();
        }
    }

    /**
     * create new core_transaction_process for batch
     *
     * @param txs
     */
    public void batchInsert(List<RsCoreTxVO> txs, CoreTxStatusEnum statusEnum) {
        try {
            Profiler.enter("[rs.core.batchInsert.coreTxProcess]");
            List<CoreTransactionProcessPO> coreTransactionProcessPOList = convert(txs, statusEnum);

            if (rsConfig.isUseMySQL()) {
                try {
                    coreTxProcessJDBCDao.batchInsert(coreTransactionProcessPOList);
                } catch (DuplicateKeyException e) {
                    log.error("[batchInsert.core_transaction_process]has idempotent error");
                    throw new RsCoreException(RsCoreErrorEnum.RS_CORE_IDEMPOTENT);
                }
            } else {
                coreTxProcessRocksDao.batchInsert(coreTransactionProcessPOList, statusEnum.getIndex());
            }
        } finally {
            Profiler.release();
        }
    }

    /**
     * update status by from to  for batch
     *
     * @param txs
     * @param from
     * @param to
     */
    public void batchUpdateStatus(List<RsCoreTxVO> txs, CoreTxStatusEnum from, CoreTxStatusEnum to) {
        try {
            Profiler.enter("[rs.core.batchUpdateStatus]");
            if (rsConfig.isUseMySQL()) {
                int r = coreTxProcessJDBCDao.batchUpdate(convert(txs, from), from, to);
                if (r != txs.size()) {
                    log.error("[batchUpdateStatus.coreTx]from {} to {} is fail,exe.num:{},txs:{}", from, to, r, txs);
                    throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_UPDATE_STATUS_FAILED);
                }
            } else {
                coreTxProcessRocksDao.batchUpdate(convert(txs, from), from, to);
            }
        } finally {
            Profiler.release();
        }
    }

    /**
     * convert bean
     *
     * @param txs
     * @return
     */
    private List<CoreTransactionProcessPO> convert(List<RsCoreTxVO> txs, CoreTxStatusEnum statusEnum) {
        List<CoreTransactionProcessPO> coreTransactionProcessPOList = new ArrayList<>(txs.size());
        for (RsCoreTxVO vo : txs) {
            CoreTransactionProcessPO po = new CoreTransactionProcessPO();
            po.setTxId(vo.getTxId());
            po.setStatus(statusEnum.getCode());
            coreTransactionProcessPOList.add(po);
        }
        return coreTransactionProcessPOList;
    }

    /**
     * delete coreTxProcess for status with END
     */
    public void deleteEnd() {
        if (rsConfig.isUseMySQL()) {
            coreTransactionProcessDao.deleteEnd();
        } else {
            coreTxProcessRocksDao.deleteEND(CoreTxStatusEnum.END.getIndex());
        }
    }
}
