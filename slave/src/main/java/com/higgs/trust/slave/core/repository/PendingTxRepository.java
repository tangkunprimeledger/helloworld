package com.higgs.trust.slave.core.repository;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.enums.MonitorTargetEnum;
import com.higgs.trust.common.utils.MonitorLogUtils;
import com.higgs.trust.slave.common.config.InitConfig;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.mysql.pack.PendingTransactionDao;
import com.higgs.trust.slave.dao.po.pack.PendingTransactionPO;
import com.higgs.trust.slave.dao.rocks.pack.PendingTxRocksDao;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.enums.biz.PendingTxStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author tangfashuang
 * @desc pending transaction repository
 * @date 2018/4/9
 */
@Repository @Slf4j public class PendingTxRepository {

    @Autowired private PendingTransactionDao pendingTransactionDao;

    @Autowired private PendingTxRocksDao pendingTxRocksDao;

    @Autowired private InitConfig initConfig;

    /**
     * check if exist in pending_transaction table
     *
     * @param txId
     * @return
     */
    public boolean isExist(String txId) {
        boolean flag = true;
        if (initConfig.isUseMySQL()) {
            if (null == pendingTransactionDao.queryByTxId(txId)) {
                flag = false;
            }
        } else {
            if (null == pendingTxRocksDao.get(txId)) {
                flag = false;
            }
        }
        return flag;
    }

    public void batchInsertToRocks(List<SignedTransaction> signedTransactions, Long packHeight) {
        if (CollectionUtils.isEmpty(signedTransactions)) {
            log.info("signed transaction list is empty");
            return;
        }
        List<String> txIds = new ArrayList<>();
        for (SignedTransaction signedTx : signedTransactions) {
            txIds.add(signedTx.getCoreTx().getTxId());
        }
        pendingTxRocksDao.batchInsert(txIds, packHeight);
    }

    public void batchInsert(List<SignedTransaction> signedTransactions, PendingTxStatusEnum status, Long packHeight) {
        if (CollectionUtils.isEmpty(signedTransactions)) {
            log.info("signed transaction list is empty");
            return;
        }

        List<PendingTransactionPO> list = new ArrayList<>();
        for (SignedTransaction signedTx : signedTransactions) {
            PendingTransactionPO pendingTransactionPO = convertSignedTxToPendingTxPO(signedTx);
            pendingTransactionPO.setStatus(status.getCode());
            pendingTransactionPO.setHeight(packHeight);
            list.add(pendingTransactionPO);
        }

        try {
            int r = pendingTransactionDao.batchInsert(list);
            if (r != signedTransactions.size()) {
                log.error("[batchSavePendingTransaction]batch insert pending transaction has error");
                MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_BATCH_INSERT_PENDING_TX_ERROR.getMonitorTarget(), 1);
                throw new SlaveException(SlaveErrorEnum.SLAVE_UNKNOWN_EXCEPTION);
            }
        } catch (DuplicateKeyException e) {
            log.error("[batchSavePendingTransaction] is idempotent packHeight:{}", packHeight);
            MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_PENDING_TRANSACTION_IDEMPOTENT_EXCEPTION.getMonitorTarget(), 1);
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        }
    }

    /**
     * get pending transaction list by block height
     *
     * @param height
     * @return
     */
    public List<SignedTransaction> getTransactionsByHeight(Long height) {
        List<SignedTransaction> signedTransactions = new ArrayList<>();
        List<PendingTransactionPO> transactionPOList = pendingTransactionDao.queryByHeight(height);
        if (CollectionUtils.isEmpty(transactionPOList)) {
            log.error("packaged transaction list is empty. height={}", height);
            return signedTransactions;
        }

        transactionPOList.forEach(pendingTransactionPO -> {
            if (StringUtils.equals(PendingTxStatusEnum.INIT.getCode(), pendingTransactionPO.getStatus())) {
                log.error("Pending transaction which status equals 'INIT' can not have height value, txId={}",
                    pendingTransactionPO.getTxId());
                MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_PENDING_TX_STATUS_EXCEPTION.getMonitorTarget(), 1);
                throw new SlaveException(SlaveErrorEnum.SLAVE_UNKNOWN_EXCEPTION);
            }
            SignedTransaction signedTransaction = convertPendingTxPOToSignedTx(pendingTransactionPO);
            if (null != signedTransaction) {
                signedTransactions.add(signedTransaction);
            }
        });

        // sort signedTransactions by txId asc
        Collections.sort(signedTransactions, new Comparator<SignedTransaction>() {
            @Override public int compare(SignedTransaction signedTx1, SignedTransaction signedTx2) {
                return signedTx1.getCoreTx().getTxId().compareTo(signedTx2.getCoreTx().getTxId());
            }
        });

        return signedTransactions;
    }

    private PendingTransactionPO convertSignedTxToPendingTxPO(SignedTransaction signedTransaction) {
        PendingTransactionPO pendingTransactionPO = new PendingTransactionPO();
        pendingTransactionPO.setTxId(signedTransaction.getCoreTx().getTxId());
        pendingTransactionPO.setTxData(JSON.toJSONString(signedTransaction));
        return pendingTransactionPO;
    }

    private SignedTransaction convertPendingTxPOToSignedTx(PendingTransactionPO pendingTransactionPO) {
        SignedTransaction signedTransaction;
        try {
            signedTransaction = JSON.parseObject(pendingTransactionPO.getTxData(), SignedTransaction.class);
        } catch (Throwable e) {
            log.error("signedTransaction json parse exception. ", e);
            MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_PENDING_TX_TO_SIGNED_TX_EXCEPTION.getMonitorTarget(), 1);
            return null;
        }
        return signedTransaction;
    }

    public List<String> queryTxIds(List<String> txIds) {

        if (CollectionUtils.isEmpty(txIds)) {
            return null;
        }

        List<String> pTxIds = new ArrayList<>();
        if (initConfig.isUseMySQL()) {
            List<PendingTransactionPO> pTxPOs = pendingTransactionDao.queryByTxIds(txIds);
            if (CollectionUtils.isEmpty(pTxPOs)) {
                return null;
            }
            for (PendingTransactionPO po : pTxPOs) {
                pTxIds.add(po.getTxId());
            }
        } else {
            List<String> resultList = pendingTxRocksDao.getTxIds(txIds);
            if (CollectionUtils.isEmpty(resultList)) {
                return null;
            }
            pTxIds.addAll(resultList);
        }
        return pTxIds;
    }

    /**
     * delete by less than height
     *
     * @param height
     * @return
     */
    public int deleteLessThanHeight(Long height) {
        if (initConfig.isUseMySQL()) {
            return pendingTransactionDao.deleteLessThanHeight(height);
        }
        //rocks db delete with package
        return 1;
    }

    /**
     * when delete package invoke
     * @param signedTxList
     */
    public void batchDelete(List<SignedTransaction> signedTxList) {
        if (CollectionUtils.isEmpty(signedTxList)) {
            return;
        }
        for (SignedTransaction signedTx : signedTxList) {
            pendingTxRocksDao.batchDelete(signedTx.getCoreTx().getTxId());
        }
    }
}
