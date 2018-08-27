package com.higgs.trust.rs.core.service;

import com.google.common.collect.Lists;
import com.higgs.trust.common.enums.MonitorTargetEnum;
import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.common.utils.MonitorLogUtils;
import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.BizTypeService;
import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.api.DistributeCallbackNotifyService;
import com.higgs.trust.rs.core.api.VoteService;
import com.higgs.trust.rs.core.api.enums.*;
import com.higgs.trust.rs.core.bo.CoreTxBO;
import com.higgs.trust.rs.core.bo.VoteReceipt;
import com.higgs.trust.rs.core.bo.VoteRule;
import com.higgs.trust.rs.core.callback.RsCoreBatchCallbackProcessor;
import com.higgs.trust.rs.core.callback.RsCoreCallbackProcessor;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import com.higgs.trust.rs.core.dao.po.CoreTransactionProcessPO;
import com.higgs.trust.rs.core.repository.CoreTxProcessRepository;
import com.higgs.trust.rs.core.repository.CoreTxRepository;
import com.higgs.trust.rs.core.repository.VoteReceiptRepository;
import com.higgs.trust.rs.core.repository.VoteRuleRepository;
import com.higgs.trust.rs.core.vo.RsCoreTxVO;
import com.higgs.trust.slave.api.BlockChainService;
import com.higgs.trust.slave.api.enums.RespCodeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.enums.manage.VotePatternEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.api.vo.TransactionVO;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidateResult;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import com.higgs.trust.slave.core.repository.PolicyRepository;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignInfo;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.manage.Policy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CoreTransactionServiceImpl implements CoreTransactionService, InitializingBean {
    @Value("${higgs.trust.joinConsensus:false}")
    private String joinConsensus;
    @Autowired
    private TransactionTemplate txRequired;
    @Autowired
    private RsConfig rsConfig;
    @Autowired
    private BizTypeService bizTypeService;
    @Autowired
    private CoreTxRepository coreTxRepository;
    @Autowired
    private CoreTxProcessRepository coreTxProcessRepository;
    @Autowired
    private VoteRuleRepository voteRuleRepository;
    @Autowired
    private VoteReceiptRepository voteReceiptRepository;
    @Autowired
    private VoteService voteService;
    @Autowired
    private PolicyRepository policyRepository;
    @Autowired
    private BlockChainService blockChainService;
    @Autowired
    private RsCoreCallbackProcessor rsCoreCallbackHandler;
    @Autowired
    private RsCoreBatchCallbackProcessor rsCoreBatchCallbackProcessor;
    @Autowired
    private SignServiceImpl signService;
    @Autowired
    private ThreadPoolTaskExecutor txSubmitExecutorPool;
    @Autowired
    private ThreadPoolTaskExecutor txProcessExecutorPool;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private DistributeCallbackNotifyService distributeCallbackNotifyService;

    /**
     * init redis distribution topic listener
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Init redis distribution topic listener to process tx.");
        //TODO lingchao open it ofter test
        if (!Boolean.valueOf(joinConsensus)) {
            log.info("this node not join consensus do not need to initAsyncProcessInitTxListener");
            return;
        }
        //init Async process init tx listener
        initAsyncProcessInitTxListener();
    }

    /**
     * init listener of async to process init tx
     */
    private void initAsyncProcessInitTxListener() {
        RTopic<String> topic = redissonClient.getTopic(RedisTopicEnum.ASYNC_TO_PROCESS_INIT_TX.getCode());
        topic.addListener((channel, msg) -> {
            processInitTx(msg);
        });
    }


    @Override
    public RespData syncWait(String txId, boolean forEnd) {
        RespData respData = null;
        try {
            //ON_PERSISTED_CALLBACK
            if (!forEnd) {
                respData = distributeCallbackNotifyService.syncWaitNotify(txId, RedisMegGroupEnum.ON_PERSISTED_CALLBACK_MESSAGE_NOTIFY, rsConfig.getSyncRequestTimeout(), TimeUnit.MILLISECONDS);
            } else {
                //ON_CLUSTER_PERSISTED_CALLBACK
                respData = distributeCallbackNotifyService.syncWaitNotify(txId, RedisMegGroupEnum.ON_CLUSTER_PERSISTED_CALLBACK_MESSAGE_NOTIFY, rsConfig.getSyncRequestTimeout(), TimeUnit.MILLISECONDS);
            }
        } catch (Throwable e) {
            log.error("tx handle exception. ", e);
            respData = new RespData();
            respData.setCode(RespCodeEnum.SYS_FAIL.getRespCode());
            respData.setMsg("handle transaction exception.");
        }

        if (null == respData) {
            respData = new RespData();
            respData.setCode(RespCodeEnum.SYS_HANDLE_TIMEOUT.getRespCode());
            respData.setMsg("tx handle timeout");
            MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.RS_WAIT_TIME_OUT_ERROR.getMonitorTarget(), 1);
        }

        return respData;
    }

    @Override
    public void submitTx(CoreTransaction coreTx) {
        log.info("[submitTx]{}", coreTx);
        if (coreTx == null) {
            log.error("[submitTx] the tx is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_PARAM_VALIDATE_ERROR);
        }
        //validate param
        BeanValidateResult validateResult = BeanValidator.validate(coreTx);
        if (!validateResult.isSuccess()) {
            log.error("[submitTx] param validate is fail,first msg:{}", validateResult.getFirstMsg());
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_PARAM_VALIDATE_ERROR);
        }
        //check bizType
        String policyId = coreTx.getPolicyId();
        InitPolicyEnum initPolicyEnum = InitPolicyEnum.getInitPolicyEnumByPolicyId(policyId);
        if (initPolicyEnum == null) {
            String bizType = bizTypeService.getByPolicyId(coreTx.getPolicyId());
            if (StringUtils.isEmpty(bizType)) {
                log.error("[submitTx] get bizType is null,policyId:{}", coreTx.getPolicyId());
                throw new RsCoreException(RsCoreErrorEnum.RS_CORE_CONFIGURATION_ERROR);
            }
        }
        //check idempotent by txId
        CoreTransactionPO po = coreTxRepository.queryByTxId(coreTx.getTxId(), false);
        if (po != null) {
            log.info("[submitTx]is idempotent txId:{}", coreTx.getTxId());
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_IDEMPOTENT);
        }
        //reset sendTime
        coreTx.setSendTime(new Date());
        //sign tx for self
        SignInfo signInfo = signService.signTx(coreTx);
        if (signInfo == null) {
            log.error("[submitTx] self sign data is empty");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_VERIFY_SIGNATURE_FAILED);
        }

        //save coreTxProcess to db
        coreTxProcessRepository.add(coreTx.getTxId(), CoreTxStatusEnum.INIT);
        //save coreTx to db
        coreTxRepository.add(coreTx, Lists.newArrayList(signInfo), 0L);
        //send redis msg for slave
        asyncProcessInitTx(coreTx.getTxId());
    }

    /**
     * publish msg to process initTx
     *
     * @param txId
     */
    private void asyncProcessInitTx(String txId) {
        try {
            // send topic
            RTopic<String> topic = redissonClient.getTopic(RedisTopicEnum.ASYNC_TO_PROCESS_INIT_TX.getCode());
            topic.publish(txId);
        } catch (Throwable e) {
            log.error("Publish msg to process initTx msg failed！");
        }

    }

    @Override
    public void processInitTx(String txId) {
        //check txId,the redis msg may be null
        if (StringUtils.isBlank(txId)) {
            return;
        }
        log.info("[processInitTx]txId:{}", txId);
        final CoreTxBO[] finalTx = {null};
        final VotePatternEnum[] finalVotePattern = {null};
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                CoreTransactionProcessPO coreTransactionProcessPO = coreTxProcessRepository.queryByTxId(txId, true);
                if (null == coreTransactionProcessPO || !StringUtils.equals(coreTransactionProcessPO.getStatus(), CoreTxStatusEnum.INIT.getCode())) {
                    log.info("[processInitTx]the coreTx is null or status is not INIT txId:{}", txId);
                    return;
                }
                CoreTransactionPO po = coreTxRepository.queryByTxId(txId, false);
                Date lockTime = po.getLockTime();
                if (lockTime != null && lockTime.after(new Date())) {
                    log.info("[processInitTx]should skip this tx by lock time:{}", lockTime);
                    return;
                }
                //convert bo
                CoreTxBO bo = coreTxRepository.convertTxBO(po);
                String policyId = bo.getPolicyId();
                log.info("[processInitTx]policyId:{}", policyId);
                Policy policy = policyRepository.getPolicyById(policyId);
                if (policy == null) {
                    log.error("[processInitTx]get policy is null by policyId:{}", policyId);
                    toEndOrCallBackByError(bo, CoreTxStatusEnum.INIT, RsCoreErrorEnum.RS_CORE_TX_POLICY_NOT_EXISTS_FAILED, true);
                    return;
                }
                // vote rule
                VoteRule voteRule = null;
                //from default
                InitPolicyEnum initPolicyEnum = InitPolicyEnum.getInitPolicyEnumByPolicyId(policyId);
                if (initPolicyEnum != null) {
                    voteRule = new VoteRule();
                    voteRule.setPolicyId(policyId);
                    voteRule.setVotePattern(initPolicyEnum.getVotePattern());
                    voteRule.setCallbackType(CallbackTypeEnum.ALL);
                } else {
                    //query vote rule
                    voteRule = voteRuleRepository.queryByPolicyId(policyId);
                }
                if (voteRule == null) {
                    log.error("[processInitTx]get voteRule is null by policyId:{}", policyId);
                    toEndOrCallBackByError(bo, CoreTxStatusEnum.INIT, RsCoreErrorEnum.RS_CORE_VOTE_RULE_NOT_EXISTS_ERROR, true);
                    return;
                }
                //check rs ids
                if (CollectionUtils.isEmpty(policy.getRsIds())) {
                    log.warn("[processInitTx]rs ids is empty");
                    //the blow system policy  needs rsIds
                    if (initPolicyEnum == InitPolicyEnum.CONTRACT_ISSUE || initPolicyEnum == InitPolicyEnum.CONTRACT_DESTROY || initPolicyEnum == InitPolicyEnum.UTXO_DESTROY || initPolicyEnum == InitPolicyEnum.UTXO_ISSUE) {
                        toEndOrCallBackByError(bo, CoreTxStatusEnum.INIT, RsCoreErrorEnum.RS_CORE_VOTE_VOTERS_IS_EMPTY_ERROR, true);
                        return;
                    }

                    //when not like before policy s ,still submit to slave
                    coreTxProcessRepository.updateStatus(bo.getTxId(), CoreTxStatusEnum.INIT, CoreTxStatusEnum.WAIT);
                    finalTx[0] = bo;
                    finalVotePattern[0] = initPolicyEnum.getVotePattern();
                    return;
                }
                //vote pattern
                VotePatternEnum votePattern = voteRule.getVotePattern();
                if (votePattern == null) {
                    log.error("[processInitTx]votePattern is empty");
                    toEndOrCallBackByError(bo, CoreTxStatusEnum.INIT, RsCoreErrorEnum.RS_CORE_VOTE_PATTERN_NOT_EXISTS_ERROR, true);
                    return;
                }
                //get need voters from saved sign info
                List<String> needVoters = voteService.getVoters(bo.getSignDatas(), policy.getRsIds());
                if (CollectionUtils.isEmpty(needVoters)) {
                    log.warn("[processInitTx]need voters is empty txId:{}", bo.getTxId());
                    //still submit to slave
                    coreTxProcessRepository.updateStatus(bo.getTxId(), CoreTxStatusEnum.INIT, CoreTxStatusEnum.WAIT);
                    finalTx[0] = bo;
                    finalVotePattern[0] = votePattern;
                    return;
                }
                //request voting
                List<VoteReceipt> receipts = voteService.requestVoting(bo, needVoters, votePattern);
                //if receipts is empty,should retry
                if (CollectionUtils.isEmpty(receipts)) {
                    log.error("[processInitTx]voting receipts is empty by SYNC txId:{}", bo.getTxId());
                    return;
                }
                //get sign info from receipts
                List<SignInfo> signInfos = voteService.getSignInfos(receipts);
                signInfos.addAll(bo.getSignDatas());
                //update signDatas
                coreTxRepository.updateSignDatas(bo.getTxId(), signInfos);
                //save already voting result for SYNC pattern
                if (votePattern == VotePatternEnum.SYNC) {
                    voteReceiptRepository.batchAdd(receipts);
                }
                //when there is failure as net-timeout,should retry
                if (receipts.size() < needVoters.size()) {
                    log.error("[processInitTx]receipts.size:{} is less than voters.size:{} txId:{}", receipts.size(), needVoters.size(), bo.getTxId());
                    return;
                }
                //check vote decision for SYNC pattern
                if (votePattern == VotePatternEnum.SYNC) {
                    //add them when have last receipts
                    List<VoteReceipt> lastReceipts = voteReceiptRepository.queryByTxId(bo.getTxId());
                    if (!CollectionUtils.isEmpty(lastReceipts)) {
                        receipts.addAll(lastReceipts);
                    }
                    //get decision result from receipts
                    boolean decision = voteService.getDecision(receipts, policy.getDecisionType());
                    log.info("[processInitTx]decision:{}", decision);
                    if (!decision) {
                        toEndOrCallBackByError(bo, CoreTxStatusEnum.INIT, RsCoreErrorEnum.RS_CORE_VOTE_DECISION_FAIL, true);
                        return;
                    }
                    //change status to WAIT for SYNC pattern
                    coreTxProcessRepository.updateStatus(bo.getTxId(), CoreTxStatusEnum.INIT, CoreTxStatusEnum.WAIT);
                } else {
                    //change status to NEED_VOTE for ASYNC pattern
                    coreTxProcessRepository.updateStatus(bo.getTxId(), CoreTxStatusEnum.INIT, CoreTxStatusEnum.NEED_VOTE);
                }
                if (!CollectionUtils.isEmpty(signInfos)) {
                    bo.setSignDatas(signInfos);
                }
                finalTx[0] = bo;
                finalVotePattern[0] = votePattern;
            }
        });
        VotePatternEnum votePattern = finalVotePattern[0];
        if (votePattern == VotePatternEnum.SYNC) {
            //submit by async
            txSubmitExecutorPool.execute(new Runnable() {
                @Override
                public void run() {
                    log.info("submitToSlave by signal");
                    submitToSlave(Lists.newArrayList(finalTx[0]));
                }
            });
        }
        log.info("[processInitTx]is success");
    }

    @Override
    public void processNeedVoteTx(String txId) {
        log.info("[processNeedVoteTx]txId:{}", txId);
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                CoreTransactionProcessPO coreTransactionProcessPO = coreTxProcessRepository.queryByTxId(txId, true);
                if (!StringUtils.equals(coreTransactionProcessPO.getStatus(), CoreTxStatusEnum.NEED_VOTE.getCode())) {
                    log.info("[processNeedVoteTx]the coreTx status is not NEED_VOTE txId:{}", txId);
                    return;
                }
                CoreTransactionPO po = coreTxRepository.queryByTxId(txId, false);
                Date lockTime = po.getLockTime();
                if (lockTime != null && lockTime.after(new Date())) {
                    log.info("[processNeedVoteTx]should skip this tx by lock time:{}", lockTime);
                    return;
                }
                //convert bo
                CoreTxBO bo = coreTxRepository.convertTxBO(po);
                String policyId = bo.getPolicyId();
                log.info("[processNeedVoteTx]policyId:{}", policyId);
                Policy policy = policyRepository.getPolicyById(policyId);
                if (policy == null) {
                    log.error("[processNeedVoteTx]get policy is null by policyId:{}", policyId);
                    toEndOrCallBackByError(bo, CoreTxStatusEnum.NEED_VOTE, RsCoreErrorEnum.RS_CORE_TX_POLICY_NOT_EXISTS_FAILED, true);
                    return;
                }
                List<String> rsIds = policy.getRsIds();
                if (CollectionUtils.isEmpty(rsIds)) {
                    log.error("[processNeedVoteTx]rsIds is empty by txId:{}", bo.getTxId());
                    toEndOrCallBackByError(bo, CoreTxStatusEnum.NEED_VOTE, RsCoreErrorEnum.RS_CORE_VOTE_VOTERS_IS_EMPTY_ERROR, true);
                    return;
                }
                //query receipts by txId
                List<VoteReceipt> receipts = voteReceiptRepository.queryByTxId(bo.getTxId());
                if (CollectionUtils.isEmpty(receipts)) {
                    log.warn("[processNeedVoteTx]receipts is empty by txId:{}", bo.getTxId());
                    return;
                }
                //filter self
                List<String> lastRsIds = new ArrayList<>();
                for (String rsName : rsIds) {
                    if (!StringUtils.equals(rsName, rsConfig.getRsName())) {
                        lastRsIds.add(rsName);
                    }
                }
                if (receipts.size() != lastRsIds.size()) {
                    log.warn("[processNeedVoteTx]receipts.size:{} less than rsIds.size:{} by txId:{}", receipts.size(), rsIds.size(), bo.getTxId());
                    return;
                }
                //get decision result
                boolean decision = voteService.getDecision(receipts, policy.getDecisionType());
                log.info("[processNeedVoteTx]decision:{}", decision);
                if (!decision) {
                    toEndOrCallBackByError(bo, CoreTxStatusEnum.NEED_VOTE, RsCoreErrorEnum.RS_CORE_VOTE_DECISION_FAIL, true);
                    return;
                }
                List<SignInfo> signInfos = voteService.getSignInfos(receipts);
                List<SignInfo> lastSigns = bo.getSignDatas();
                for (SignInfo signInfo : lastSigns) {
                    if (StringUtils.equals(rsConfig.getRsName(), signInfo.getOwner())) {
                        signInfos.add(signInfo);
                        break;
                    }
                }
                coreTxRepository.updateSignDatas(bo.getTxId(), signInfos);
                //change status to WAIT for SYNC pattern
                coreTxProcessRepository.updateStatus(bo.getTxId(), CoreTxStatusEnum.NEED_VOTE, CoreTxStatusEnum.WAIT);
                log.info("[processNeedVoteTx]is success");
            }
        });
    }

    /**
     * to tx END status for fail business and call back custom rs
     *
     * @param bo
     * @param from
     * @param rsCoreErrorEnum
     */
    private void toEndOrCallBackByError(CoreTxBO bo, CoreTxStatusEnum from, RsCoreErrorEnum rsCoreErrorEnum, boolean isCallback) {
        RespData respData = new RespData();
        respData.setCode(rsCoreErrorEnum.getCode());
        respData.setMsg(rsCoreErrorEnum.getDescription());
        toEndOrCallBackByError(bo, from, respData, isCallback);
    }

    /**
     * to tx END status for fail business and call back custom rs
     *
     * @param bo
     * @param from
     * @param respData
     */
    private void toEndOrCallBackByError(CoreTxBO bo, CoreTxStatusEnum from, RespData respData, boolean isCallback) {
        log.info("[toEndOrCallBackByError]tx:{},from:{},respData:{}", bo, from, respData);
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                //save execute result and error code
                String txId = bo.getTxId();
                coreTxRepository.saveExecuteResultAndHeight(txId, CoreTxResultEnum.FAIL, respData.getRespCode(), respData.getMsg(), 0L);
                //update status from 'from' to END
                coreTxProcessRepository.updateStatus(txId, from, CoreTxStatusEnum.END);
                respData.setData(coreTxRepository.convertTxVO(bo));
                //callback custom rs
                if (isCallback) {
                    if (!rsConfig.isBatchCallback()) {
                        rsCoreCallbackHandler.onEnd(respData, null);
                    } else {
                        //for batch interface
                        RsCoreTxVO vo = BeanConvertor.convertBean(bo, RsCoreTxVO.class);
                        vo.setStatus(CoreTxStatusEnum.END);
                        vo.setExecuteResult(CoreTxResultEnum.FAIL);
                        vo.setErrorCode(respData.getRespCode());
                        vo.setErrorMsg(respData.getMsg());
                        rsCoreBatchCallbackProcessor.onEnd(Lists.newArrayList(vo), null);
                    }
                }
            }
        });
        //同步通知
        try {
            distributeCallbackNotifyService.notifySyncResult(bo.getTxId(), respData, RedisMegGroupEnum.ON_PERSISTED_CALLBACK_MESSAGE_NOTIFY);
            distributeCallbackNotifyService.notifySyncResult(bo.getTxId(), respData, RedisMegGroupEnum.ON_CLUSTER_PERSISTED_CALLBACK_MESSAGE_NOTIFY);
        } catch (Throwable e) {
            log.warn("sync notify rs resp data failed", e);
        }
    }

    /**
     * submit slave by schedule
     */
    @Override
    public void submitToSlave() {
        //max size
        int maxSize = 200;
        List<CoreTransactionProcessPO> list = coreTxProcessRepository.queryByStatus(CoreTxStatusEnum.WAIT, 0, maxSize);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> txIdList = Lists.newArrayListWithCapacity(list.size());
        for (CoreTransactionProcessPO po : list) {
            txIdList.add(po.getTxId());
        }
        List<CoreTransactionPO> coreTransactionPOList = coreTxRepository.queryByTxIds(txIdList);
        List<CoreTxBO> boList = new ArrayList<>(list.size());
        for (CoreTransactionPO po : coreTransactionPOList) {
            boList.add(coreTxRepository.convertTxBO(po));
        }
        //submit
        submitToSlave(boList);
    }

    @Override
    public RsCoreTxVO queryCoreTx(String txId) {
        CoreTransactionPO coreTransactionPO = coreTxRepository.queryByTxId(txId, false);
        if (coreTransactionPO == null) {
            return null;
        }
        CoreTransactionProcessPO coreTransactionProcessPO = coreTxProcessRepository.queryByTxId(txId, false);
        CoreTxBO coreTxBO = coreTxRepository.convertTxBO(coreTransactionPO);
        RsCoreTxVO coreTxVO = BeanConvertor.convertBean(coreTxBO, RsCoreTxVO.class);
        coreTxVO.setStatus(CoreTxStatusEnum.formCode(null != coreTransactionProcessPO ? coreTransactionProcessPO.getStatus() : CoreTxStatusEnum.END.getCode()));
        coreTxVO.setExecuteResult(CoreTxResultEnum.formCode(coreTransactionPO.getExecuteResult()));
        coreTxVO.setErrorCode(coreTransactionPO.getErrorCode());
        coreTxVO.setErrorMsg(coreTransactionPO.getErrorMsg());
        return coreTxVO;
    }

    /**
     * submit slave
     *
     * @param boList
     */
    private void submitToSlave(List<CoreTxBO> boList) {
        List<SignedTransaction> txs = makeTxs(boList);
        try {
            log.debug("[submitToSlave] start");
            RespData<List<TransactionVO>> respData = blockChainService.submitTransactions(txs);
            if (respData.getData() == null) {
                log.debug("[submitToSlave] end");
                return;
            }
            //has fail tx
            List<TransactionVO> txsOfFail = respData.getData();
            if (log.isDebugEnabled()) {
                log.debug("[submitToSlave] tx result:{}", txsOfFail);
            }
            for (TransactionVO txVo : txsOfFail) {
                //dont need
                if (!txVo.getRetry()) {
                    CoreTransactionPO po = coreTxRepository.queryByTxId(txVo.getTxId(), false);
                    CoreTxBO bo = coreTxRepository.convertTxBO(po);
                    //end
                    RespData mRes = new RespData();
                    //set error code
                    mRes.setCode(txVo.getErrCode());
                    mRes.setMsg(txVo.getErrMsg());
                    try {
                        //require db-transaction and try self
                        toEndOrCallBackByError(bo, CoreTxStatusEnum.WAIT, mRes, true);
                    } catch (Throwable e) {
                        log.error("[submitToSlave.toEndOrCallBackByError] has error", e);
                    }
                }
            }
        } catch (SlaveException e) {
            log.error("[submitToSlave] has slave error", e);
            MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.RS_SUBMIT_TO_SLAVE_ERROR.getMonitorTarget(), 10);
        } catch (Throwable e) {
            log.error("[submitToSlave] has unknown error", e);
            MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.RS_SUBMIT_TO_SLAVE_ERROR.getMonitorTarget(), 10);
        }
    }

    /**
     * make txs from core transaction
     *
     * @param list
     * @return
     */
    private List<SignedTransaction> makeTxs(List<CoreTxBO> list) {
        List<SignedTransaction> txs = new ArrayList<>(list.size());
        for (CoreTxBO bo : list) {
            SignedTransaction tx = new SignedTransaction();
            CoreTransaction coreTx = coreTxRepository.convertTxVO(bo);
            tx.setCoreTx(coreTx);
            tx.setSignatureList(bo.getSignDatas());
            txs.add(tx);
        }
        return txs;
    }
}
