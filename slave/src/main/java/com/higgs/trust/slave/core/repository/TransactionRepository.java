package com.higgs.trust.slave.core.repository;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.slave.api.vo.CoreTransactionVO;
import com.higgs.trust.slave.common.config.InitConfig;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.mysql.transaction.TransactionDao;
import com.higgs.trust.slave.dao.mysql.transaction.TransactionJDBCDao;
import com.higgs.trust.slave.dao.po.block.BlockPO;
import com.higgs.trust.slave.dao.po.transaction.TransactionPO;
import com.higgs.trust.slave.dao.po.transaction.TransactionReceiptPO;
import com.higgs.trust.slave.dao.rocks.block.BlockRocksDao;
import com.higgs.trust.slave.dao.rocks.transaction.TransactionRocksDao;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignInfo;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.TransactionReceipt;
import com.higgs.trust.slave.model.bo.action.Action;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * @author tangfashuang
 * @desc pending transaction repository
 * @date 2018/4/9
 */

@Repository @Slf4j public class TransactionRepository {
    @Autowired TransactionDao transactionDao;
    @Autowired TransactionJDBCDao transactionJDBCDao;
    @Autowired TransactionRocksDao transactionRocksDao;
    @Autowired BlockRocksDao blockRocksDao;
    @Autowired InitConfig initConfig;

