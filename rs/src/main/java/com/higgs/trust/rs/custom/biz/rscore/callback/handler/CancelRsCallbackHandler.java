package com.higgs.trust.rs.custom.biz.rscore.callback.handler;

import com.higgs.trust.rs.custom.api.enums.BankChainExceptionCodeEnum;
import com.higgs.trust.rs.custom.api.enums.CustomExceptionCodeEnum;
import com.higgs.trust.rs.custom.api.enums.RequestEnum;
import com.higgs.trust.rs.custom.api.enums.RequestStatusEnum;
import com.higgs.trust.rs.custom.dao.RequestDao;
import com.higgs.trust.rs.custom.dao.po.RequestPO;
import com.higgs.trust.rs.custom.exceptions.BankChainException;
import com.higgs.trust.rs.custom.exceptions.CustomException;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author tangfashuang
 * @date 2018/06/10 11:02
 * @desc cancel rs callback handler
 */
@Service
@Slf4j
public class CancelRsCallbackHandler {
    @Autowired private TransactionTemplate txRequired;
    @Autowired private RequestDao requestDao;

    public void process(RespData<CoreTransaction> respData) {

        if (null == respData || null == respData.getData()) {
            log.error("[process] cancel RS callback error, respData or respData.getData() is null");
            throw new BankChainException(BankChainExceptionCodeEnum.IdentityCallbackProcessException,
                "[process] cancel RS callback error, respData or respData.getData() is null");
        }

        try {
            String requestId = respData.getData().getTxId();

            RequestPO requestPO = new RequestPO();
            requestPO.setRequestId(requestId);

            if (respData.isSuccess()) {
                requestPO.setStatus(RequestStatusEnum.SUCCESS.getCode());
            } else {
                requestPO.setStatus(RequestStatusEnum.FAILED.getCode());
            }

            // 开启事务执行DB操作
            txRequired.execute(new TransactionCallbackWithoutResult() {
                @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
                    log.info("[process] transaction start，txId={}", requestId);
                    requestDao.updateStatusByRequestId(requestPO.getRequestId(), RequestEnum.PROCESS.getCode(),
                        requestPO.getStatus(), respData.getRespCode(), respData.getMsg());
                    log.info("[process] transaction success，txId={}", requestId);
                }
            });
        } catch (Throwable e) {
            log.error("[process] cancel RS error", e);
            throw new CustomException(CustomExceptionCodeEnum.CancelRsCallBackProcessException,
                "[process] cancel RS error");
        }
    }
}
