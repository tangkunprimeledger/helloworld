package com.higgs.trust.rs.core.callback.handler;

import com.higgs.trust.rs.custom.api.enums.CustomExceptionCodeEnum;
import com.higgs.trust.rs.custom.api.enums.RequestEnum;
import com.higgs.trust.rs.custom.api.enums.RequestStatusEnum;
import com.higgs.trust.rs.core.dao.RequestDao;
import com.higgs.trust.rs.core.dao.po.RequestPO;
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
 * @author WangQuanzhou
 * @desc register RS callback handler
 * @date 2018/5/18 14:45
 */
@Service @Slf4j public class RegisterPolicyCallbackHandler {
    @Autowired private TransactionTemplate txRequired;
    @Autowired private RequestDao requestDao;

    public void process(RespData<CoreTransaction> respData) {

        if (null == respData || null == respData.getData()) {
            log.error("[process] register policy callback error, respData or respData.getData() is null");
            throw new CustomException(CustomExceptionCodeEnum.RegisterPolicyCallbackProcessException,
                "[process] register policy callback error, respData or respData.getData() is null");
        }

        try {
            String reqNo = respData.getData().getTxId();

            RequestPO requestPO = new RequestPO();
            requestPO.setRequestId(reqNo);

            if (respData.isSuccess()) {
                requestPO.setStatus(RequestStatusEnum.SUCCESS.getCode());
            } else {
                requestPO.setStatus(RequestStatusEnum.FAILED.getCode());
            }

            // 开启事务执行DB操作
            txRequired.execute(new TransactionCallbackWithoutResult() {
                @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
                    log.info("[process] transaction start，reqNo={}", reqNo);
                    requestDao.updateStatusByRequestId(requestPO.getRequestId(), RequestEnum.PROCESS.getCode(),
                        requestPO.getStatus(), respData.getRespCode(), respData.getMsg());
                    log.info("[process] transaction success，reqNo={}", reqNo);
                }
            });
        } catch (Throwable e) {
            log.error("[process] register policy error", e);
            throw new CustomException(CustomExceptionCodeEnum.RegisterPolicyCallbackProcessException,
                "[process] register policy error");
        }
    }
}
