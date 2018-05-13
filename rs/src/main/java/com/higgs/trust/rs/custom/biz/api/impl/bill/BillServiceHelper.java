package com.higgs.trust.rs.custom.biz.api.impl.bill;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.rs.common.enums.BizTypeEnum;
import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.api.SignService;
import com.higgs.trust.rs.custom.dao.RequestDao;
import com.higgs.trust.rs.custom.dao.po.RequestPO;
import com.higgs.trust.rs.custom.util.converter.CoreTransactionConvertor;
import com.higgs.trust.rs.custom.util.converter.RequestConvertor;
import com.higgs.trust.rs.custom.util.converter.UTXOActionConvertor;
import com.higgs.trust.rs.custom.vo.BillCreateVO;
import com.higgs.trust.rs.custom.vo.BillTransferVO;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import lombok.extern.slf4j.Slf4j;
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

        //send and get callback result
        com.higgs.trust.slave.api.vo.RespData rsRespData = coreTransactionService.syncSubmitTxForEnd(BizTypeEnum.TRANSFER_UTXO, coreTransaction, signData);
        BeanUtils.copyProperties(rsRespData, respData);
        return respData;
    }



}
