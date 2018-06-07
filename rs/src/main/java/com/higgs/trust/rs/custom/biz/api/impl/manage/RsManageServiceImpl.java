package com.higgs.trust.rs.custom.biz.api.impl.manage;

import com.higgs.trust.rs.common.enums.BizTypeEnum;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.custom.api.enums.RespCodeEnum;
import com.higgs.trust.rs.custom.api.manage.RsManageService;
import com.higgs.trust.rs.custom.api.vo.manage.RegisterPolicyVO;
import com.higgs.trust.rs.custom.api.vo.manage.RegisterRsVO;
import com.higgs.trust.rs.custom.biz.api.impl.RequestHelper;
import com.higgs.trust.rs.custom.util.converter.CoreTransactionConvertor;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

import static com.higgs.trust.rs.common.enums.BizTypeEnum.REGISTER_POLICY;
import static com.higgs.trust.rs.common.enums.BizTypeEnum.REGISTER_RS;

/**
 * @author tangfashuang
 * @date 2018/05/18 11:31
 * @desc rs manage service
 */
@Service
@Slf4j
public class RsManageServiceImpl implements RsManageService{

    @Autowired
    private TransactionTemplate txRequired;

    @Autowired
    private CoreTransactionService coreTransactionService;

    @Autowired
    private CoreTransactionConvertor coreTransactionConvertor;

    @Autowired
    private RequestHelper requestHelper;

    @Override public RespData registerRs(RegisterRsVO registerRsVO) {
        RespData respData;
        try {
            //开启事务
            respData = txRequired.execute(new TransactionCallback<RespData>() {
                @Override public RespData doInTransaction(TransactionStatus txStatus) {
                    //初步幂等校验
                    RespData respData = requestHelper.requestIdempotent(registerRsVO.getRequestId());
                    if (null != respData) {
                        return respData;
                    }
                    //请求入库
                    respData = requestHelper.insertRequest(registerRsVO.getRequestId(), registerRsVO);
                    if (null != respData) {
                        return respData;
                    }
                    //todo check slave rsId idempotent

                    //组装UTXO,CoreTransaction，下发
                    return submitTx(REGISTER_RS,
                        coreTransactionConvertor.buildCoreTransaction(registerRsVO.getRequestId(),
                            null,
                            buildRsActionList(registerRsVO),
                            InitPolicyEnum.REGISTER_RS.getPolicyId()));
                }
            });

            if (null == respData) {
                log.error("register rs error, respData is null");
                return new RespData(RespCodeEnum.SYS_FAIL.getRespCode(), RespCodeEnum.SYS_FAIL.getMsg());
            }

            if(!respData.isSuccess()){
                return respData;
            }
            respData = coreTransactionService.syncWait(registerRsVO.getRequestId(), true);
        } catch (Throwable e) {
            log.error("register rs error", e);
            respData = new RespData(RespCodeEnum.SYS_FAIL.getRespCode(), RespCodeEnum.SYS_FAIL.getMsg());
        }
        return respData;
    }
    private List<Action> buildRsActionList(RegisterRsVO registerRsVO) {
        List<Action> actions = new ArrayList<>();
        RegisterRS registerRS = new RegisterRS();
        registerRS.setRsId(registerRsVO.getRsId());
        registerRS.setPubKey(registerRsVO.getPubKey());
        registerRS.setDesc(registerRsVO.getDesc());
        registerRS.setType(ActionTypeEnum.REGISTER_RS);
        registerRS.setIndex(0);
        actions.add(registerRS);
        return actions;
    }

    @Override public RespData registerPolicy(RegisterPolicyVO registerPolicyVO) {
        RespData respData;
        try {
            //开启事务
            respData = txRequired.execute(new TransactionCallback<RespData>() {
                @Override public RespData doInTransaction(TransactionStatus txStatus) {
                    //初步幂等校验
                    RespData respData = requestHelper.requestIdempotent(registerPolicyVO.getRequestId());
                    if (null != respData) {
                        return respData;
                    }
                    //请求入库
                    respData = requestHelper.insertRequest(registerPolicyVO.getRequestId(), registerPolicyVO);
                    if (null != respData) {
                        return respData;
                    }
                    //todo check slave rsId idempotent

                    //组装UTXO,CoreTransaction，下发
                    return submitTx(REGISTER_POLICY,
                        coreTransactionConvertor.buildCoreTransaction(registerPolicyVO.getRequestId(),
                            null,
                            buildPolicyActionList(registerPolicyVO),
                            InitPolicyEnum.REGISTER_POLICY.getPolicyId()));
                }
            });

            if (null == respData) {
                log.error("register policy error, respData is null");
                return new RespData(RespCodeEnum.SYS_FAIL.getRespCode(), RespCodeEnum.SYS_FAIL.getMsg());
            }

            if(!respData.isSuccess()){
                return respData;
            }
            respData = coreTransactionService.syncWait(registerPolicyVO.getRequestId(), true);
        } catch (Throwable e) {
            log.error("register policy error", e);
            respData = new RespData<>(RespCodeEnum.SYS_FAIL.getRespCode(),
                RespCodeEnum.SYS_FAIL.getMsg());
        }
        return respData;
    }

    private List<Action> buildPolicyActionList(RegisterPolicyVO registerPolicyVO) {
        List<Action> actions = new ArrayList<>();

        RegisterPolicy registerPolicy = new RegisterPolicy();
        registerPolicy.setPolicyId(registerPolicyVO.getPolicyId());
        registerPolicy.setPolicyName(registerPolicyVO.getPolicyName());
        registerPolicy.setRsIds(registerPolicyVO.getRsIds());
        registerPolicy.setType(ActionTypeEnum.REGISTER_POLICY);
        registerPolicy.setIndex(0);
        actions.add(registerPolicy);
        return actions;
    }

    /**
     * 发送交易到rs-core
     */
    private RespData<?> submitTx (BizTypeEnum bizType,CoreTransaction coreTransaction){
        //send and get callback result
        try {
            coreTransactionService.submitTx(bizType, coreTransaction);
        } catch (RsCoreException e) {
            if (e.getCode() == RsCoreErrorEnum.RS_CORE_IDEMPOTENT) {
                return requestHelper.requestIdempotent(coreTransaction.getTxId());
            }
        }
        return new RespData<>();
    }
}
