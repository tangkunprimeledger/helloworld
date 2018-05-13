package com.higgs.trust.rs.custom.biz.rscore.callback;

import com.higgs.trust.rs.common.TxCallbackHandler;
import com.higgs.trust.rs.common.enums.BizTypeEnum;
import com.higgs.trust.rs.core.api.TxCallbackRegistor;
import com.higgs.trust.rs.custom.api.enums.BankChainExceptionCodeEnum;
import com.higgs.trust.rs.custom.config.RsPropertiesConfig;
import com.higgs.trust.rs.custom.dao.BankChainRequestDAO;
import com.higgs.trust.rs.custom.dao.identity.IdentityDAO;
import com.higgs.trust.rs.custom.dao.po.BankChainRequestPO;
import com.higgs.trust.rs.custom.dao.po.identity.IdentityPO;
import com.higgs.trust.rs.custom.exceptions.BankChainException;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 存证回调处理
 *
 * @author wangquanzhou
 * @time 2018年3月16日15:14:51
 */
@Slf4j @Service public class StorageIdentityCallbackProcess implements TxCallbackHandler,InitializingBean {

    @Autowired private RsPropertiesConfig propertiesConfig;
    @Autowired private TransactionTemplate txRequired;
    @Autowired private BankChainRequestDAO bankChainRequestDAO;
    @Autowired private IdentityDAO identityDAO;
    @Autowired private TxCallbackRegistor txCallbackRegistor;

    @Override public void afterPropertiesSet() throws Exception {
        txCallbackRegistor.registCallback(this);
    }

    /**
     * on slave persisted phase,only current node persisted
     *
     * @param bizTypeEnum
     * @param respData
     */
    @Override public void onPersisted(BizTypeEnum bizTypeEnum, RespData<CoreTransaction> respData) {

    }

    /**
     * on slave end phase,cluster node persisted
     * only after cluster node persisted, then identity data can be stored into identity table
     *
     * @param bizTypeEnum
     * @param respData
     */
    @Override public void onEnd(BizTypeEnum bizTypeEnum, RespData<CoreTransaction> respData) {
        log.info("[onEnd] start process");
        switch (bizTypeEnum) {
            case STORAGE:
                store(respData);
                break;
            default:
                break;
        }
        log.info("[onEnd] end process");
    }

    private void store(RespData<CoreTransaction> respData) {
        try {
            String reqNo = respData.getData().getTxId();
            String key = respData.getData().getBizModel().getString("key");
            String value = respData.getData().getBizModel().getString("value");

            BankChainRequestPO bankChainRequestPO = new BankChainRequestPO();
            bankChainRequestPO.setReqNo(reqNo);

            IdentityPO identityPO = new IdentityPO();
            identityPO.setReqNo(reqNo);
            identityPO.setKey(key);
            identityPO.setValue(value);

            // 开启事务执行DB操作
            txRequired.execute(new TransactionCallbackWithoutResult() {
                @Override protected void doInTransactionWithoutResult(TransactionStatus status) {
                    log.info("[store] transaction start，reqNo={}", reqNo);
                    // 更新bankchain_request表的对应的请求状态为SUCCESS
                    bankChainRequestDAO.updateRequest(bankChainRequestPO);

                    // 将回调的数据存入identity表
                    identityDAO.insertIdentity(identityPO);
                    log.info("[store] transaction success，reqNo={}", reqNo);
                }
            });
        } catch (Throwable e) {
            log.error("[store] store identity data error", e);
            throw new BankChainException(BankChainExceptionCodeEnum.IdentityCallbackProcessException, "[store] store identity data error");
        }
    }
}
