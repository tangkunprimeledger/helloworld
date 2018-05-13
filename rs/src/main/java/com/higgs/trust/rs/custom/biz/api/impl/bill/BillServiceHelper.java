package com.higgs.trust.rs.custom.biz.api.impl.bill;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.rs.common.enums.BizTypeEnum;
import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.api.SignService;
import com.higgs.trust.rs.custom.api.enums.BillStatusEnum;
import com.higgs.trust.rs.custom.dao.ReceivableBillDao;
import com.higgs.trust.rs.custom.dao.RequestDao;
import com.higgs.trust.rs.custom.dao.po.ReceivableBillPO;
import com.higgs.trust.rs.custom.dao.po.RequestPO;
import com.higgs.trust.rs.custom.util.converter.BillConvertor;
import com.higgs.trust.rs.custom.util.converter.CoreTransactionConvertor;
import com.higgs.trust.rs.custom.util.converter.RequestConvertor;
import com.higgs.trust.rs.custom.util.converter.UTXOActionConvertor;
import com.higgs.trust.rs.custom.vo.BillCreateVO;
import com.higgs.trust.rs.custom.vo.BillTransferVO;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
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
    private RequestDao requestDao;

    @Autowired
    private UTXOActionConvertor utxoActionConvertor;

    @Autowired
    private CoreTransactionConvertor coreTransactionConvertor;

    @Autowired
    private SignService signService;

    @Autowired
    private CoreTransactionService coreTransactionService;

    @Autowired
    private ReceivableBillDao receivableBillDao;

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
        ReceivableBillPO receivableBillPO = BillConvertor.buildBill(billCreateVO, actionIndex, index);
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

        //创建coreTx
        CoreTransaction coreTransaction = coreTransactionConvertor.buildBillCoreTransaction(billCreateVO.getRequestId(), JSON.parseObject(billCreateVO.getBizModel()), actionList);
        //签名
        String signData = signService.signTx(coreTransaction).getData();

        //insert bill
        if (isIdentityExist){
            insertBill(billCreateVO, 1L, Long.valueOf(actionList.get(1).getIndex()));
        }else {
            insertBill(billCreateVO, 0L, Long.valueOf(actionList.get(0).getIndex()));
        }

        //send and get callback result
        RespData<?> respData = coreTransactionService.syncSubmitTxForEnd(BizTypeEnum.ISSUE_UTXO, coreTransaction, signData);
        return respData;
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
            log.error("build Transfer Bill WithIdentity  ActionList  error, receivableBillPOList: {}", receivableBillPOList);
            throw new RuntimeException("build Transfer Bill WithIdentity  ActionList  error for receivableBillPOList is null or receivableBillPOList size bigger than 1");
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
        RespData<?> respData = new RespData<>();
        List<Action> actionList = null;
        if (isIdentityExist) {
            actionList = utxoActionConvertor.buildTransferBillActionList(billTransferVO);
        } else {
            actionList = utxoActionConvertor.buildTransferBillWithIdentityActionList(billTransferVO);
        }
        //创建coreTx
        CoreTransaction coreTransaction = coreTransactionConvertor.buildBillCoreTransaction(billTransferVO.getRequestId(), JSON.parseObject(billTransferVO.getBizModel()), actionList);
        //签名
        String signData = signService.signTx(coreTransaction).getData();

        //insert bill
        if (isIdentityExist){
            insertBill(billTransferVO, 1L, Long.valueOf(actionList.get(1).getIndex()));
        }else {
            insertBill(billTransferVO, 0L, Long.valueOf(actionList.get(0).getIndex()));
        }

        //send and get callback result
        RespData rsRespData = coreTransactionService.syncSubmitTxForEnd(BizTypeEnum.TRANSFER_UTXO, coreTransaction, signData);
        BeanUtils.copyProperties(rsRespData, respData);
        return respData;
    }


}
