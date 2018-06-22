package com.higgs.trust.rs.custom.biz.api.impl.bill;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.api.RsBlockChainService;
import com.higgs.trust.rs.custom.api.enums.BillStatusEnum;
import com.higgs.trust.rs.custom.api.enums.RespCodeEnum;
import com.higgs.trust.rs.custom.dao.ReceivableBillDao;
import com.higgs.trust.rs.core.dao.RequestDao;
import com.higgs.trust.rs.custom.dao.po.ReceivableBillPO;
import com.higgs.trust.rs.core.dao.po.RequestPO;
import com.higgs.trust.rs.custom.exceptions.BillException;
import com.higgs.trust.rs.custom.model.BizTypeConst;
import com.higgs.trust.rs.custom.util.converter.BillConvertor;
import com.higgs.trust.rs.custom.util.converter.CoreTransactionConvertor;
import com.higgs.trust.rs.custom.util.converter.RequestConvertor;
import com.higgs.trust.rs.custom.util.converter.UTXOActionConvertor;
import com.higgs.trust.rs.custom.vo.BillCreateVO;
import com.higgs.trust.rs.custom.vo.BillTransferVO;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.utxo.TxOut;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * BillService helper
 *
 * @author lingchao
 * @create 2018年05月13日16:11
 */
@Service
@Slf4j
public class BillServiceHelper {

    @Autowired
    private UTXOActionConvertor utxoActionConvertor;

    @Autowired
    private CoreTransactionConvertor coreTransactionConvertor;

    @Autowired
    private CoreTransactionService coreTransactionService;

    @Autowired
    private ReceivableBillDao receivableBillDao;

    @Autowired
    private RsConfig rsConfig;

    @Autowired
    private RsBlockChainService rsBlockChainService;

    @Autowired
    private RequestDao requestDao;

    /**
     * requestIdempotent
     *
     * @param requestId
     * @return
     */
    public RespData<?> requestIdempotent(String requestId) {
        RespData<?> respData = null;
        RequestPO requestPO = requestDao.queryByRequestId(requestId);
        if (null != requestPO) {
            return new RespData<>(requestPO.getRespCode(), requestPO.getRespMsg());
        }
        return respData;
    }

    /**
     * billCreateVO 请求入库
     *
     * @param billCreateVO
     */
    public RespData<?> insertRequest(BillCreateVO billCreateVO) {
        RespData<?> respData = null;
        RequestPO requestPO = RequestConvertor.buildRequestPO(billCreateVO);

        try {
            requestDao.add(requestPO);
        } catch (DuplicateKeyException e) {
            log.error("requestId : {} for billCreateVO: {} is idempotent", requestPO.getRequestId(), requestPO);
            respData = requestIdempotent(requestPO.getRequestId());
        }
        return respData;
    }

    /**
     * bill create入库
     *
     * @param billCreateVO
     */
    public void insertBill(BillCreateVO billCreateVO, Long actionIndex, Long index) {
        ReceivableBillPO receivableBillPO = BillConvertor.buildBill(billCreateVO, actionIndex, index, rsConfig.getContractAddress());
        try {
            receivableBillDao.add(receivableBillPO);
        } catch (DuplicateKeyException e) {
            log.error("receivableBillPO : {} for txId : {} is idempotent", receivableBillPO, receivableBillPO.getTxId());
            throw new RuntimeException("receivableBillPO is idempotent");
        }
    }

    /**
     * build Create Bill And Send
     *
     * @param isIdentityExist
     * @param billCreateVO
     * @return
     */
    public RespData<?> buildCreateBillAndSend(boolean isIdentityExist, BillCreateVO billCreateVO) {

        List<Action> actionList = null;
        if (isIdentityExist) {
            actionList = utxoActionConvertor.buildCreateBillActionList(billCreateVO);
        } else {
            actionList = utxoActionConvertor.buildCreateBillWithIdentityActionList(billCreateVO);
        }

        JSONObject bizModel = new JSONObject();
        try {
            bizModel = JSON.parseObject(billCreateVO.getBizModel());
        } catch (Throwable e) {
            bizModel.put("bizModel", billCreateVO.getBizModel());
        }

        //创建coreTx
        CoreTransaction coreTransaction = coreTransactionConvertor.buildBillCoreTransaction(billCreateVO.getRequestId(), bizModel, actionList,
            InitPolicyEnum.UTXO_ISSUE.getPolicyId());

        //insert bill
        for (Action action : actionList) {
            if (action.getType() == ActionTypeEnum.UTXO) {
                UTXOAction utxoAction = (UTXOAction) action;
                List<TxOut> outputList = utxoAction.getOutputList();
                for (TxOut txOut : outputList) {
                    insertBill(billCreateVO, txOut.getActionIndex().longValue(), txOut.getIndex().longValue());
                }
            }
        }

        return submitTx(coreTransaction);
    }


