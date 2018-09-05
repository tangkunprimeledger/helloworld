package com.higgs.trust.rs.core.callback;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.common.enums.MonitorTargetEnum;
import com.higgs.trust.common.utils.MonitorLogUtils;
import com.higgs.trust.config.p2p.AbstractClusterInfo;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.rs.common.enums.RequestEnum;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.TxCallbackRegistor;
import com.higgs.trust.rs.core.api.enums.CallbackTypeEnum;
import com.higgs.trust.rs.core.api.enums.CoreTxResultEnum;
import com.higgs.trust.rs.core.bo.VoteRule;
import com.higgs.trust.rs.core.repository.RequestRepository;
import com.higgs.trust.rs.core.repository.VoteRuleRepository;
import com.higgs.trust.rs.core.vo.RsCoreTxVO;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.enums.manage.VotePatternEnum;
import com.higgs.trust.slave.core.repository.config.ConfigRepository;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author liuyu
 * @description
 * @date 2018-06-07
 */
@Component @Slf4j public class RsCoreBatchCallbackProcessor {
    @Autowired private TxCallbackRegistor txCallbackRegistor;
    @Autowired private VoteRuleRepository voteRuleRepository;
    @Autowired private ConfigRepository configRepository;
    @Autowired private NodeState nodeState;
    @Autowired private RequestRepository requestRepository;
    @Autowired AbstractClusterInfo clusterInfo;

