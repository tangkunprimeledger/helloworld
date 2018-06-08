package com.higgs.trust.rs.custom.biz.rscore.callback;

import com.higgs.trust.rs.common.TxCallbackHandler;
import com.higgs.trust.rs.common.enums.BizTypeEnum;
import com.higgs.trust.rs.core.api.TxCallbackRegistor;
import com.higgs.trust.rs.core.vo.VotingRequest;
import com.higgs.trust.rs.custom.biz.rscore.callback.handler.*;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
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

    @Autowired private TxCallbackRegistor txCallbackRegistor;
    @Autowired private StorageIdentityCallbackHandler storageIdentityCallbackHandler;
    @Autowired private CreateBillCallbackHandler createBillCallbackHandler;
    @Autowired private TransferBillCallbackHandler transferBillCallbackHandler;
    @Autowired private RegisterPolicyCallbackHandler registerPolicyCallbackHandler;
    @Autowired private RegisterRsCallbackHandler registerRsCallbackHandler;

    @Override public void afterPropertiesSet() throws Exception {
        txCallbackRegistor.registCallback(this);
    }

    @Override public void onVote(VotingRequest votingRequest) {

    }

    /**
     * on slave persisted phase,only current node persisted
     *
     * @param respData
     */
    @Override public void onPersisted(RespData<CoreTransaction> respData) {

    }

    /**
     * on slave end phase,cluster node persisted
     * only after cluster node persisted, then identity data can be stored into identity table
     *
     * @param respData
     */
    @Override public void onEnd(RespData<CoreTransaction> respData) {
        log.info("[onEnd] start process");
        CoreTransaction coreTransaction = respData.getData();
        String policyId = coreTransaction.getPolicyId();
//            case STORAGE:
//                storageIdentityCallbackHandler.process(respData);
//                break;
//            case ISSUE_UTXO:
//                createBillCallbackHandler.process(respData);
//                break;
//            case TRANSFER_UTXO:
//                transferBillCallbackHandler.process(respData);
//                break;
//            case REGISTER_RS:
//                registerRsCallbackHandler.process(respData);
//                break;
//            case REGISTER_POLICY:
//                registerPolicyCallbackHandler.process(respData);
//                break;
//            case NOP:
//                break;
//            default:
//                break;
//        }
        log.info("[onEnd] end process");
    }

}