    /**
     * billTransferVO 请求入库
     *
     * @param billTransferVO
     */
    public RespData<?> insertRequest(BillTransferVO billTransferVO) {
        RespData<?> respData = null;
        RequestPO requestPO = RequestConvertor.buildRequestPO(billTransferVO);

        try {
            requestDao.add(requestPO);
        } catch (DuplicateKeyException e) {
            log.error("requestId : {} for billTransferVO: {} is idempotent", requestPO.getRequestId(), requestPO);
            respData = requestIdempotent(requestPO.getRequestId());
        }
        return respData;
    }

    /**
     * bill transfer new 入库
     *
     * @param billTransferVO
     */
    public void insertBill(BillTransferVO billTransferVO, Long actionIndex, Long index) {

        ReceivableBillPO receivableBillParam = new ReceivableBillPO();
        receivableBillParam.setBillId(billTransferVO.getBillId());
        receivableBillParam.setStatus(BillStatusEnum.UNSPENT.getCode());
        List<ReceivableBillPO> receivableBillPOList = receivableBillDao.queryByList(receivableBillParam);

        if (CollectionUtils.isEmpty(receivableBillPOList) || receivableBillPOList.size() > 1) {
            log.error("build Transfer Bill WithIdentity  ActionList  error for receivableBillPOList is null or receivableBillPOList size bigger than 1," + "build Transfer Bill WithIdentity  ActionList  error, receivableBillPOList: {}", receivableBillPOList);
            throw new BillException(RespCodeEnum.BILL_TRANSFER_INVALID_PARAM);
        }
        ReceivableBillPO receivableBill = receivableBillPOList.get(0);

        ReceivableBillPO receivableBillPO = BillConvertor.buildBill(billTransferVO, receivableBill, actionIndex, index);
        try {
            receivableBillDao.add(receivableBillPO);
        } catch (DuplicateKeyException e) {
            log.error("receivableBillPO : {} for txId : {} is idempotent", receivableBillPO, receivableBillPO.getTxId());
            throw new RuntimeException("receivableBillPO is idempotent");
        }
    }


    /**
     * build Transfer Bill And Send
     *
     * @param isIdentityExist
     * @param billTransferVO
     * @return
     */
    public RespData<?> buildTransferBillAndSend(boolean isIdentityExist, BillTransferVO billTransferVO) {
        List<Action> actionList = null;
        if (isIdentityExist) {
            actionList = utxoActionConvertor.buildTransferBillActionList(billTransferVO);
        } else {
            actionList = utxoActionConvertor.buildTransferBillWithIdentityActionList(billTransferVO);
        }
        //创建coreTx

        JSONObject bizModel = new JSONObject();
        try {
            bizModel = JSON.parseObject(billTransferVO.getBizModel());
        } catch (Throwable e) {
            bizModel.put("bizModel", billTransferVO.getBizModel());
        }

        CoreTransaction coreTransaction = coreTransactionConvertor.buildBillCoreTransaction(billTransferVO.getRequestId(), bizModel, actionList,
            BizTypeConst.TRANSFER_UTXO);

        //insert bill
        for (Action action : actionList) {
            if (action.getType() == ActionTypeEnum.UTXO) {
                UTXOAction utxoAction = (UTXOAction) action;
                List<TxOut> outputList = utxoAction.getOutputList();
                for (TxOut txOut : outputList) {
                    insertBill(billTransferVO, txOut.getActionIndex().longValue(), txOut.getIndex().longValue());
                }
            }
        }
        return submitTx(coreTransaction);
    }

    /**
     * 发送交易到rs-core
     */
    private RespData<?> submitTx (CoreTransaction coreTransaction){
        //send and get callback result
        try {
            coreTransactionService.submitTx(coreTransaction);
        } catch (RsCoreException e) {
            if (e.getCode() == RsCoreErrorEnum.RS_CORE_IDEMPOTENT) {
                return requestIdempotent(coreTransaction.getTxId());
            }
        }
        return new RespData<>();
    }

    /**
     * update request status
     *
     * @param txId
     * @param fromStatus
     * @param toStatus
     * @param respCode
     * @param msg
     */
    public void updateRequestStatus(String txId, String fromStatus, String toStatus, String respCode, String msg) {
        //update process status from process to done
        int isUpdated = requestDao.updateStatusByRequestId(txId, fromStatus, toStatus, respCode, msg);

        if (0 == isUpdated) {
            log.error("update request status  for requestId :{} , to status: {} is failed!", txId, toStatus);
            throw new RuntimeException("update request  status failed!");
        }
    }


}
