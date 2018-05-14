package com.higgs.trust.rs.custom.biz.api.impl.bill;

import com.higgs.trust.rs.core.api.RsBlockChainService;
import com.higgs.trust.rs.custom.api.bill.BillService;
import com.higgs.trust.rs.custom.api.enums.RespCodeEnum;
import com.higgs.trust.rs.custom.vo.BillCreateVO;
import com.higgs.trust.rs.custom.vo.BillTransferVO;
import com.higgs.trust.slave.api.vo.RespData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * bill service impl
 *
 * @author lingchao
 * @create 2018年05月13日15:24
 */
@Service
@Slf4j
public class BillServiceImpl implements BillService {
    @Autowired
    private BillServiceHelper billServiceHelper;
    @Autowired
    private RsBlockChainService rsBlockChainService;

    /**
     * 创建票据方法
     *
     * @param billCreateVO
     * @return
     */
    @Override
    public RespData<?> create(BillCreateVO billCreateVO) {
        RespData<?> respData = null;
        try {
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
            boolean isIdentityExist = rsBlockChainService.isExistedIdentity(billCreateVO.getHolder());

            //组装UTXO,CoreTransaction,签名，下发
            respData = billServiceHelper.buildCreateBillAndSend(isIdentityExist, billCreateVO);
        } catch (Throwable e) {
            log.error("create bill error", e);
            respData = new RespData<>(RespCodeEnum.SYS_FAIL.getRespCode(), RespCodeEnum.SYS_FAIL.getMsg());
        }
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
        try {
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
            boolean isIdentityExist = rsBlockChainService.isExistedIdentity(billTransferVO.getNextHolder());

            //组装UTXO,CoreTransaction,签名，下发
            respData = billServiceHelper.buildTransferBillAndSend(isIdentityExist, billTransferVO);
        } catch (Throwable e) {
            log.error("transfer bill error", e);
            respData = new RespData<>(RespCodeEnum.SYS_FAIL.getRespCode(), RespCodeEnum.SYS_FAIL.getMsg());
        }
        return respData;
    }
}
