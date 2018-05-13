package com.higgs.trust.rs.custom.biz.api.impl.bill;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.rs.custom.dao.RequestDao;
import com.higgs.trust.rs.custom.dao.po.RequestPO;
import com.higgs.trust.rs.custom.model.RespData;
import com.higgs.trust.rs.custom.util.converter.CoreTransactionConvertor;
import com.higgs.trust.rs.custom.util.converter.RequestConvertor;
import com.higgs.trust.rs.custom.util.converter.UTXOActionConvertor;
import com.higgs.trust.rs.custom.vo.BillCreateVO;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import lombok.extern.slf4j.Slf4j;
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
     * 请求入库
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

    public RespData<?> buildCreateBillAndSend(boolean isIdentityExist, BillCreateVO billCreateVO) {
        RespData<?> respData = new RespData<>();
        List<Action> actionList =  null;
        if (isIdentityExist){
            actionList =  utxoActionConvertor.buildCreateBillActionList(billCreateVO);
        }else {
            actionList =  utxoActionConvertor.buildCreateBillWithIdentityActionList(billCreateVO);
        }

        CoreTransaction coreTransaction = coreTransactionConvertor.buildBillCoreTransaction(billCreateVO.getRequestId(), JSON.parseObject(billCreateVO.getBizModel()), actionList);



        return respData;
    }


}
