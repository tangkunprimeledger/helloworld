package com.higgs.trust.rs.core.service;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.common.dao.RocksUtils;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.common.enums.RespCodeEnum;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.common.utils.CoreTransactionConvertor;
import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.api.RsManageService;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import com.higgs.trust.rs.core.repository.CoreTxRepository;
import com.higgs.trust.rs.core.vo.manage.CancelRsVO;
import com.higgs.trust.rs.core.vo.manage.RegisterPolicyVO;
import com.higgs.trust.rs.core.vo.manage.RegisterRsVO;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.manage.DecisionTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.core.repository.PolicyRepository;
import com.higgs.trust.slave.core.repository.RsNodeRepository;
import com.higgs.trust.slave.core.repository.ca.CaRepository;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.ca.Ca;
import com.higgs.trust.slave.model.bo.manage.CancelRS;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import com.higgs.trust.slave.model.bo.manage.RsNode;
import com.higgs.trust.slave.model.enums.biz.RsNodeStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tangfashuang
 * @date 2018/05/18 11:31
 * @desc rs manage service
 */
@Service @Slf4j public class RsManageServiceImpl implements RsManageService {

    @Autowired private TransactionTemplate txRequired;

    @Autowired private CoreTransactionService coreTransactionService;

    @Autowired private CoreTransactionConvertor coreTransactionConvertor;

    @Autowired private CoreTxRepository coreTxRepository;

    @Autowired private NodeState nodeState;

    @Autowired private RsNodeRepository rsNodeRepository;

    @Autowired private PolicyRepository policyRepository;

    @Autowired private CaRepository caRepository;

    @Autowired private RsConfig rsConfig;

    @Override public RespData registerRs(RegisterRsVO registerRsVO) {
        RespData respData;
        try {
            //request idempotent check
            String reqId = registerRsVO.getRequestId();
            respData = checkIdempotent(reqId);
            if (null != respData) {
                return respData;
            }

            //校验rsId是否可注册
            respData = checkRsForRegister(registerRsVO.getRsId());
            if (null != respData) {
                return respData;
            }

            if (rsConfig.isUseMySQL()) {
                //开启事务
                respData = txRequired.execute(new TransactionCallback<RespData>() {
                    @Override public RespData doInTransaction(TransactionStatus txStatus) {
                        //组装CoreTransaction，下发
                        return submitTx(coreTransactionConvertor
                            .buildCoreTransaction(reqId, null, buildRegisterRsActionList(registerRsVO), InitPolicyEnum.REGISTER_RS.getPolicyId()));
                    }
                });
            } else {
                try {
                    ThreadLocalUtils.putWriteBatch(new WriteBatch());
                    respData =  submitTx(coreTransactionConvertor
                        .buildCoreTransaction(reqId, null, buildRegisterRsActionList(registerRsVO), InitPolicyEnum.REGISTER_RS.getPolicyId()));
                    RocksUtils.batchCommit(new WriteOptions(), ThreadLocalUtils.getWriteBatch());
                } finally {
                    ThreadLocalUtils.clearWriteBatch();
                }
            }

            if (null == respData) {
                log.error("register rs error, respData is null");
                return new RespData(RespCodeEnum.SYS_FAIL.getRespCode(), RespCodeEnum.SYS_FAIL.getMsg());
            }
        } catch (Throwable e) {
            log.error("register rs error", e);
            respData = new RespData(RespCodeEnum.SYS_FAIL.getRespCode(), RespCodeEnum.SYS_FAIL.getMsg());
        }
        return respData;
    }

    private RespData checkRsForRegister(String rsId) {
        //判断rsId与nodeName是否一致
        if (!StringUtils.equals(rsId, nodeState.getNodeName())) {
            log.error("rsId:{} is not equals nodeName:{}", rsId, nodeState.getNodeName());
            return new RespData(RespCodeEnum.RS_ID_NOT_MATCH_NODE_NAME.getRespCode(),
                RespCodeEnum.RS_ID_NOT_MATCH_NODE_NAME.getMsg());
        }

        //校验CA是否存在且有效
        Ca ca = caRepository.getCaForBiz(rsId);
        if (null == ca || !ca.isValid()) {
            log.error("Ca is null or ca is not valid, rsId={}", rsId);
            return new RespData(RespCodeEnum.CA_IS_NOT_EXIST_OR_IS_NOT_VALID.getRespCode(),
                RespCodeEnum.CA_IS_NOT_EXIST_OR_IS_NOT_VALID.getMsg());
        }

        //校验rs节点是否已经注册
        RsNode rsNode = rsNodeRepository.queryByRsId(rsId);
        if (null != rsNode && RsNodeStatusEnum.COMMON == rsNode.getStatus()) {
            log.warn("rsNode already exist, rsId={}", rsId);
            return new RespData(RespCodeEnum.RS_NODE_ALREADY_EXIST.getRespCode(),
                RespCodeEnum.RS_NODE_ALREADY_EXIST.getMsg());
        }
        return null;
    }

