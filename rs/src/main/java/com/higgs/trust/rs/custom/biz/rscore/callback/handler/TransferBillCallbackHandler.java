package com.higgs.trust.rs.custom.biz.rscore.callback.handler;

import com.higgs.trust.rs.custom.api.enums.BillStatusEnum;
import com.higgs.trust.rs.custom.api.enums.RequestEnum;
import com.higgs.trust.rs.custom.dao.ReceivableBillDao;
import com.higgs.trust.rs.custom.dao.RequestDao;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * transfer callback handler
 *
 * @author lingchao
 * @create 2018年05月13日23:03
 */
@Service
@Slf4j
public class TransferBillCallbackHandler {
    @Autowired
    private TransactionTemplate txRequired;
    @Autowired
    private ReceivableBillDao receivableBillDao;
    @Autowired
    private RequestDao requestDao;

    public void process(RespData<CoreTransaction> respData) {
        try {
            // 开启事务执行DB操作
            txRequired.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    CoreTransaction coreTransaction = respData.getData();
                    List<Action> actionList = coreTransaction.getActionList();
                    String toStatus = null;
                    Long actionIndex = null;

                    if (respData.isSuccess()) {
                        toStatus = BillStatusEnum.UNSPENT.getCode();

                    } else {
                        toStatus = BillStatusEnum.FAILED.getCode();
                    }

                    if (actionList.size() > 1) {
                        actionIndex = 1L;

                    } else {
                        actionIndex = 0L;
                    }

                    int isUpdate = receivableBillDao.updateStatus(coreTransaction.getTxId(), actionIndex, Long.valueOf(actionList.get(actionIndex.intValue()).getIndex()), BillStatusEnum.PROCESS.getCode(), toStatus);

                    if (0 == isUpdate) {
                        log.error(" transfer bill update status  for txId :{} ,actionIndex :{} ,index :{} to status: {} is failed!", coreTransaction.getTxId(), actionIndex, actionList.get(actionIndex.intValue()).getIndex(), toStatus);
                        throw new RuntimeException("create bill update status failed!");
                    }

                    if (respData.isSuccess()) {
                        TxIn txIn = ((UTXOAction) actionList.get(actionIndex.intValue())).getInputList().get(0);
                        int isUpdateSTXO = receivableBillDao.updateStatus(txIn.getTxId(), txIn.getActionIndex().longValue(), txIn.getIndex().longValue(), BillStatusEnum.UNSPENT.getCode(), BillStatusEnum.SPENT.getCode());
                        if (0 == isUpdateSTXO) {
                            log.error(" transfer spend bill to update status  for txId :{} ,actionIndex :{} ,index :{} to status: {} is failed!", txIn.getTxId(), txIn.getActionIndex().longValue(), txIn.getIndex().longValue(), BillStatusEnum.SPENT.getCode());
                            throw new RuntimeException("create bill update status failed!");
                        }
                    }

                    int isUpdated = requestDao.updateStatusByRequestId(coreTransaction.getTxId(), RequestEnum.PROCESS.getCode(), RequestEnum.DONE.getCode(), respData.getRespCode(), respData.getMsg());

                    if (0 == isUpdated) {
                        log.error("update request status  for requestId :{} , to status: {} is failed!", coreTransaction.getTxId(), RequestEnum.DONE.getCode());
                        throw new RuntimeException("update request  status failed!");
                    }

                }
            });
        } catch (Throwable e) {
            log.error("[transfer] transfer bill error", e);
            throw e;
        }
    }
}