package com.higgs.trust.rs.core.service;

import com.google.common.collect.Lists;
import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.api.VoteService;
import com.higgs.trust.rs.core.api.enums.CallbackTypeEnum;
import com.higgs.trust.rs.core.api.enums.CoreTxResultEnum;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.bo.CoreTxBO;
import com.higgs.trust.rs.core.bo.VoteReceipt;
import com.higgs.trust.rs.core.bo.VoteRule;
import com.higgs.trust.rs.core.callback.RsCoreCallbackProcessor;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import com.higgs.trust.rs.core.repository.CoreTxRepository;
import com.higgs.trust.rs.core.repository.VoteReceiptRepository;
import com.higgs.trust.rs.core.repository.VoteRuleRepository;
import com.higgs.trust.slave.api.BlockChainService;
import com.higgs.trust.slave.api.enums.RespCodeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.enums.manage.VotePatternEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.api.vo.TransactionVO;
import com.higgs.trust.slave.common.util.asynctosync.HashBlockingMap;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service @Slf4j public class CoreTransactionServiceImpl implements CoreTransactionService {
    @Autowired private TransactionTemplate txRequired;
    @Autowired private RsConfig rsConfig;
    @Autowired private CoreTxRepository coreTxRepository;
    @Autowired private VoteRuleRepository voteRuleRepository;
    @Autowired private VoteReceiptRepository voteReceiptRepository;
    @Autowired private VoteService voteService;
    @Autowired private PolicyRepository policyRepository;
    @Autowired private BlockChainService blockChainService;
    @Autowired private RsCoreCallbackProcessor rsCoreCallbackHandler;
    @Autowired private SignServiceImpl signService;
    @Autowired private HashBlockingMap<RespData> persistedResultMap;
    @Autowired private HashBlockingMap<RespData> clusterPersistedResultMap;
    @Autowired private ThreadPoolTaskExecutor txSubmitExecutorPool;
    @Autowired private ThreadPoolTaskExecutor txProcessExecutorPool;

    @Override public RespData syncWait(String key, boolean forEnd) {
        RespData respData = null;
        try {
            if (forEnd) {
                respData = clusterPersistedResultMap.poll(key, rsConfig.getSyncRequestTimeout());
            } else {
                respData = persistedResultMap.poll(key, rsConfig.getSyncRequestTimeout());
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
        }

        return respData;
    }

    @Override public void submitTx(CoreTransaction coreTx) {
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
        //check by txId
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
        //save to db
        coreTxRepository.add(coreTx, Lists.newArrayList(signInfo), CoreTxStatusEnum.INIT);
        //process by async
        txProcessExecutorPool.execute(new Runnable() {
            @Override public void run() {
                processInitTx(coreTx.getTxId());
            }
        });
    }

    @Override public void processInitTx(String txId) {
        log.info("[processInitTx]txId:{}", txId);
        final CoreTxBO[] finalTx = {null};
        final VotePatternEnum[] finalVotePattern = {null};
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                CoreTransactionPO po = coreTxRepository.queryByTxId(txId, true);
                if (!StringUtils.equals(po.getStatus(), CoreTxStatusEnum.INIT.getCode())) {
                    log.info("[processInitTx]the coreTx status is not INIT txId:{}", txId);
                    return;
                }
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
                    toEndAndCallBackByError(bo, CoreTxStatusEnum.INIT,
                        RsCoreErrorEnum.RS_CORE_TX_POLICY_NOT_EXISTS_FAILED);
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
                    toEndAndCallBackByError(bo, CoreTxStatusEnum.INIT,
                        RsCoreErrorEnum.RS_CORE_VOTE_RULE_NOT_EXISTS_ERROR);
                    return;
                }
                //vote pattern
                VotePatternEnum votePattern = voteRule.getVotePattern();
                if (votePattern == null) {
                    log.error("[processInitTx]votePattern is empty");
                    toEndAndCallBackByError(bo, CoreTxStatusEnum.INIT,
                        RsCoreErrorEnum.RS_CORE_VOTE_PATTERN_NOT_EXISTS_ERROR);
                    return;
                }
                //get required voters from saved sign info
                List<String> voters = voteService.getVoters(bo.getSignDatas(), policy.getRsIds());
                if (CollectionUtils.isEmpty(voters)) {
                    log.warn("[processInitTx]required voters is empty txId:{}", bo.getTxId());
                    if (initPolicyEnum == InitPolicyEnum.REGISTER_POLICY || initPolicyEnum == InitPolicyEnum.REGISTER_RS
                        || initPolicyEnum == InitPolicyEnum.CA_AUTH || initPolicyEnum == InitPolicyEnum.CA_UPDATE
                        || initPolicyEnum == InitPolicyEnum.CA_CANCEL || initPolicyEnum == InitPolicyEnum.CANCEL_RS) {
                        //still submit to slave
                        coreTxRepository.updateStatus(bo.getTxId(), CoreTxStatusEnum.INIT, CoreTxStatusEnum.WAIT);
                        return;
                    }
                    toEndAndCallBackByError(bo, CoreTxStatusEnum.INIT,
                        RsCoreErrorEnum.RS_CORE_VOTE_VOTERS_IS_EMPTY_ERROR);
                    return;
                }
                //request voting
                List<VoteReceipt> receipts = voteService.requestVoting(bo, voters, votePattern);
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
                if (receipts.size() < voters.size()) {
                    log.error("[processInitTx]receipts.size:{} is less than voters.size:{} txId:{}", receipts.size(),
                        voters.size(), bo.getTxId());
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
                        toEndAndCallBackByError(bo, CoreTxStatusEnum.INIT, RsCoreErrorEnum.RS_CORE_VOTE_DECISION_FAIL);
                        return;
                    }
                    //change status to WAIT for SYNC pattern
                    coreTxRepository.updateStatus(bo.getTxId(), CoreTxStatusEnum.INIT, CoreTxStatusEnum.WAIT);
                } else {
                    //change status to NEED_VOTE for ASYNC pattern
                    coreTxRepository.updateStatus(bo.getTxId(), CoreTxStatusEnum.INIT, CoreTxStatusEnum.NEED_VOTE);
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
                @Override public void run() {
                    submitToSlave(Lists.newArrayList(finalTx[0]));
                }
            });
        }
        log.info("[processInitTx]is success");
    }

    @Override public void processNeedVoteTx(String txId) {
        log.info("[processNeedVoteTx]txId:{}", txId);
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                CoreTransactionPO po = coreTxRepository.queryByTxId(txId, true);
                if (!StringUtils.equals(po.getStatus(), CoreTxStatusEnum.NEED_VOTE.getCode())) {
                    log.info("[processNeedVoteTx]the coreTx status is not NEED_VOTE txId:{}", txId);
                    return;
                }
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
                    toEndAndCallBackByError(bo, CoreTxStatusEnum.NEED_VOTE,
                        RsCoreErrorEnum.RS_CORE_TX_POLICY_NOT_EXISTS_FAILED);
                    return;
                }
                List<String> rsIds = policy.getRsIds();
                if (CollectionUtils.isEmpty(rsIds)) {
                    log.error("[processNeedVoteTx]rsIds is empty by txId:{}", bo.getTxId());
                    toEndAndCallBackByError(bo, CoreTxStatusEnum.NEED_VOTE,
                        RsCoreErrorEnum.RS_CORE_VOTE_VOTERS_IS_EMPTY_ERROR);
                    return;
                }
                //query receipts by txId
                List<VoteReceipt> receipts = voteReceiptRepository.queryByTxId(bo.getTxId());
                if (CollectionUtils.isEmpty(receipts)) {
                    log.error("[processNeedVoteTx]receipts is empty by txId:{}", bo.getTxId());
                    return;
                }
                if (receipts.size() < rsIds.size() - 1) {
                    log.error("[processNeedVoteTx]receipts.size:{} less than rsIds.size:{} by txId:{}", receipts.size(),
                        rsIds.size(), bo.getTxId());
                    return;
                }
                //get decision result
                boolean decision = voteService.getDecision(receipts, policy.getDecisionType());
                log.info("[processNeedVoteTx]decision:{}", decision);
                if (!decision) {
                    toEndAndCallBackByError(bo, CoreTxStatusEnum.NEED_VOTE, RsCoreErrorEnum.RS_CORE_VOTE_DECISION_FAIL);
                    return;
                }
                List<SignInfo> signInfos = voteService.getSignInfos(receipts);
                signInfos.addAll(bo.getSignDatas());
                coreTxRepository.updateSignDatas(bo.getTxId(), signInfos);
                //change status to WAIT for SYNC pattern
                coreTxRepository.updateStatus(bo.getTxId(), CoreTxStatusEnum.NEED_VOTE, CoreTxStatusEnum.WAIT);
            }
        });
        log.info("[processNeedVoteTx]is success");
    }

    /**
     * to tx END status for fail business and call back custom rs
     *
     * @param bo
     * @param from
     * @param rsCoreErrorEnum
     */
    private void toEndAndCallBackByError(CoreTxBO bo, CoreTxStatusEnum from, RsCoreErrorEnum rsCoreErrorEnum) {
        RespData respData = new RespData();
        respData.setCode(rsCoreErrorEnum.getCode());
        respData.setMsg(rsCoreErrorEnum.getDescription());
        toEndAndCallBackByError(bo, from, respData);
    }

    /**
     * to tx END status for fail business and call back custom rs
     *
     * @param bo
     * @param from
     * @param respData
     */
    private void toEndAndCallBackByError(CoreTxBO bo, CoreTxStatusEnum from, RespData respData) {
        log.info("[toEndAndCallBackByError]tx:{},from:{},respData:{}", bo, from, respData);
        txRequired.execute(new TransactionCallbackWithoutResult() {
            @Override protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                //save execute result and error code
                String txId = bo.getTxId();
                coreTxRepository.saveExecuteResult(txId, CoreTxResultEnum.FAIL, respData.getRespCode());
                //update status from 'from' to END
                coreTxRepository.updateStatus(txId, from, CoreTxStatusEnum.END);
                respData.setData(coreTxRepository.convertTxVO(bo));
                //callback custom rs
                rsCoreCallbackHandler.onEnd(respData);
            }
        });
        //同步通知
        try {
            persistedResultMap.put(bo.getTxId(), respData);
            clusterPersistedResultMap.put(bo.getTxId(), respData);
        } catch (Throwable e) {
            log.warn("sync notify rs resp data failed", e);
        }
    }

    /**
     * submit slave by schedule
     */
    @Override public void submitToSlave() {
        //max size
        int maxSize = 20;
        List<CoreTransactionPO> list = coreTxRepository.queryByStatus(CoreTxStatusEnum.WAIT, 0, maxSize);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<CoreTxBO> boList = new ArrayList<>(list.size());
        for (CoreTransactionPO po : list) {
            boList.add(coreTxRepository.convertTxBO(po));
        }
        //submit
        submitToSlave(boList);
    }

    /**
     * submit slave
     *
     * @param boList
     */
    private void submitToSlave(List<CoreTxBO> boList) {
        List<SignedTransaction> txs = makeTxs(boList);
        try {
            log.info("[submitToSlave] start");
            RespData respData = blockChainService.submitTransactions(txs);
            if (respData.getData() == null) {
                log.info("[submitToSlave] end");
                return;
            }
            //has fail tx
            List<TransactionVO> txsOfFail = (List<TransactionVO>)respData.getData();
            log.info("[submitToSlave] has fail tx:{}", txsOfFail);
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
                        toEndAndCallBackByError(bo, CoreTxStatusEnum.WAIT, mRes);
                    } catch (Throwable e) {
                        log.error("[submitToSlave.toEndAndCallBackByError] has error", e);
                    }
                }
            }
        } catch (SlaveException e) {
            log.error("[submitToSlave] has slave error", e);
        } catch (Throwable e) {
            log.error("[submitToSlave] has unknown error", e);
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
