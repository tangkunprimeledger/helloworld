package com.higgs.trust.slave.core.service.pending;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.higgs.trust.slave.api.vo.TransactionVO;
import com.higgs.trust.slave.common.constant.Constant;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidateResult;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.repository.PendingTxRepository;
import com.higgs.trust.slave.core.repository.TransactionRepository;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.enums.biz.PendingTxStatusEnum;
import com.higgs.trust.slave.model.enums.biz.TxSubmitResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @Description:
 * @author: pengdi
 **/
@Service @Slf4j public class PendingStateImpl implements PendingState {
    @Autowired private PendingTxRepository pendingTxRepository;

    @Autowired private TransactionRepository transactionRepository;

    @Autowired private Deque<SignedTransaction> pendingTxQueue;

    @Autowired private ConcurrentLinkedHashMap existTxMap;

    /**
     * add pending transaction to db
     * @param transactions
     * @return transaction list which check failed
     */
    @Override public List<TransactionVO> addPendingTransactions(List<SignedTransaction> transactions) {

        if (CollectionUtils.isEmpty(transactions)) {
            log.error("received transaction list is empty");
            return null;
        }

        List<TransactionVO> transactionVOList = new ArrayList<>();
        transactions.forEach(signedTransaction -> {
            TransactionVO transactionVO = new TransactionVO();
            String txId = signedTransaction.getCoreTx().getTxId();
            transactionVO.setTxId(txId);

            // params check
            BeanValidateResult validateResult = BeanValidator.validate(signedTransaction);
            if (!validateResult.isSuccess()) {
                log.error("transaction invalid. errMsg={}, txId={}", validateResult.getFirstMsg(), txId);
                transactionVO.setErrCode(TxSubmitResultEnum.PARAM_INVALID.getCode());
                transactionVO.setErrMsg(TxSubmitResultEnum.PARAM_INVALID.getDesc());
                transactionVO.setRetry(false);
                transactionVOList.add(transactionVO);
                return;
            }

            // chained transaction idempotent check, cannot retry
            //TODO rocks db isExist method
            if (transactionRepository.isExist(txId)) {
                log.warn("transaction idempotent, txId={}", txId);
                transactionVO.setErrCode(TxSubmitResultEnum.TX_IDEMPOTENT.getCode());
                transactionVO.setErrMsg(TxSubmitResultEnum.TX_IDEMPOTENT.getDesc());
                transactionVO.setRetry(false);
                transactionVOList.add(transactionVO);
                return;
            }

            //limit queue size
            if (pendingTxQueue.size() > Constant.MAX_PENDING_TX_QUEUE_SIZE) {
                log.warn("pending transaction queue size is too large , txId={}", txId);
                transactionVO.setErrCode(TxSubmitResultEnum.TX_QUEUE_SIZE_TOO_LARGE.getCode());
                transactionVO.setErrMsg(TxSubmitResultEnum.TX_QUEUE_SIZE_TOO_LARGE.getDesc());
                transactionVO.setRetry(true);
                transactionVOList.add(transactionVO);
                return;
            }

            //check exist tx map and pending_tx_index
            //TODO rocks db isExist method
            if (existTxMap.containsKey(txId) || pendingTxRepository.isExist(txId)) {
                log.warn("pending transaction idempotent, txId={}", txId);
                transactionVO.setErrCode(TxSubmitResultEnum.PENDING_TX_IDEMPOTENT.getCode());
                transactionVO.setErrMsg(TxSubmitResultEnum.PENDING_TX_IDEMPOTENT.getDesc());
                transactionVO.setRetry(true);
                transactionVOList.add(transactionVO);
                return;
            }

            //insert memory
            pendingTxQueue.offerLast(signedTransaction);
            // key and value all are txId
            existTxMap.put(signedTransaction.getCoreTx().getTxId(), signedTransaction.getCoreTx().getTxId());
        });

        // if all transaction received success, RespData will set data 'null'
        if (CollectionUtils.isEmpty(transactionVOList)) {
            return null;
        }
        return transactionVOList;
    }

    @Override public List<SignedTransaction> getPendingTransactions(int count) {
        if (null == pendingTxQueue.peekFirst()) {
            return null;
        }

        int num = 0;
        List<SignedTransaction> list = new ArrayList<>();
        while (num < count) {
            SignedTransaction signedTx = pendingTxQueue.pollFirst();
            if (null != signedTx) {
                list.add(signedTx);
                num++;
            } else {
                break;
            }
        }
        return list;
    }

    @Override public int packagePendingTransactions(List<SignedTransaction> signedTransactions, Long height) {
        return pendingTxRepository.batchUpdateStatus(signedTransactions, PendingTxStatusEnum.INIT, PendingTxStatusEnum.PACKAGED, height);
    }

    @Override public List<SignedTransaction> getPackagedTransactions(Long height) {
        return pendingTxRepository.getTransactionsByHeight(height);
    }

    @Override public void addPendingTxsToQueueFirst(List<SignedTransaction> signedTransactions) {
        signedTransactions.forEach(signedTx->{
            try {
                pendingTxQueue.offerFirst(signedTx);
            } catch (Exception e) {
                log.error("add transaction to pendingTxQueue exception. ", e);
            }
        });
        System.out.println("pendingTxQueue.size = " + pendingTxQueue.size());
    }
}