    public boolean isExist(String txId) {
        if (initConfig.isUseMySQL()) {
            if (null != transactionDao.queryByTxId(txId)) {
                return true;
            }
        } else {
            if (null != transactionRocksDao.get(txId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * query more execute receipt for txs
     *
     * @param txs
     * @return
     */
    public Map<String, TransactionReceipt> queryTxReceiptMap(List<SignedTransaction> txs) {
        if (CollectionUtils.isEmpty(txs)) {
            return null;
        }
        List<String> txIds = new ArrayList<>();
        for (SignedTransaction signedTransaction : txs) {
            txIds.add(signedTransaction.getCoreTx().getTxId());
        }

        Map<String, TransactionReceipt> receiptMap;
        if (initConfig.isUseMySQL()) {
            List<TransactionPO> transactionPOS = transactionDao.queryByTxIds(txIds);
            if (CollectionUtils.isEmpty(transactionPOS)) {
                return null;
            }
            receiptMap = new HashMap<>(transactionPOS.size());
            for (TransactionPO transactionPO : transactionPOS) {
                TransactionReceipt receipt = new TransactionReceipt();
                receipt.setTxId(transactionPO.getTxId());
                receipt.setResult(StringUtils.equals(transactionPO.getExecuteResult(), "1") ? true : false);
                receipt.setErrorCode(transactionPO.getErrorCode());
                receiptMap.put(transactionPO.getTxId(), receipt);
            }
        } else {
            List<TransactionReceiptPO> pos = transactionRocksDao.queryTxReceipts(txIds);
            receiptMap = new HashMap<>(pos.size());
            for (TransactionReceiptPO po : pos) {
                TransactionReceipt receipt = new TransactionReceipt();
                receipt.setTxId(po.getTxId());
                receipt.setResult(po.isResult());
                receipt.setErrorCode(po.getErrorCode());
                receiptMap.put(po.getTxId(), receipt);
            }
        }

        return receiptMap;
    }

    /**
     * query transactions by block height
     *
     * @param blockHeight
     * @return
     */
    public List<SignedTransaction> queryTransactions(Long blockHeight) {
        List<TransactionPO> txPOs;
        if (!initConfig.isUseMySQL()) {
            BlockPO blockPO = blockRocksDao.get(String.valueOf(blockHeight));
            return blockPO == null ? null : blockPO.getSignedTxs();
        } else {
            txPOs = transactionDao.queryByBlockHeight(blockHeight);
        }

        if (CollectionUtils.isEmpty(txPOs)) {
            return null;
        }
        return convertPOsToBOs(txPOs);
    }

    public List<SignedTransaction> convertPOsToBOs(List<TransactionPO> txPOs) {
        List<SignedTransaction> txs = new ArrayList<>();
        for (TransactionPO tx : txPOs) {
            SignedTransaction signedTransaction = new SignedTransaction();
            CoreTransaction coreTx = BeanConvertor.convertBean(tx, CoreTransaction.class);
            if (tx.getBizModel() != null) {
                coreTx.setBizModel(JSON.parseObject(tx.getBizModel()));
            }
            String actionDatas = tx.getActionDatas();
            List<Action> actions = JSON.parseArray(actionDatas, Action.class);
            coreTx.setActionList(actions);
            signedTransaction.setCoreTx(coreTx);
            List<SignInfo> signDatas = JSON.parseArray(tx.getSignDatas(), SignInfo.class);
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
     * @param txReceiptMap
     */
    public void batchSaveTransaction(Long blockHeight, Date blockTime, List<SignedTransaction> txs,
        Map<String, TransactionReceipt> txReceiptMap) {
        if (log.isDebugEnabled()) {
            log.debug("[TransactionRepository.batchSaveTransaction] is start");
        }
        if (CollectionUtils.isEmpty(txs)) {
            log.info("[batchSaveTransaction] txs is empty");
            return;
        }

        if (initConfig.isUseMySQL()) {
            List<TransactionPO> txPOs = buildTransactionPOs(blockHeight, blockTime, txs, txReceiptMap);
            try {
                int r = transactionJDBCDao.batchInsertTransaction(txPOs);
                if (r != txPOs.size()) {
                    log.error("[batchSaveTransaction]batch insert txs has error");
                    throw new SlaveException(SlaveErrorEnum.SLAVE_UNKNOWN_EXCEPTION);
                }
            } catch (DuplicateKeyException e) {
                log.error("[batchSaveTransaction] is idempotent blockHeight:{}", blockHeight);
                throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
            }
        } else {
            transactionRocksDao.batchInsert(buildTxReceiptPO(blockHeight, txReceiptMap));
        }
        log.info("[TransactionRepository.batchSaveTransaction] is end");
    }

    private List<TransactionReceiptPO> buildTxReceiptPO(Long blockHeight, Map<String, TransactionReceipt> txReceiptMap) {
        Profiler.enter("build txReceiptPOs");
        List<TransactionReceiptPO> receiptPOS = new ArrayList<>();
        for (String txId : txReceiptMap.keySet()) {
            TransactionReceipt receipt = txReceiptMap.get(txId);
            if (null != receipt) {
                TransactionReceiptPO po = new TransactionReceiptPO();
                po.setBlockHeight(blockHeight);
                po.setErrorCode(receipt.getErrorCode());
                po.setResult(receipt.isResult());
                po.setTxId(receipt.getTxId());
                receiptPOS.add(po);
            }
        }
        Profiler.release();
        return receiptPOS;
    }

    public List<TransactionPO> buildTransactionPOs(Long blockHeight, Date blockTime, List<SignedTransaction> txs,
        Map<String, TransactionReceipt> txReceiptMap) {
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
            po.setSendTime(coreTx.getSendTime());
            TransactionReceipt receipt = txReceiptMap.get(coreTx.getTxId());
            if (receipt != null) {
                po.setExecuteResult(receipt.isResult() ? "1" : "0");
                po.setErrorCode(receipt.getErrorCode());
            }
            txPOs.add(po);
        }

        return txPOs;
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

        if (initConfig.isUseMySQL()) {
            List<TransactionPO> transactionPOS = transactionDao.queryByTxIds(txIds);
            if (CollectionUtils.isEmpty(transactionPOS)) {
                return datas;
            }
            for (TransactionPO transactionPO : transactionPOS) {
                datas.add(transactionPO.getTxId());
            }
        } else {
            List<String> resultTxIds = transactionRocksDao.queryTxIds(txIds);
            if (!CollectionUtils.isEmpty(resultTxIds)) {
                datas.addAll(resultTxIds);
            }
        }
        return datas;
    }

    public List<String> queryTxIdsByIds(List<String> txIds) {
        if (CollectionUtils.isEmpty(txIds)) {
            return null;
        }

        List<String> datas = new ArrayList<>();
        if (initConfig.isUseMySQL()) {
            List<TransactionPO> transactionPOS = transactionDao.queryByTxIds(txIds);

            if (CollectionUtils.isEmpty(transactionPOS)) {
                return null;
            }

            for (TransactionPO transactionPO : transactionPOS) {
                datas.add(transactionPO.getTxId());
            }
        } else {
            List<String> resultTxIds = transactionRocksDao.queryTxIds(txIds);
            if (!CollectionUtils.isEmpty(resultTxIds)) {
                datas.addAll(resultTxIds);
            }
        }
        return datas;
    }

    /**
     * query by condition、page
     *
     * @param blockHeight
     * @param txId
     * @param sender
     * @param pageNum
     * @param pageSize
     * @return
     */
    public List<CoreTransactionVO> queryTxsWithCondition(Long blockHeight, String txId, String sender, Integer pageNum,
        Integer pageSize) {
        if (null != txId) {
            txId = txId.trim();
        }
        if (null != sender) {
            sender = sender.trim();
        }
        List<TransactionPO> list =
            transactionDao.queryTxWithCondition(blockHeight, txId, sender, (pageNum - 1) * pageSize, pageSize);
        return BeanConvertor.convertList(list, CoreTransactionVO.class);
    }

    @Deprecated public long countTxsWithCondition(Long blockHeight, String txId, String sender) {
        return transactionDao.countTxWithCondition(blockHeight, txId, sender);
    }

    /**
     * query tx by id
     *
     * @param txId
     * @return
     */
    public CoreTransactionVO queryTxById(String txId) {
        TransactionPO transactionPO = transactionDao.queryByTxId(txId);
        if (null == transactionPO) {
            return null;
        }
        return BeanConvertor.convertBean(transactionPO, CoreTransactionVO.class);
    }

    /**
     * return CoreTransactionVO from db
     *
     * @param txIds
     * @return
     */
    public List<CoreTransactionVO> queryTxs(List<String> txIds) {
        if (CollectionUtils.isEmpty(txIds)) {
            return null;
        }
        List<TransactionPO> transactionPOS = transactionDao.queryByTxIds(txIds);
        return BeanConvertor.convertList(transactionPOS, CoreTransactionVO.class);
    }

}
