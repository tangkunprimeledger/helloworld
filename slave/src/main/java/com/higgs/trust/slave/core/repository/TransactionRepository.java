package com.higgs.trust.slave.core.repository;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.slave.api.vo.CoreTransactionVO;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.transaction.TransactionPO;
import com.higgs.trust.slave.dao.transaction.TransactionDao;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.TransactionReceipt;
import com.higgs.trust.slave.model.bo.action.Action;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author tangfashuang
 * @desc pending transaction repository
 * @date 2018/4/9
 */

@Repository @Slf4j public class TransactionRepository {
    @Autowired TransactionDao transactionDao;

    public boolean isExist(String txId) {
        TransactionPO transactionPO = transactionDao.queryByTxId(txId);
        if (null != transactionPO) {
            return true;
        }
        return false;
    }

    /**
     * query more execute receipt for txs
     * @param txs
     * @return
     */
    public List<TransactionReceipt> queryTxReceipts(List<SignedTransaction> txs) {
        if (CollectionUtils.isEmpty(txs)) {
            return null;
        }
        List<String> txIds = new ArrayList<>();
        for (SignedTransaction signedTransaction : txs) {
            txIds.add(signedTransaction.getCoreTx().getTxId());
        }
        List<TransactionPO> transactionPOS = transactionDao.queryByTxIds(txIds);
        if (CollectionUtils.isEmpty(transactionPOS)) {
            return null;
        }
        List<TransactionReceipt> receipts = new ArrayList<>(transactionPOS.size());
        for (TransactionPO transactionPO : transactionPOS) {
            TransactionReceipt receipt = new TransactionReceipt();
            receipt.setResult(StringUtils.equals(transactionPO.getExecuteResult(),"1")?true:false);
            receipt.setErrorCode(transactionPO.getErrorCode());
            receipts.add(receipt);
        }
        return receipts;
    }
    /**
     * query transactions by block height
     *
     * @param blockHeight
     * @return
     */
    public List<SignedTransaction> queryTransactions(Long blockHeight) {
        List<TransactionPO> txPOs = transactionDao.queryByBlockHeight(blockHeight);
        if (CollectionUtils.isEmpty(txPOs)) {
            return null;
        }
        List<SignedTransaction> txs = new ArrayList<>();
        for (TransactionPO tx : txPOs) {
            SignedTransaction signedTransaction = new SignedTransaction();
            CoreTransaction coreTx = BeanConvertor.convertBean(tx, CoreTransaction.class);
            if (tx.getBizModel() != null) {
                coreTx.setBizModel(JSON.parseObject(String.valueOf(tx.getBizModel())));
            }
            String actionDatas = tx.getActionDatas();
            List<Action> actions = JSON.parseArray(actionDatas, Action.class);
            coreTx.setActionList(actions);
            signedTransaction.setCoreTx(coreTx);
            List<String> signDatas = JSON.parseArray(tx.getSignDatas(), String.class);
            signedTransaction.setSignatureList(signDatas);
            txs.add(signedTransaction);
        }
        return txs;
    }

    /**
     * batch process transaction
     *
     * @param blockHeight
     * @param txs
     * @param txReceipts
     */
    public void batchSaveTransaction(Long blockHeight, Date blockTime, List<SignedTransaction> txs,
        List<TransactionReceipt> txReceipts) {
        log.info("[TransactionRepository.batchSaveTransaction] is start");
        if (CollectionUtils.isEmpty(txs)) {
            log.info("[batchSaveTransaction] txs is empty");
            return;
        }
        List<TransactionPO> txPOs = new ArrayList<>();
        for (SignedTransaction tx : txs) {
            CoreTransaction coreTx = tx.getCoreTx();
            TransactionPO po = BeanConvertor.convertBean(coreTx, TransactionPO.class);
            if (coreTx.getBizModel() != null) {
                po.setBizModel(coreTx.getBizModel().toJSONString());
            }
            po.setBlockHeight(blockHeight);
            po.setBlockTime(blockTime);
            po.setSignDatas(JSON.toJSONString(tx.getSignatureList()));
            po.setActionDatas(JSON.toJSONString(coreTx.getActionList()));
            TransactionReceipt receipt = getTxReceipt(txReceipts, coreTx.getTxId());
            if (receipt != null) {
                po.setExecuteResult(receipt.isResult() ? "1" : "0");
                po.setErrorCode(receipt.getErrorCode());
            }
            txPOs.add(po);
        }
        try {
            int r = transactionDao.batchInsert(txPOs);
            if (r != txPOs.size()) {
                log.error("[batchSaveTransaction]batch insert txs has error");
                throw new SlaveException(SlaveErrorEnum.SLAVE_UNKNOWN_EXCEPTION);
            }
        } catch (DuplicateKeyException e) {
            log.error("[batchSaveTransaction] is idempotent blockHeight:{}", blockHeight);
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        }
        log.info("[TransactionRepository.batchSaveTransaction] is end");
    }

    /**
     * get tx receipt by txId
     *
     * @param txReceipts
     * @param txId
     * @return
     */
    private TransactionReceipt getTxReceipt(List<TransactionReceipt> txReceipts, String txId) {
        if (CollectionUtils.isEmpty(txReceipts)) {
            return null;
        }
        for (TransactionReceipt txReceipt : txReceipts) {
            if (StringUtils.equals(txReceipt.getTxId(), txId)) {
                return txReceipt;
            }
        }
        return null;
    }

    /**
     * return txIds from db
     *
     * @param txs
     * @return
     */
    public List<String> queryTxIds(List<SignedTransaction> txs) {
        List<String> datas = new ArrayList<>();
        if (CollectionUtils.isEmpty(txs)) {
            return datas;
        }
        List<String> txIds = new ArrayList<>();
        for (SignedTransaction signedTransaction : txs) {
            txIds.add(signedTransaction.getCoreTx().getTxId());
        }
        List<TransactionPO> transactionPOS = transactionDao.queryByTxIds(txIds);
        if (CollectionUtils.isEmpty(transactionPOS)) {
            return datas;
        }
        for (TransactionPO transactionPO : transactionPOS) {
            datas.add(transactionPO.getTxId());
        }
        return datas;
    }

    public List<CoreTransactionVO> queryTxsWithCondition(Long blockHeight, String txId,
        String sender, Integer pageNum, Integer pageSize) {
        if (null != txId) {
            txId = txId.trim();
        }

        if (null != sender) {
            sender = sender.trim();
        }

        if (null == pageNum || pageNum < 1) {
            pageNum = 1;
        }

        if (null == pageSize || pageSize < 1) {
            pageSize = 200;
        }

        List<TransactionPO> list = transactionDao.queryTxWithCondition(blockHeight, txId, sender, (pageNum - 1) * pageSize, pageSize);
        return BeanConvertor.convertList(list, CoreTransactionVO.class);
    }
}
