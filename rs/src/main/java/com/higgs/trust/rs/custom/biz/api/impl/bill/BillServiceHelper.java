package com.higgs.trust.rs.custom.biz.api.impl.bill;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.api.RsBlockChainService;
import com.higgs.trust.rs.core.dao.RequestDao;
import com.higgs.trust.rs.core.dao.po.RequestPO;
import com.higgs.trust.rs.custom.api.enums.BillStatusEnum;
import com.higgs.trust.rs.custom.api.enums.RequestEnum;
import com.higgs.trust.rs.custom.api.enums.RespCodeEnum;
import com.higgs.trust.rs.custom.dao.ReceivableBillDao;
import com.higgs.trust.rs.custom.dao.po.ReceivableBillPO;
import com.higgs.trust.rs.custom.model.BizTypeConst;
import com.higgs.trust.rs.custom.util.converter.BillConvertor;
import com.higgs.trust.rs.common.utils.CoreTransactionConvertor;
import com.higgs.trust.rs.custom.util.converter.RequestConvertor;
import com.higgs.trust.rs.custom.util.converter.UTXOActionConvertor;
import com.higgs.trust.rs.custom.vo.BillCreateVO;
import com.higgs.trust.rs.custom.vo.BillTransferVO;
import com.higgs.trust.rs.custom.vo.TransferDetailVO;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.utxo.TxOut;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        CoreTransaction coreTransaction = coreTransactionConvertor.buildBillCoreTransaction(billCreateVO.getRequestId(), bizModel, actionList, InitPolicyEnum.UTXO_ISSUE.getPolicyId());

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
     * @param requestId
     * @param utxoAction
     * @param txOut
     */
    public void insertBill(String requestId, UTXOAction utxoAction, TxOut txOut) {

        ReceivableBillPO receivableBillPO = BillConvertor.buildBill(requestId, utxoAction, txOut);
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
     * @param billTransferVO
     * @return
     */
    public RespData<?> buildTransferBillAndSend(BillTransferVO billTransferVO) {
        List<Action> actionList = utxoActionConvertor.buildTransferBillWithIdentityActionList(billTransferVO);
        //创建coreTx

        JSONObject bizModel = new JSONObject();
        try {
            bizModel = JSON.parseObject(billTransferVO.getBizModel());
        } catch (Throwable e) {
            bizModel.put("bizModel", billTransferVO.getBizModel());
        }

        CoreTransaction coreTransaction = coreTransactionConvertor.buildBillCoreTransaction(billTransferVO.getRequestId(), bizModel, actionList, BizTypeConst.TRANSFER_UTXO);

        //insert bill
        for (Action action : actionList) {
            if (action.getType() == ActionTypeEnum.UTXO) {
                UTXOAction utxoAction = (UTXOAction) action;
                List<TxOut> outputList = utxoAction.getOutputList();
                for (TxOut txOut : outputList) {
                    insertBill(billTransferVO.getRequestId(), utxoAction, txOut);
                }
            }
        }
        return submitTx(coreTransaction);
    }

    /**
     * 发送交易到rs-core
     */
    private RespData<?> submitTx(CoreTransaction coreTransaction) {
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

    /**
     * 创建票据业务校验
     * @param billCreateVO
     * @return
     */
    public RespData<?> createBizCheck(BillCreateVO  billCreateVO) {
        RespData<?> respData = null;
        ReceivableBillPO receivableBillPO = receivableBillDao.queryByBillId(billCreateVO.getBillId());
        if (null != receivableBillPO) {
            log.error("the new  crate bill for  billId：{} is  existed exception", billCreateVO.getBillId());
            updateRequestStatus(billCreateVO.getRequestId(), RequestEnum.PROCESS.getCode(), RequestEnum.DONE.getCode(), RespCodeEnum.BILL_BILLID_EXIST_EXCEPTION.getRespCode(), RespCodeEnum.BILL_BILLID_EXIST_EXCEPTION.getMsg());
            respData = new RespData(RespCodeEnum.BILL_BILLID_EXIST_EXCEPTION.getRespCode(), RespCodeEnum.BILL_BILLID_EXIST_EXCEPTION.getMsg());
        }
        return respData;
    }


    /**
     * 票据转移业务check
     *
     * @param billTransferVO
     * @return
     */
    public RespData<?> transferBizCheck(BillTransferVO billTransferVO) {
        RespData<?> respData = null;

        //bill check
        Set<String> billIds = new HashSet<>();
        billIds.add(billTransferVO.getBillId());
        List<TransferDetailVO> transferList = billTransferVO.getTransferList();
        for (TransferDetailVO transferDetailVO : transferList) {
            billIds.add(transferDetailVO.getNextBillId());
        }
        int billNum = transferList.size() + 1;
        if (billIds.size() != billNum) {
            log.error("The billId can not be the same for a transfer tx: {}  ", billTransferVO);
            updateRequestStatus(billTransferVO.getRequestId(), RequestEnum.PROCESS.getCode(), RequestEnum.DONE.getCode(), RespCodeEnum.BILL_TRANSFER_BILLID_IDEMPOTENT_FAIED.getRespCode(), RespCodeEnum.BILL_TRANSFER_BILLID_IDEMPOTENT_FAIED.getMsg());
            return new RespData(RespCodeEnum.BILL_TRANSFER_BILLID_IDEMPOTENT_FAIED.getRespCode(), RespCodeEnum.BILL_TRANSFER_BILLID_IDEMPOTENT_FAIED.getMsg());
        }

        //be  transfer bill exist check
        ReceivableBillPO receivableBillParam = new ReceivableBillPO();
        receivableBillParam.setBillId(billTransferVO.getBillId());
        receivableBillParam.setHolder(billTransferVO.getHolder());
        receivableBillParam.setStatus(BillStatusEnum.UNSPENT.getCode());
        List<ReceivableBillPO> receivableBillPOList = receivableBillDao.queryByList(receivableBillParam);
        if (CollectionUtils.isEmpty(receivableBillPOList)) {
            log.error("the bill  for holder:{} for the billId:{} with the status UNSPENT is not existed exception", billTransferVO.getHolder(), billTransferVO.getBillId());
            updateRequestStatus(billTransferVO.getRequestId(), RequestEnum.PROCESS.getCode(), RequestEnum.DONE.getCode(), RespCodeEnum.BILL_TRANSFER_INVALID_PARAM.getRespCode(), RespCodeEnum.BILL_TRANSFER_INVALID_PARAM.getMsg());
            return new RespData(RespCodeEnum.BILL_TRANSFER_INVALID_PARAM.getRespCode(), RespCodeEnum.BILL_TRANSFER_INVALID_PARAM.getMsg());
        }


        //the new bill exist check
        for (TransferDetailVO transferDetailVO : transferList) {
            ReceivableBillPO receivableBillPO = receivableBillDao.queryByBillId(transferDetailVO.getNextBillId());
            if (null != receivableBillPO) {
                log.error("the billId：{}  for nextHolder  is  existed exception", transferDetailVO.getNextBillId());
                updateRequestStatus(billTransferVO.getRequestId(), RequestEnum.PROCESS.getCode(), RequestEnum.DONE.getCode(), RespCodeEnum.BILL_BILLID_EXIST_EXCEPTION.getRespCode(), RespCodeEnum.BILL_BILLID_EXIST_EXCEPTION.getMsg());
                return new RespData(RespCodeEnum.BILL_BILLID_EXIST_EXCEPTION.getRespCode(), RespCodeEnum.BILL_BILLID_EXIST_EXCEPTION.getMsg());
            }
        }

        return respData;
    }

}
