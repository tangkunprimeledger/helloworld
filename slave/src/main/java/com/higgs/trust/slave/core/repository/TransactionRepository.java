package com.higgs.trust.slave.core.repository;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.slave.api.vo.TransactionVO;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.po.transaction.ActionPO;
import com.higgs.trust.slave.dao.po.transaction.TransactionPO;
import com.higgs.trust.slave.dao.transaction.ActionDao;
import com.higgs.trust.slave.dao.transaction.TransactionDao;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
    @Autowired ActionDao actionDao;

    public boolean isExist(String txId) {
        TransactionVO transactionVO = transactionDao.queryByTxId(txId);
        if (null != transactionVO) {
            return true;
        }
        return false;
    }

    /**
     * query transactions by block height
     *
     * @param blockHeight
     * @return
     */
    public List<SignedTransaction> queryTransactions(Long blockHeight) {
        List<TransactionPO> txPOs = transactionDao.queryByBlockHeight(blockHeight);
        if(CollectionUtils.isEmpty(txPOs)){
            return null;
        }
        List<SignedTransaction> txs = new ArrayList<>();
        for(TransactionPO tx : txPOs){
            SignedTransaction signedTransaction = new SignedTransaction();
            CoreTransaction coreTx = BeanConvertor.convertBean(tx,CoreTransaction.class);
            coreTx.setBizModel(JSON.parseObject(String.valueOf(tx.getBizModel())));
            List<ActionPO> actionPOs = actionDao.queryByTxId(tx.getTxId());
            List<Action> actions = new ArrayList<>();
            for(ActionPO actionPO : actionPOs){
                Action action = JSON.parseObject(String.valueOf(actionPO.getData()),Action.class);
                actions.add(action);
            }
            coreTx.setActionList(actions);
            signedTransaction.setCoreTx(coreTx);
            List<String> signDatas = JSON.parseArray(tx.getSignDatas(),String.class);
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
     */
    public void batchSaveTransaction(Long blockHeight,Date blockTime,List<SignedTransaction> txs){
        if(CollectionUtils.isEmpty(txs)){
            log.warn("[batchSaveTransaction] txs is empty");
            return;
        }
        List<TransactionPO> txPOs = new ArrayList<>();
        List<ActionPO> actionPOs = new ArrayList<>();
        for(SignedTransaction tx : txs){
            CoreTransaction coreTx = tx.getCoreTx();
            TransactionPO po = BeanConvertor.convertBean(coreTx,TransactionPO.class);
            if(coreTx.getBizModel() != null) {
                po.setBizModel(coreTx.getBizModel().toJSONString());
            }
            po.setBlockHeight(blockHeight);
            po.setBlockTime(blockTime);
            po.setSignDatas(JSON.toJSONString(tx.getSignatureList()));
            txPOs.add(po);
            List<Action> actions = coreTx.getActionList();
            for(Action action : actions){
                ActionPO actionPO = new ActionPO();
                actionPO.setTxId(coreTx.getTxId());
                actionPO.setType(action.getType().getCode());
                actionPO.setIndex(action.getIndex());
                actionPO.setCreateTime(new Date());
                actionPO.setData(JSON.toJSONString(action));
                actionPOs.add(actionPO);
            }
        }
        try {
            int r = transactionDao.batchInsert(txPOs);
            if(r != txPOs.size()){
                log.error("[batchSaveTransaction]batch insert txs has eror");
                throw new SlaveException(SlaveErrorEnum.SLAVE_UNKNOWN_EXCEPTION);
            }
            r = actionDao.batchInsert(actionPOs);
            if(r != actionPOs.size()){
                log.error("[batchSaveTransaction]batch insert action has eror");
                throw new SlaveException(SlaveErrorEnum.SLAVE_UNKNOWN_EXCEPTION);
            }
        }catch (DuplicateKeyException e){
            log.error("[batchSaveTransaction] is idempotent", e);
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        }
    }
}
