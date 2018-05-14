package com.higgs.trust.rs.custom.biz.rscore.callback;

import com.higgs.trust.rs.common.TxCallbackHandler;
import com.higgs.trust.rs.common.enums.BizTypeEnum;
import com.higgs.trust.rs.core.api.TxCallbackRegistor;
import com.higgs.trust.rs.custom.biz.rscore.callback.handler.CreateBillCallbackHandler;
import com.higgs.trust.rs.custom.biz.rscore.callback.handler.StorageIdentityCallbackHandler;
import com.higgs.trust.rs.custom.biz.rscore.callback.handler.TransferBillCallbackHandler;
import com.higgs.trust.rs.custom.config.RsPropertiesConfig;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 存证回调处理
 *
 * @author wangquanzhou
 * @time 2018年3月16日15:14:51
 */
@Slf4j @Service public class CallbackProcess implements TxCallbackHandler, InitializingBean {

    @Autowired private RsPropertiesConfig propertiesConfig;
    @Autowired private TxCallbackRegistor txCallbackRegistor;
    @Autowired private StorageIdentityCallbackHandler storageIdentityCallbackHandler;
    @Autowired private CreateBillCallbackHandler createBillCallbackHandler;
    @Autowired private TransferBillCallbackHandler transferBillCallbackHandler;

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
                storageIdentityCallbackHandler.process(respData);
                break;
            case ISSUE_UTXO:
                createBillCallbackHandler.process(respData);
                break;
            case TRANSFER_UTXO:
                transferBillCallbackHandler.process(respData);
                break;
            case NOP:
                break;
            default:
                break;
        }
        log.info("[onEnd] end process");
    }

}