    private TxBatchCallbackHandler getCallbackHandler() {
        TxBatchCallbackHandler txCallbackHandler = txCallbackRegistor.getCoreTxBatchCallback();
        if (txCallbackHandler == null) {
            log.error("[getCallbackHandler]batch call back handler is not register");
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_TX_CORE_TX_CALLBACK_NOT_SET);
        }
        return txCallbackHandler;
    }

    public void onPersisted(List<RsCoreTxVO> txs, BlockHeader blockHeader) {
        Map<String, List<RsCoreTxVO>> map = parseTx(txs);
        log.debug("[onPersisted]map:{}",map);
        for (String policyId : map.keySet()) {
            List<RsCoreTxVO> rsCoreTxVOS = map.get(policyId);
            InitPolicyEnum policyEnum = InitPolicyEnum.getInitPolicyEnumByPolicyId(policyId);
            if (policyEnum != null) {
                switch (policyEnum) {
                    case NA:
                        return;
                    case REGISTER_POLICY:
                        processRegisterPolicy(rsCoreTxVOS);
                        return;
                    case REGISTER_RS:
                        processRegisterRS(rsCoreTxVOS);
                        return;
                    case CONTRACT_ISSUE:
                        return;
                    case CONTRACT_DESTROY:
                        return;
                    case CA_UPDATE:
                        processCaUpdate(rsCoreTxVOS);
                        return;
                    case CA_CANCEL:
                        processCaCancel(rsCoreTxVOS);
                        return;
                    case CA_AUTH:
                        processCaAuth(rsCoreTxVOS);
                        return;
                    case CANCEL_RS:
                        processCancelRS(rsCoreTxVOS);
                        return;
                    case NODE_JOIN:
                        processNodeJoin(rsCoreTxVOS);
                        return;
                    case NODE_LEAVE:
                        processNodeLeave(rsCoreTxVOS);
                        return;
                    default:
                        break;
                }
            }
            //callback custom
            TxBatchCallbackHandler txBatchCallbackHandler = getCallbackHandler();
            txBatchCallbackHandler.onPersisted(policyId,rsCoreTxVOS,blockHeader);
        }
    }

    public void onEnd(List<RsCoreTxVO> txs, BlockHeader blockHeader) {
        Map<String, List<RsCoreTxVO>> map = parseTx(txs);
        for (String policyId : map.keySet()) {
            InitPolicyEnum policyEnum = InitPolicyEnum.getInitPolicyEnumByPolicyId(policyId);
            if (policyEnum != null) {
                switch (policyEnum) {
                    case NA:
                        return;
                    case REGISTER_POLICY:
                        return;
                    case REGISTER_RS:
                        return;
                    case CONTRACT_ISSUE:
                        return;
                    case CONTRACT_DESTROY:
                        return;
                    case CA_UPDATE:
                        return;
                    case CA_CANCEL:
                        return;
                    case CA_AUTH:
                        return;
                    case CANCEL_RS:
                        return;
                    case NODE_JOIN:
                        return;
                    case NODE_LEAVE:
                        return;
                    default:
                        break;
                }
            }
            List<RsCoreTxVO> rsCoreTxVOS = map.get(policyId);
            //callback custom
            TxBatchCallbackHandler txBatchCallbackHandler = getCallbackHandler();
            txBatchCallbackHandler.onEnd(policyId,rsCoreTxVOS,blockHeader);
        }
    }

    public void onFailover(List<RsCoreTxVO> txs, BlockHeader blockHeader) {
        Map<String, List<RsCoreTxVO>> map = parseTx(txs);
        for (String policyId : map.keySet()) {
            List<RsCoreTxVO> rsCoreTxVOS = map.get(policyId);
            InitPolicyEnum policyEnum = InitPolicyEnum.getInitPolicyEnumByPolicyId(policyId);
            if (policyEnum != null) {
                switch (policyEnum) {
                    case NA:
                    case REGISTER_RS:
                    case CONTRACT_ISSUE:
                    case CONTRACT_DESTROY:
                    case CA_UPDATE:
                    case CA_CANCEL:
                    case CA_AUTH:
                    case CANCEL_RS:
                    case NODE_JOIN:
                    case NODE_LEAVE:
                        return;
                    case REGISTER_POLICY:
                        processRegisterPolicy(rsCoreTxVOS);
                        return;
                    default:
                        break;
                }
            }
            //callback custom
            TxBatchCallbackHandler txBatchCallbackHandler = getCallbackHandler();
            txBatchCallbackHandler.onFailover(policyId,rsCoreTxVOS,blockHeader);
        }
    }

    /**
     * return key:policyId value:txs
     *
     * @param txs
     * @return
     */
    private static Map<String, List<RsCoreTxVO>> parseTx(List<RsCoreTxVO> txs) {
        Set<String> policyIds = new LinkedHashSet<>();
        for (RsCoreTxVO tx : txs) {
            policyIds.add(tx.getPolicyId());
        }
        Map<String, List<RsCoreTxVO>> map = new LinkedHashMap<>();
        for (String policyId : policyIds) {
            List<RsCoreTxVO> list = new ArrayList<>();
            for (RsCoreTxVO tx : txs) {
                if (StringUtils.equals(tx.getPolicyId(), policyId)) {
                    list.add(tx);
                }
            }
            map.put(policyId, list);
        }
        return map;
    }

    /**
     * process register-policy
     *
     * @param rsCoreTxVOS
     */
    private void processRegisterPolicy(List<RsCoreTxVO> rsCoreTxVOS) {
        log.debug("[processRegisterPolicy]txs:{}", rsCoreTxVOS);
        List<VoteRule> voteRules = new ArrayList<>();
        for (RsCoreTxVO tx : rsCoreTxVOS) {
            if (tx.getExecuteResult() == CoreTxResultEnum.SUCCESS) {
                RegisterPolicy registerPolicy = (RegisterPolicy)tx.getActionList().get(0);
                JSONObject jsonObject = tx.getBizModel();
                VoteRule voteRule = new VoteRule();
                voteRule.setPolicyId(registerPolicy.getPolicyId());
                voteRule.setVotePattern(VotePatternEnum.fromCode(jsonObject.getString("votePattern")));
                voteRule.setCallbackType(CallbackTypeEnum.fromCode(jsonObject.getString("callbackType")));
                voteRules.add(voteRule);
            }
        }
        //batch inset
        if (!CollectionUtils.isEmpty(voteRules)) {
            voteRuleRepository.batchInsert(voteRules);
        }
        //update request status
        requestRepository.batchUpdateStatus(rsCoreTxVOS, RequestEnum.PROCESS, RequestEnum.DONE);
    }

    private void processRegisterRS(List<RsCoreTxVO> rsCoreTxVOS) {
        requestRepository.batchUpdateStatus(rsCoreTxVOS, RequestEnum.PROCESS, RequestEnum.DONE);
    }

    private void processCancelRS(List<RsCoreTxVO> rsCoreTxVOS) {
        requestRepository.batchUpdateStatus(rsCoreTxVOS, RequestEnum.PROCESS, RequestEnum.DONE);
    }

    /**
     * get success txs
     *
     * @param rsCoreTxVOS
     * @return
     */
    private List<RsCoreTxVO> getSuccessful(List<RsCoreTxVO> rsCoreTxVOS) {
        List<RsCoreTxVO> list = new ArrayList<>();
        for (RsCoreTxVO vo : rsCoreTxVOS) {
            if (vo.getExecuteResult() == CoreTxResultEnum.SUCCESS) {
                list.add(vo);
            }
        }
        return list;
    }

    /**
     * get self
     *
     * @param rsCoreTxVOS
     * @return
     */
    private List<String> getSelfCANodes(List<RsCoreTxVO> rsCoreTxVOS) {
        List<String> list = new ArrayList<>();
        for (RsCoreTxVO vo : rsCoreTxVOS) {
            if (StringUtils.equals(vo.getSender(), nodeState.getNodeName())) {
                list.add(vo.getSender());
            }
        }
        return list;
    }

    private void processCaUpdate(List<RsCoreTxVO> rsCoreTxVOS) {
        rsCoreTxVOS = getSuccessful(rsCoreTxVOS);
        if (CollectionUtils.isEmpty(rsCoreTxVOS)) {
            log.info("[processCaUpdate]ca update has fail");
            MonitorLogUtils.logTextMonitorInfo(MonitorTargetEnum.SLAVE_CA_UPDATE_ERROR, 1);
            return;
        }
        clusterInfo.setRefresh();
        log.info("[processCaUpdate] set cluster info refresh");

        List<String> nodes = getSelfCANodes(rsCoreTxVOS);

        if (CollectionUtils.isEmpty(nodes)) {
            log.info("[processCaUpdate] current node ={}, is not ca updated pubKey/priKey", nodeState.getNodeName());
            return;
        }
        log.info("[processCaUpdate] start to update pubKey/priKey");
        // update table config, set tmpKey to key
        configRepository.batchEnable(nodes);
        log.info("[processCaUpdate] end update pubKey/priKey, nodeName={}", nodes);
    }

    private void processCaCancel(List<RsCoreTxVO> rsCoreTxVOS) {
        rsCoreTxVOS = getSuccessful(rsCoreTxVOS);
        if (CollectionUtils.isEmpty(rsCoreTxVOS)) {
            log.info("[processCaCancel]ca cancel is fail");
            MonitorLogUtils.logTextMonitorInfo(MonitorTargetEnum.SLAVE_CA_CANCEL_ERROR, 1);
            return;
        }

        clusterInfo.setRefresh();
        log.info("[processCaCancel] set cluster info refresh");

        List<String> nodes = getSelfCANodes(rsCoreTxVOS);

        if (CollectionUtils.isEmpty(nodes)) {
            log.info("[processCaCancel] current node ={}, is not ca cancel, end cancel pubKey/priKey",
                nodeState.getNodeName());
            return;
        }
        log.info("[processCaCancel] start to invalid pubKeyForConsensus/priKey, nodeName={}", nodes);
        //set pubKeyForConsensus and priKey to invalid
        configRepository.batchCancel(nodes);
        log.info("[processCaCancel] end invalid pubKeyForConsensus/priKey, nodeName={}", nodes);
    }

    private void processCaAuth(List<RsCoreTxVO> rsCoreTxVOS) {
        rsCoreTxVOS = getSuccessful(rsCoreTxVOS);
        if (CollectionUtils.isEmpty(rsCoreTxVOS)) {
            log.info("[processCaAuth]ca auth is fail");
            MonitorLogUtils.logTextMonitorInfo(MonitorTargetEnum.SLAVE_CA_AUTH_ERROR, 1);
            return;
        }

        clusterInfo.setRefresh();
        log.info("[processCaAuth] set cluster info refresh");
    }

    private void processNodeJoin(List<RsCoreTxVO> rsCoreTxVOS) {
        rsCoreTxVOS = getSuccessful(rsCoreTxVOS);
        if (CollectionUtils.isEmpty(rsCoreTxVOS)) {
            log.info("[processNodeJoin]node join is fail");
            MonitorLogUtils.logTextMonitorInfo(MonitorTargetEnum.SLAVE_NODE_JOIN_ERROR, 1);
            return;
        }

        clusterInfo.setRefresh();
        log.info("[processNodeJoin] set cluster info refresh");
    }

    private void processNodeLeave(List<RsCoreTxVO> rsCoreTxVOS) {
        rsCoreTxVOS = getSuccessful(rsCoreTxVOS);
        if (CollectionUtils.isEmpty(rsCoreTxVOS)) {
            log.info("[processNodeLeave]node leave is fail");
            MonitorLogUtils.logTextMonitorInfo(MonitorTargetEnum.SLAVE_NODE_LEAVE_ERROR, 1);
            return;
        }

        clusterInfo.setRefresh();
        log.info("[processNodeLeave] set cluster info refresh");
    }
}
