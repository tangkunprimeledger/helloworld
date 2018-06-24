package com.higgs.trust.rs.custom.biz.api.impl.bill;

import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.api.RsBlockChainService;
import com.higgs.trust.rs.custom.api.bill.BillService;
import com.higgs.trust.rs.custom.api.enums.RespCodeEnum;
import com.higgs.trust.rs.custom.vo.BillCreateVO;
import com.higgs.trust.rs.custom.vo.BillTransferVO;
import com.higgs.trust.rs.custom.vo.TransferDetailVO;
import com.higgs.trust.slave.api.vo.RespData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    TransactionTemplate txRequired;

    @Autowired
    private RsBlockChainService rsBlockChainService;

    @Autowired
    private CoreTransactionService coreTransactionService;

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
            //开启事务
            respData = txRequired.execute(new TransactionCallback<RespData>() {
                @Override
                public RespData doInTransaction(TransactionStatus txStatus) {
                    //初步幂等校验
                    RespData<?> respData = null;
                    respData = billServiceHelper.requestIdempotent(billCreateVO.getRequestId());
                    if (null != respData) {
                        return respData;
                    }
                    //请求入库
                    respData = billServiceHelper.insertRequest(billCreateVO);
                    if (null != respData) {
                        return respData;
                    }

                    //biz check
                    respData = billServiceHelper.createBizCheck(billCreateVO);
                    if (null != respData) {
                        return respData;
                    }

                    //identity 是否存在
                    boolean isIdentityExist = rsBlockChainService.isExistedIdentity(billCreateVO.getHolder());

                    //组装UTXO,CoreTransaction，下发
                    respData = billServiceHelper.buildCreateBillAndSend(isIdentityExist, billCreateVO);
                    return respData;
                }
            });
            if(!respData.isSuccess()){
                return respData;
            }
            respData = coreTransactionService.syncWait(billCreateVO.getRequestId(), true);
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
            //开启事务
            respData = txRequired.execute(new TransactionCallback<RespData>() {
                @Override
                public RespData doInTransaction(TransactionStatus txStatus) {
                    //初步幂等校验
                    RespData<?> respData = null;
                    respData = billServiceHelper.requestIdempotent(billTransferVO.getRequestId());
                    if (null != respData) {
                        return respData;
                    }
                    //请求入库
                    respData = billServiceHelper.insertRequest(billTransferVO);
                    if (null != respData) {
                        return respData;
                    }

                    //业务校验
                    respData = billServiceHelper.transferBizCheck(billTransferVO);
                    if (null != respData) {
                        return respData;
                    }

                    //组装UTXO,CoreTransaction，下发
                    respData = billServiceHelper.buildTransferBillAndSend(billTransferVO);
                    return respData;
                }
            });
            if(!respData.isSuccess()){
                return respData;
            }
            respData =  coreTransactionService.syncWait(billTransferVO.getRequestId(), true);
        } catch (Throwable e) {
            log.error("transfer bill error", e);
            respData = new RespData<>(RespCodeEnum.SYS_FAIL.getRespCode(), RespCodeEnum.SYS_FAIL.getMsg());
        }
        return respData;
    }
}