    private List<Action> buildRegisterRsActionList(RegisterRsVO registerRsVO) {
        List<Action> actions = new ArrayList<>();
        RegisterRS registerRS = new RegisterRS();
        registerRS.setRsId(registerRsVO.getRsId());
        registerRS.setDesc(registerRsVO.getDesc());
        registerRS.setType(ActionTypeEnum.REGISTER_RS);
        registerRS.setIndex(0);
        actions.add(registerRS);
        return actions;
    }

    @Override public RespData registerPolicy(RegisterPolicyVO registerPolicyVO) {
        RespData respData;
        try {
            //request idempotent check
            String reqId = registerPolicyVO.getRequestId();
            respData = checkIdempotent(reqId);
            if (null != respData) {
                return respData;
            }

            //校验是否可注册policy
            respData = checkPolicy(registerPolicyVO);
            if (null != respData) {
                return respData;
            }

            if (rsConfig.isUseMySQL()) {
                //开启事务
                respData = txRequired.execute(new TransactionCallback<RespData>() {
                    @Override public RespData doInTransaction(TransactionStatus txStatus) {
                        //组装CoreTransaction，下发
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("votePattern", registerPolicyVO.getVotePattern());
                        jsonObject.put("callbackType", registerPolicyVO.getCallbackType());
                        return submitTx(coreTransactionConvertor
                            .buildCoreTransaction(reqId, jsonObject, buildPolicyActionList(registerPolicyVO),
                                InitPolicyEnum.REGISTER_POLICY.getPolicyId()));
                    }
                });
            } else {
                try {
                    ThreadLocalUtils.putWriteBatch(new WriteBatch());

                    //组装CoreTransaction，下发
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("votePattern", registerPolicyVO.getVotePattern());
                    jsonObject.put("callbackType", registerPolicyVO.getCallbackType());
                    respData = submitTx(coreTransactionConvertor
                        .buildCoreTransaction(reqId, jsonObject, buildPolicyActionList(registerPolicyVO),
                            InitPolicyEnum.REGISTER_POLICY.getPolicyId()));

                    RocksUtils.batchCommit(new WriteOptions(), ThreadLocalUtils.getWriteBatch());
                } finally {
                    ThreadLocalUtils.clearWriteBatch();
                }
            }

            if (null == respData) {
                log.error("register policy error, respData is null");
                return new RespData(RespCodeEnum.SYS_FAIL.getRespCode(), RespCodeEnum.SYS_FAIL.getMsg());
            }
        } catch (Throwable e) {
            log.error("register policy error", e);
            respData = new RespData<>(RespCodeEnum.SYS_FAIL.getRespCode(), RespCodeEnum.SYS_FAIL.getMsg());
        }
        return respData;
    }

    @Override public RespData cancelRs(CancelRsVO cancelRsVO) {
        RespData respData;
        try {

            //request idempotent check
            String reqId = cancelRsVO.getRequestId();
            respData = checkIdempotent(reqId);
            if (null != respData) {
                return respData;
            }

            //校验rsId是否可注销
            respData = checkRsForCancel(cancelRsVO.getRsId());
            if (null != respData) {
                return respData;
            }

            if (rsConfig.isUseMySQL()) {
                //开启事务
                respData = txRequired.execute(new TransactionCallback<RespData>() {
                    @Override public RespData doInTransaction(TransactionStatus txStatus) {
                        //组装CoreTransaction，下发
                        return submitTx(coreTransactionConvertor
                            .buildCoreTransaction(reqId, null, buildCancelRsActionList(cancelRsVO), InitPolicyEnum.CANCEL_RS.getPolicyId()));
                    }
                });
            } else {
                try {
                    ThreadLocalUtils.putWriteBatch(new WriteBatch());

                    //组装CoreTransaction，下发
                    respData = submitTx(coreTransactionConvertor
                        .buildCoreTransaction(reqId, null, buildCancelRsActionList(cancelRsVO), InitPolicyEnum.CANCEL_RS.getPolicyId()));

                    RocksUtils.batchCommit(new WriteOptions(), ThreadLocalUtils.getWriteBatch());
                } finally {
                    ThreadLocalUtils.clearWriteBatch();
                }
            }

            if (null == respData) {
                log.error("register rs error, respData is null");
                return new RespData(RespCodeEnum.SYS_FAIL.getRespCode(), RespCodeEnum.SYS_FAIL.getMsg());
            }
        } catch (Throwable e) {
            log.error("register rs error", e);
            respData = new RespData(RespCodeEnum.SYS_FAIL.getRespCode(), RespCodeEnum.SYS_FAIL.getMsg());
        }
        return respData;
    }

