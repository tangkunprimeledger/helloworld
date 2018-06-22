package com.higgs.trust.rs.custom.biz.rscore.callback.handler;

import com.higgs.trust.rs.custom.api.enums.BillStatusEnum;
import com.higgs.trust.rs.custom.api.enums.RequestEnum;
import com.higgs.trust.rs.custom.biz.api.impl.bill.BillServiceHelper;
import com.higgs.trust.rs.custom.dao.ReceivableBillDao;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.utxo.TxOut;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * create bill callback handler
 *
 * @author lingchao
 * @create 2018年05月13日23:03
 */
@Service
@Slf4j
public class CreateBillCallbackHandler {
    @Autowired
    private TransactionTemplate txRequired;
    @Autowired
    private ReceivableBillDao receivableBillDao;
    @Autowired
    private BillServiceHelper billServiceHelper;


    public void process(RespData<CoreTransaction> respData) {
        try {

            // 开启事务执行DB操作
            txRequired.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    CoreTransaction coreTransaction = respData.getData();
                    log.info("coreTransaction:{}", coreTransaction);
                    List<Action> actionList = coreTransaction.getActionList();
                    String toStatus = null;
                    Long actionIndex = null;
                    if (respData.isSuccess()) {
                        toStatus = BillStatusEnum.UNSPENT.getCode();
                    } else {
                        toStatus = BillStatusEnum.FAILED.getCode();
                    }

                    for (Action action : actionList) {
                        if (action.getType() == ActionTypeEnum.UTXO) {
                            UTXOAction utxoAction = (UTXOAction) action;
                            List<TxOut> outputList = utxoAction.getOutputList();
                            for (TxOut txOut : outputList) {
                                //update bill status from process to
                                int isUpdate = receivableBillDao.updateStatus(coreTransaction.getTxId(), txOut.getActionIndex().longValue(), txOut.getIndex().longValue(), BillStatusEnum.PROCESS.getCode(), toStatus);

                                if (0 == isUpdate) {
                                    log.error(" create bill update status  for txId :{} ,actionIndex :{} ,index :{} to status: {} is failed!", coreTransaction.getTxId(), actionIndex, actionList.get(actionIndex.intValue()).getIndex(), toStatus);
                                    throw new RuntimeException("create bill update status failed!");
                                }
                            }
                        }
                    }

                    //update request status
                    billServiceHelper.updateRequestStatus(coreTransaction.getTxId(), RequestEnum.PROCESS.getCode(), RequestEnum.DONE.getCode(), respData.getRespCode(), respData.getMsg());
                }
            });
        } catch (Throwable e) {
            log.error("[create] create bill error", e);
            throw e;
        }
    }
}
