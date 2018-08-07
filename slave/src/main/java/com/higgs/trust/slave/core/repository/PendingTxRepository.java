package com.higgs.trust.slave.core.repository;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.pack.PendingTransactionDao;
import com.higgs.trust.slave.dao.po.pack.PendingTransactionPO;
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

    /**
     * check if exist in pending_transaction table
     *
     * @param txId
     * @return
     */
    public boolean isExist(String txId) {
        PendingTransactionPO pendingTransactionPO = pendingTransactionDao.queryByTxId(txId);
        if (null != pendingTransactionPO) {
            return true;
        }
        return false;
    }

    /**
     * batch update pending_transaction status
     *
     * @param signedTransactions
     * @param fromStatus
     * @param toStatus
     * @param height
     * @return
     */
    public int batchUpdateStatus(List<SignedTransaction> signedTransactions, PendingTxStatusEnum fromStatus,
        PendingTxStatusEnum toStatus, Long height) {
        int count = 0;
        if (CollectionUtils.isEmpty(signedTransactions)) {
            log.info("signed transaction list is empty");
            return count;
        }

        for (SignedTransaction signedTx : signedTransactions) {
            int update = pendingTransactionDao
                .updateStatus(signedTx.getCoreTx().getTxId(), fromStatus.getCode(), toStatus.getCode(),
                    height);
            count = count + update;
        }
        return count;
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
                throw new SlaveException(SlaveErrorEnum.SLAVE_UNKNOWN_EXCEPTION);
            }
        } catch (DuplicateKeyException e) {
            log.error("[batchSavePendingTransaction] is idempotent packHeight:{}", packHeight);
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        }
    }

    /**
     * save pending_transaction
     *
     * @param signedTransaction
     */
    public void saveWithStatus(SignedTransaction signedTransaction, PendingTxStatusEnum status, Long height) {
        if (null == signedTransaction) {
            log.error("signedTransaction is null");
            return;
        }

        PendingTransactionPO pendingTransactionPO = convertSignedTxToPendingTxPO(signedTransaction);
        pendingTransactionPO.setStatus(status.getCode());

        // if status equals 'PACKAGED', height cannot be null
        if (StringUtils.equals(PendingTxStatusEnum.PACKAGED.getCode(), status.getCode())) {
            if (null == height) {
                log.error("height cannot be null while status equals 'PACKAGED'");
                return;
            }
            pendingTransactionPO.setHeight(height);
        }

        pendingTransactionDao.add(pendingTransactionPO);
    }

    /**
     * get pending transaction list by transaction status
     *
     * @param status
     * @param count
     * @return
     */
    public List<SignedTransaction> getTransactionsByStatus(String status, int count) {
        List<SignedTransaction> signedTransactions = new ArrayList<>();
        List<PendingTransactionPO> transactionPOList = pendingTransactionDao.queryByStatus(status, count);
        if (CollectionUtils.isEmpty(transactionPOList)) {
            return signedTransactions;
        }

        transactionPOList.forEach(pendingTransactionPO -> {
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
                //TODO 添加告警
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
            return null;
        }
        return signedTransaction;
    }

    public List<String> queryTxIds(List<String> txIds) {

        if (CollectionUtils.isEmpty(txIds)) {
            return null;
        }

        List<PendingTransactionPO> pTxPOs = pendingTransactionDao.queryByTxIds(txIds);
        if (CollectionUtils.isEmpty(pTxPOs)) {
            return null;
        }

        List<String> pTxIds = new ArrayList<>();
        for (PendingTransactionPO po : pTxPOs) {
            pTxIds.add(po.getTxId());
        }
        return pTxIds;
    }
}