    private RespData checkRsForCancel(String rsId) {
        //判断rsId与nodeName是否一致
        if (!StringUtils.equals(rsId, nodeState.getNodeName())) {
            log.error("rsId:{} is not equals nodeName:{}", rsId, nodeState.getNodeName());
            return new RespData(RespCodeEnum.RS_ID_NOT_MATCH_NODE_NAME.getRespCode(),
                RespCodeEnum.RS_ID_NOT_MATCH_NODE_NAME.getMsg());
        }

        //校验CA是否存在且有效
        Ca ca = caRepository.getCaForBiz(rsId);
        if (null == ca || !ca.isValid()) {
            log.error("Ca is null or ca is not valid, rsId={}", rsId);
            return new RespData(RespCodeEnum.CA_IS_NOT_EXIST_OR_IS_NOT_VALID.getRespCode(),
                RespCodeEnum.CA_IS_NOT_EXIST_OR_IS_NOT_VALID.getMsg());
        }

        //校验rs节点是否存在且状态是否已注销
        RsNode rsNode = rsNodeRepository.queryByRsId(rsId);
        if (null == rsNode || RsNodeStatusEnum.CANCELED == rsNode.getStatus()) {
            return new RespData(RespCodeEnum.RS_NODE_NOT_EXIST_OR_RS_NODE_ALREADY_CANCELED.getRespCode(),
                RespCodeEnum.RS_NODE_NOT_EXIST_OR_RS_NODE_ALREADY_CANCELED.getMsg());
        }
        return null;
    }

    private List<Action> buildCancelRsActionList(CancelRsVO cancelRsVO) {
        List<Action> actions = new ArrayList<>();

        CancelRS cancelRs = new CancelRS();
        cancelRs.setRsId(cancelRsVO.getRsId());
        cancelRs.setIndex(0);
        cancelRs.setType(ActionTypeEnum.RS_CANCEL);

        actions.add(cancelRs);
        return actions;
    }

    private RespData checkPolicy(RegisterPolicyVO registerPolicyVO) {
        //校验policy发起方是否包含在rsIds中
        if (!registerPolicyVO.getRsIds().contains(nodeState.getNodeName())) {
            return new RespData(RespCodeEnum.POLICY_RS_IDS_MUST_HAVE_SENDER.getRespCode(),
                RespCodeEnum.POLICY_RS_IDS_MUST_HAVE_SENDER.getMsg());
        }

        //校验policyId是否已经存在
        if (null != policyRepository.getPolicyById(registerPolicyVO.getPolicyId())) {
            return new RespData(RespCodeEnum.POLICY_ALREADY_EXIST.getRespCode(),
                RespCodeEnum.POLICY_ALREADY_EXIST.getMsg());
        }
        return null;
    }

    private List<Action> buildPolicyActionList(RegisterPolicyVO registerPolicyVO) {
        List<Action> actions = new ArrayList<>();

        RegisterPolicy registerPolicy = new RegisterPolicy();
        registerPolicy.setPolicyId(registerPolicyVO.getPolicyId());
        registerPolicy.setPolicyName(registerPolicyVO.getPolicyName());
        registerPolicy.setDecisionType(DecisionTypeEnum.getBycode(registerPolicyVO.getDecisionType()));
        registerPolicy.setRsIds(registerPolicyVO.getRsIds());
        registerPolicy.setType(ActionTypeEnum.REGISTER_POLICY);
        registerPolicy.setIndex(0);
        actions.add(registerPolicy);
        return actions;
    }

    /**
     * 发送交易到rs-core
     */
    private RespData<?> submitTx(CoreTransaction coreTransaction) {
        //send and get callback result
        try {
            coreTransactionService.submitTx(coreTransaction);
        } catch (RsCoreException e) {
            if (e.getCode() == RsCoreErrorEnum.RS_CORE_IDEMPOTENT) {
                return checkIdempotent(coreTransaction.getTxId());
            }
        }
        return new RespData<>();
    }

    /**
     *
     * @param txId
     * @return
     */
    private RespData checkIdempotent(String txId) {
        CoreTransactionPO po = coreTxRepository.queryByTxId(txId, false);
        if (null != po) {
            if (null == po.getBlockHeight()) {
                return new RespData(RespCodeEnum.REQUEST_DUPLICATE.getRespCode(), RespCodeEnum.REQUEST_DUPLICATE.getMsg());
            }
            return new RespData<>(po.getErrorCode(), po.getErrorMsg());
        }
        return null;
    }
}
