package com.higgs.trust.rs.core.service;

import com.google.common.collect.Lists;
import com.higgs.trust.common.dao.RocksUtils;
import com.higgs.trust.common.enums.MonitorTargetEnum;
import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.common.utils.MonitorLogUtils;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.config.view.ClusterView;
import com.higgs.trust.config.view.IClusterViewManager;
import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.*;
import com.higgs.trust.rs.core.api.enums.*;
import com.higgs.trust.rs.core.bo.CoreTxBO;
import com.higgs.trust.rs.core.bo.VoteReceipt;
import com.higgs.trust.rs.core.bo.VoteRule;
import com.higgs.trust.rs.core.callback.RsCoreBatchCallbackProcessor;
import com.higgs.trust.rs.core.callback.RsCoreCallbackProcessor;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import com.higgs.trust.rs.core.dao.po.CoreTransactionProcessPO;
import com.higgs.trust.rs.core.repository.CoreTxRepository;
import com.higgs.trust.rs.core.repository.VoteReceiptRepository;
import com.higgs.trust.rs.core.repository.VoteRuleRepository;
import com.higgs.trust.rs.core.task.TxIdBO;
import com.higgs.trust.rs.core.task.TxIdProducer;
import com.higgs.trust.rs.core.vo.RsCoreTxVO;
import com.higgs.trust.slave.api.BlockChainService;
import com.higgs.trust.slave.api.enums.RespCodeEnum;
import com.higgs.trust.slave.api.enums.TxTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.enums.manage.VotePatternEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.api.vo.TransactionVO;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.asynctosync.HashBlockingMap;
import com.higgs.trust.slave.core.repository.PolicyRepository;
import com.higgs.trust.slave.core.service.pending.TransactionValidator;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignInfo;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.manage.Policy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.rocksdb.ReadOptions;
import org.rocksdb.Transaction;
import org.rocksdb.WriteOptions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CoreTransactionServiceImpl implements CoreTransactionService, InitializingBean {
    @Value("${higgs.trust.isSlave:false}")
    private boolean isSlave;
    @Autowired
    private TransactionTemplate txRequired;
    @Autowired
    private RsConfig rsConfig;
    @Autowired
    private BizTypeService bizTypeService;
    @Autowired
    private CoreTxRepository coreTxRepository;
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
    private SignService signService;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private HashBlockingMap<RespData> persistedResultMap;
    @Autowired
    private HashBlockingMap<RespData> clusterPersistedResultMap;
    @Autowired
    private DistributeCallbackNotifyService distributeCallbackNotifyService;
    @Autowired
    private TxIdProducer txIdProducer;
    @Autowired
    private TransactionValidator transactionValidator;
    @Autowired
    private IClusterViewManager viewManager;

    /**
     * init redis distribution topic listener
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Init redis distribution topic listener to process tx.");
        if (!isSlave) {
            log.info("this node not slave do not need to initAsyncProcessInitTxListener");
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
            txIdProducer.put(new TxIdBO(msg, CoreTxStatusEnum.INIT));
        });
    }

    @Override
    public RespData syncWait(String txId, boolean forEnd) {
        RespData respData = null;
        try {
            //ON_PERSISTED_CALLBACK
            if (!forEnd) {
                respData = distributeCallbackNotifyService
                        .syncWaitNotify(txId, RedisMegGroupEnum.ON_PERSISTED_CALLBACK_MESSAGE_NOTIFY,
                                rsConfig.getSyncRequestTimeout(), TimeUnit.MILLISECONDS);
            } else {
                //ON_CLUSTER_PERSISTED_CALLBACK
                respData = distributeCallbackNotifyService
                        .syncWaitNotify(txId, RedisMegGroupEnum.ON_CLUSTER_PERSISTED_CALLBACK_MESSAGE_NOTIFY,
                                rsConfig.getSyncRequestTimeout(), TimeUnit.MILLISECONDS);
            }
        } catch (RsCoreException e) {
            log.error("tx handle exception. ", e);
            respData = new RespData();
            respData.setCode(e.getCode().getCode());
            respData.setMsg(e.getCode().getDescription());
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
        if (log.isDebugEnabled()) {
            log.debug("[submitTx]{}", coreTx);
        }
        if (coreTx == null) {
            log.error("[submitTx] the tx is null");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_PARAM_VALIDATE_ERROR);
        }
        //tx.validator
        transactionValidator.verify(coreTx);
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
            log.warn("[submitTx]is idempotent txId:{}", coreTx.getTxId());
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_IDEMPOTENT);
        }
        List<SignInfo> signs = Lists.newArrayList();
        if (!TxTypeEnum.isTargetType(coreTx.getTxType(), TxTypeEnum.NODE)) {
            //sign tx for self
            SignInfo signInfo = signService.signTx(coreTx);
            if (signInfo == null) {
                log.error("[submitTx] self sign data is empty");
                throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_VERIFY_SIGNATURE_FAILED);
            }
            signs.add(signInfo);
        }

        if (rsConfig.isUseMySQL()) {
            txRequired.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    //save coreTx to db
                    coreTxRepository.add(coreTx, signs, 0L);
                }
            });
        } else {
            Transaction tx = RocksUtils.beginTransaction(new WriteOptions());
            ThreadLocalUtils.putRocksTx(tx);
            try {
                if (null == coreTxRepository.getForUpdate(tx, new ReadOptions(), coreTx.getTxId(), true)) {
                    //save coreTx to db
                    coreTxRepository.add(coreTx, signs, 0L);
                    RocksUtils.txCommit(tx);
                }
            } finally {
                ThreadLocalUtils.clearRocksTx();
            }
        }
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
            topic.publishAsync(txId);
        } catch (Throwable e) {
            log.error("Publish msg to process initTx msg failed！");
        }

    }

    @Override
    public void processInitTx(String txId) {
        log.debug("[processInitTx]txId:{}", txId);
        CoreTxBO bo;
        if (rsConfig.isUseMySQL()) {
            bo = txRequired.execute(new TransactionCallback<CoreTxBO>() {
                @Override
                public CoreTxBO doInTransaction(TransactionStatus transactionStatus) {
                    CoreTransactionPO po = coreTxRepository.queryByTxId(txId, true);
                    if (null == po) {
                        log.debug("[processInitTx]cannot acquire lock, txId={}", txId);
                        return null;
                    }
                    return processInitTxInTransaction(po);
                }
            });
        } else {
            //check by status
            CoreTransactionProcessPO processPO = coreTxRepository.queryByStatus(txId, CoreTxStatusEnum.INIT);
            if (processPO == null) {
                return;
            }
            Transaction tx = RocksUtils.beginTransaction(new WriteOptions());
            try {
                ThreadLocalUtils.putRocksTx(tx);
                CoreTransactionPO po = coreTxRepository.getForUpdate(tx, new ReadOptions(), txId, true);
                if (null == po) {
                    log.warn("[processInitTx]cannot acquire lock, txId={}", txId);
                    return;
                }
                bo = processInitTxInTransaction(po);
            } finally {
                if (null != tx) {
                    RocksUtils.txCommit(tx);
                }
                ThreadLocalUtils.clearRocksTx();
            }
        }
        if (null == bo) {
            return;
        }
        /**
         * if voteRule is null, bo must be null
         */
        VoteRule voteRule = getVoteRule(bo.getPolicyId());
        if (voteRule.getVotePattern() == VotePatternEnum.SYNC) {
            txIdProducer.put(new TxIdBO(txId, CoreTxStatusEnum.WAIT));
        }
    }

    private CoreTxBO processInitTxInTransaction(CoreTransactionPO po) {
        if (null == coreTxRepository.queryStatusByTxId(po.getTxId(), CoreTxStatusEnum.INIT)) {
            log.info("[processInitTx]the coreTx is null or status is not INIT txId:{}", po.getTxId());
            return null;
        }
        Date lockTime = po.getLockTime();
        if (lockTime != null && lockTime.after(new Date())) {
            log.info("[processInitTx]should skip this tx by lock time:{}", lockTime);
            return null;
        }
        //convert bo
        CoreTxBO bo = coreTxRepository.convertTxBO(po);
        String policyId = bo.getPolicyId();
        log.debug("[processInitTx]policyId:{}", policyId);
        Policy policy = policyRepository.getPolicyById(policyId);
        if (policy == null) {
            log.error("[processInitTx]get policy is null by policyId:{}", policyId);
            toEndOrCallBackByError(bo, CoreTxStatusEnum.INIT, RsCoreErrorEnum.RS_CORE_TX_POLICY_NOT_EXISTS_FAILED,
                    true);
            return null;
        }
        //define default sign type
        SignInfo.SignTypeEnum signType = SignInfo.SignTypeEnum.BIZ;
        //check txType for NODE type
        if (TxTypeEnum.isTargetType(bo.getTxType(), TxTypeEnum.NODE)) {
            //reset rsIds,require all consensus layer nodes
            ClusterView currentView = viewManager.getCurrentView();
            policy.setRsIds(currentView.getNodeNames());
            //reset sign type
            signType = SignInfo.SignTypeEnum.CONSENSUS;
            log.info("[processInitTx]reset rsIds:{}", policy.getRsIds());
        }
        // vote rule
        VoteRule voteRule = getVoteRule(policyId);
        if (voteRule == null || null == voteRule.getVotePattern()) {
            log.error("[processInitTx]get voteRule is null or votePattern is null by policyId:{}", policyId);
            toEndOrCallBackByError(bo, CoreTxStatusEnum.INIT, RsCoreErrorEnum.RS_CORE_VOTE_RULE_NOT_EXISTS_ERROR, true);
            return null;
        }
        VotePatternEnum votePattern = voteRule.getVotePattern();
        //check rs ids
        if (CollectionUtils.isEmpty(policy.getRsIds())) {
            log.debug("[processInitTx]rs ids is empty txId:{}", bo.getTxId());
            //the blow system policy  needs rsIds
            if (StringUtils.equals(InitPolicyEnum.CONTRACT_ISSUE.getPolicyId(), policyId) || StringUtils
                    .equals(InitPolicyEnum.CONTRACT_DESTROY.getPolicyId(), policyId) || StringUtils
                    .equals(InitPolicyEnum.UTXO_DESTROY.getPolicyId(), policyId) || StringUtils
                    .equals(InitPolicyEnum.UTXO_ISSUE.getPolicyId(), policyId)) {
                toEndOrCallBackByError(bo, CoreTxStatusEnum.INIT, RsCoreErrorEnum.RS_CORE_VOTE_VOTERS_IS_EMPTY_ERROR,
                        true);
                return null;
            }
            //when not like before policy s ,still submit to slave
            coreTxRepository.updateStatus(bo.getTxId(), CoreTxStatusEnum.INIT, CoreTxStatusEnum.WAIT);
            return bo;
        }
        //get need voters from saved sign info
        List<String> needVoters = voteService.getVoters(bo.getSignDatas(), policy.getRsIds());
        if (CollectionUtils.isEmpty(needVoters)) {
            log.debug("[processInitTx]need voters is empty txId:{}", bo.getTxId());
            //still submit to slave
            coreTxRepository.updateStatus(bo.getTxId(), CoreTxStatusEnum.INIT, CoreTxStatusEnum.WAIT);
            return bo;
        }
        //request voting
        List<VoteReceipt> receipts = voteService.requestVoting(bo, needVoters, votePattern);
        //if receipts is empty,should retry
        if (CollectionUtils.isEmpty(receipts)) {
            log.error("[processInitTx]voting receipts is empty by SYNC txId:{}", bo.getTxId());
            return null;
        }
        //get sign info from receipts
        List<SignInfo> signInfos = voteService.getSignInfos(receipts, signType);
        signInfos.addAll(bo.getSignDatas());
        //update signDatas
        coreTxRepository.updateSignDatas(bo.getTxId(), signInfos);
        //save already voting result for SYNC pattern
        if (votePattern == VotePatternEnum.SYNC) {
            voteReceiptRepository.batchAdd(receipts);
        }
        //when there is failure as net-timeout,should retry
        if (receipts.size() < needVoters.size()) {
            log.error("[processInitTx]receipts.size:{} is less than voters.size:{} txId:{}", receipts.size(),
                    needVoters.size(), bo.getTxId());
            return null;
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
            log.debug("[processInitTx]decision:{}", decision);
            if (!decision) {
                toEndOrCallBackByError(bo, CoreTxStatusEnum.INIT, RsCoreErrorEnum.RS_CORE_VOTE_DECISION_FAIL, true);
                return null;
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
        return bo;
    }

    private VoteRule getVoteRule(String policyId) {
        VoteRule voteRule;
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
        log.debug("[processInitTx]is success");
        return voteRule;
    }

    @Override
    public void processNeedVoteTx(String txId) {
        log.info("[processNeedVoteTx]txId:{}", txId);
        if (rsConfig.isUseMySQL()) {
            txRequired.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    CoreTransactionPO po = coreTxRepository.queryByTxId(txId, true);
                    if (null == po) {
                        log.warn("[processNeedVoteTx]the coreTx can not acquired lock txId:{}", txId);
                        return;
                    }
                    processNeedVoteTxInTransaction(po);
                }
            });
        } else {
            Transaction tx = RocksUtils.beginTransaction(new WriteOptions());
            try {
                ThreadLocalUtils.putRocksTx(tx);
                CoreTransactionPO po = coreTxRepository.getForUpdate(tx, new ReadOptions(), txId, true);
                if (null == po) {
                    log.warn("[processNeedVoteTx]the coreTx can not acquired lock txId:{}", txId);
                    return;
                }
                processNeedVoteTxInTransaction(po);
            } finally {
                if (tx != null) {
                    RocksUtils.txCommit(tx);
                }
                ThreadLocalUtils.clearRocksTx();
                ;
            }
        }
    }

    private void processNeedVoteTxInTransaction(CoreTransactionPO po) {

        if (null == coreTxRepository.queryStatusByTxId(po.getTxId(), CoreTxStatusEnum.NEED_VOTE)) {
            log.info("[processNeedVoteTx]the coreTx status is not NEED_VOTE txId:{}", po.getTxId());
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
            toEndOrCallBackByError(bo, CoreTxStatusEnum.NEED_VOTE, RsCoreErrorEnum.RS_CORE_TX_POLICY_NOT_EXISTS_FAILED,
                    true);
            return;
        }
        //check tx type
        boolean isNodeType = TxTypeEnum.isTargetType(bo.getTxType(), TxTypeEnum.NODE);
        if (isNodeType) {
            //reset rsIds,require all consensus layer nodes
            ClusterView currentView = viewManager.getCurrentView();
            policy.setRsIds(currentView.getNodeNames());
        }
        List<String> rsIds = policy.getRsIds();
        if (CollectionUtils.isEmpty(rsIds)) {
            log.error("[processNeedVoteTx]rsIds is empty by txId:{}", bo.getTxId());
            toEndOrCallBackByError(bo, CoreTxStatusEnum.NEED_VOTE, RsCoreErrorEnum.RS_CORE_VOTE_VOTERS_IS_EMPTY_ERROR,
                    true);
            return;
        }
        //query receipts by txId
        List<VoteReceipt> receipts = voteReceiptRepository.queryByTxId(bo.getTxId());
        if (CollectionUtils.isEmpty(receipts)) {
            log.warn("[processNeedVoteTx]receipts is empty by txId:{}", bo.getTxId());
            return;
        }
        //normal business
        List<String> lastRsIds = rsIds;
        if (!isNodeType) {
            lastRsIds = new ArrayList<>();
            //filter self
            for (String rsName : rsIds) {
                if (!StringUtils.equals(rsName, rsConfig.getRsName())) {
                    lastRsIds.add(rsName);
                }
            }
        }
        if (receipts.size() != lastRsIds.size()) {
            log.warn("[processNeedVoteTx]receipts.size:{} less than rsIds.size:{} by txId:{}", receipts.size(),
                    rsIds.size(), bo.getTxId());
            return;
        }
        //get decision result
        boolean decision = voteService.getDecision(receipts, policy.getDecisionType());
        log.info("[processNeedVoteTx]decision:{}", decision);
        if (!decision) {
            toEndOrCallBackByError(bo, CoreTxStatusEnum.NEED_VOTE, RsCoreErrorEnum.RS_CORE_VOTE_DECISION_FAIL, true);
            return;
        }
        //get signType
        SignInfo.SignTypeEnum signType = isNodeType ? SignInfo.SignTypeEnum.CONSENSUS : SignInfo.SignTypeEnum.BIZ;
        List<SignInfo> signInfos = voteService.getSignInfos(receipts, signType);
        if (!isNodeType) {
            List<SignInfo> lastSigns = bo.getSignDatas();
            for (SignInfo signInfo : lastSigns) {
                if (StringUtils.equals(rsConfig.getRsName(), signInfo.getOwner())) {
                    signInfos.add(signInfo);
                    break;
                }
            }
        }
        coreTxRepository.updateSignDatas(bo.getTxId(), signInfos);
        //change status to WAIT for SYNC pattern
        coreTxRepository.updateStatus(bo.getTxId(), CoreTxStatusEnum.NEED_VOTE, CoreTxStatusEnum.WAIT);
        log.info("[processNeedVoteTx]is success");
    }

    /**
     * to tx END status for fail business and call back custom rs
     *
     * @param bo
     * @param from
     * @param rsCoreErrorEnum
     */
    private void toEndOrCallBackByError(CoreTxBO bo, CoreTxStatusEnum from, RsCoreErrorEnum rsCoreErrorEnum,
                                        boolean isCallback) {
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
        //save execute result and error code
        String txId = bo.getTxId();
        coreTxRepository
                .saveExecuteResultAndHeight(txId, CoreTxResultEnum.FAIL, respData.getRespCode(), respData.getMsg(), 0L);

        RsCoreTxVO vo = BeanConvertor.convertBean(bo, RsCoreTxVO.class);
        vo.setStatus(CoreTxStatusEnum.INIT);
        vo.setExecuteResult(CoreTxResultEnum.FAIL);
        vo.setErrorCode(respData.getRespCode());
        vo.setErrorMsg(respData.getMsg());
        //delete by status
        coreTxRepository.batchDelete(Lists.newArrayList(vo), from);
        //callback custom rs
        if (isCallback) {
            if (!rsConfig.isBatchCallback()) {
                respData.setData(coreTxRepository.convertTxVO(bo));
                rsCoreCallbackHandler.onEnd(respData, null);
            } else {
                //for batch interface
                rsCoreBatchCallbackProcessor.onEnd(Lists.newArrayList(vo), null);
            }
        }
        //同步通知
        try {
            //reset txId
            respData.setData(bo.getTxId());
            distributeCallbackNotifyService.notifySyncResult(Lists.newArrayList(respData), RedisMegGroupEnum.ON_PERSISTED_CALLBACK_MESSAGE_NOTIFY);
            distributeCallbackNotifyService.notifySyncResult(Lists.newArrayList(respData), RedisMegGroupEnum.ON_CLUSTER_PERSISTED_CALLBACK_MESSAGE_NOTIFY);
        } catch (Throwable e) {
            log.warn("sync notify rs resp data failed", e);
        }
    }

    @Override
    public RsCoreTxVO queryCoreTx(String txId) {
        CoreTransactionPO coreTransactionPO = coreTxRepository.queryByTxId(txId, false);
        if (coreTransactionPO == null) {
            log.error("[queryCoreTx]result is null txId:{}", txId);
            return null;
        }
        CoreTransactionProcessPO coreTransactionProcessPO = coreTxRepository.queryStatusByTxId(txId, null);
        CoreTxBO coreTxBO = coreTxRepository.convertTxBO(coreTransactionPO);
        RsCoreTxVO coreTxVO = BeanConvertor.convertBean(coreTxBO, RsCoreTxVO.class);
        coreTxVO.setStatus(CoreTxStatusEnum.formCode(
                null != coreTransactionProcessPO ? coreTransactionProcessPO.getStatus() : null));
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
    @Override
    public void submitToSlave(List<CoreTxBO> boList) {
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
                        if (rsConfig.isUseMySQL()) {
                            txRequired.execute(new TransactionCallbackWithoutResult() {
                                @Override
                                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                                    toEndOrCallBackByError(bo, CoreTxStatusEnum.WAIT, mRes, true);
                                }
                            });
                        } else {
                            try {
                                Transaction tx = RocksUtils.beginTransaction(new WriteOptions());
                                ThreadLocalUtils.putRocksTx(tx);
                                toEndOrCallBackByError(bo, CoreTxStatusEnum.WAIT, mRes, true);
                                RocksUtils.txCommit(tx);
                            } finally {
                                ThreadLocalUtils.clearRocksTx();
                            }
                        }

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
