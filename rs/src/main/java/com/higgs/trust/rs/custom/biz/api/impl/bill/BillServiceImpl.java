package com.higgs.trust.rs.custom.biz.api.impl.bill;

import com.higgs.trust.rs.custom.api.bill.BillService;
import com.higgs.trust.rs.custom.vo.BillCreateVO;
import com.higgs.trust.rs.custom.vo.BillTransferVO;
import com.higgs.trust.slave.api.vo.RespData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * bill service impl
 *
 * @author lingchao
 * @create 2018年05月13日15:24
 */
@Service
public class BillServiceImpl implements BillService {
    @Autowired
    private BillServiceHelper billServiceHelper;

    /**
     * 创建票据方法
     *
     * @param billCreateVO
     * @return
     */
    @Override
    public RespData<?> create(BillCreateVO billCreateVO) {
        RespData<?> respData = null;
        //初步幂等校验
        respData = billServiceHelper.requestIdempotent(billCreateVO.getRequestId());
        if (null != respData) {
            return respData;
        }
        //请求入库
        respData = billServiceHelper.insertRequest(billCreateVO);
        if (null != respData) {
            return respData;
        }
        //identity 是否存在
        //##############################//
        boolean isIdentityExist = true;

        //组装UTXO,CoreTransaction,签名，下发
        respData = billServiceHelper.buildCreateBillAndSend(isIdentityExist, billCreateVO);
        return respData;
    }


    /**
     * 票据转移
     *
     * @param billTransferVO
     * @return
     */

    @Override
    public RespData<?> transfer(BillTransferVO billTransferVO) {
        RespData<?> respData = null;
        //初步幂等校验
        respData = billServiceHelper.requestIdempotent(billTransferVO.getRequestId());
        if (null != respData) {
            return respData;
        }
        //请求入库
        respData = billServiceHelper.insertRequest(billTransferVO);
        if (null != respData) {
            return respData;
        }
        //identity 是否存在
        //##############################//
        boolean isIdentityExist = true;

        //组装UTXO,CoreTransaction,签名，下发
        respData = billServiceHelper.buildTransferBillAndSend(isIdentityExist, billTransferVO);
        return respData;
    }
}
