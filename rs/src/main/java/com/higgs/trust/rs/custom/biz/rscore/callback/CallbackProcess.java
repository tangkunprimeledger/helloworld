package com.higgs.trust.rs.custom.biz.rscore.callback;

import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.BizTypeService;
import com.higgs.trust.rs.core.api.TxCallbackRegistor;
import com.higgs.trust.rs.core.callback.TxCallbackHandler;
import com.higgs.trust.rs.core.vo.VotingRequest;
import com.higgs.trust.rs.custom.biz.rscore.callback.handler.CreateBillCallbackHandler;
import com.higgs.trust.rs.custom.biz.rscore.callback.handler.StorageIdentityCallbackHandler;
import com.higgs.trust.rs.custom.biz.rscore.callback.handler.TransferBillCallbackHandler;
import com.higgs.trust.rs.custom.model.BizTypeConst;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    @Autowired private TxCallbackRegistor txCallbackRegistor;
    @Autowired private StorageIdentityCallbackHandler storageIdentityCallbackHandler;
    @Autowired private CreateBillCallbackHandler createBillCallbackHandler;
    @Autowired private TransferBillCallbackHandler transferBillCallbackHandler;
    @Autowired private BizTypeService bizTypeService;

    @Override public void afterPropertiesSet() throws Exception {
//        txCallbackRegistor.registCallback(this);
    }

    @Override public void onVote(VotingRequest votingRequest) {

    }

    /**
     * on slave persisted phase,only current node persisted
     *
     * @param respData
     */
    @Override public void onPersisted(RespData<CoreTransaction> respData,BlockHeader blockHeader) {

    }

    /**
     * on slave end phase,cluster node persisted
     * only after cluster node persisted, then identity data can be stored into identity table
     *
     * @param respData
     */
    @Override public void onEnd(RespData<CoreTransaction> respData,BlockHeader blockHeader) {
        log.debug("[onEnd] start process");
        CoreTransaction coreTransaction = respData.getData();
        String policyId = coreTransaction.getPolicyId();

        //process default
        InitPolicyEnum initPolicyEnum = InitPolicyEnum.getInitPolicyEnumByPolicyId(policyId);
        if(initPolicyEnum!=null) {
            switch (initPolicyEnum) {
                default:
                    break;
            }
            return;
        }
        //process custom
        String bizType = bizTypeService.getByPolicyId(policyId);
        if(StringUtils.isEmpty(bizType)){
            log.error("[onEnd] get bizType is null,policyId:{}",policyId);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_CONFIGURATION_ERROR);
        }
        switch (bizType) {
            case BizTypeConst.STORAGE_IDENTITY:
                storageIdentityCallbackHandler.process(respData);
                break;
            case BizTypeConst.TRANSFER_UTXO:
                transferBillCallbackHandler.process(respData);
                break;
            case BizTypeConst.ISSUE_UTXO:
                createBillCallbackHandler.process(respData);
                break;
            default:
                log.error("[onEnd] do not has bizType:{} handler",bizType);
                throw new RsCoreException(RsCoreErrorEnum.RS_CORE_CONFIGURATION_ERROR);
        }
        log.info("[onEnd] end process");
    }

    @Override public void onFailover(RespData<CoreTransaction> respData,BlockHeader blockHeader) {

    }

}
