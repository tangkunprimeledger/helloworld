package com.higgs.trust.slave.core.service.ca;

import com.higgs.trust.common.enums.MonitorTargetEnum;
import com.higgs.trust.common.utils.MonitorLogUtils;
import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.consensus.config.NodeProperties;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.RespCodeEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.config.ConfigRepository;
import com.higgs.trust.slave.core.service.action.ca.CaInitHandler;
import com.higgs.trust.slave.integration.ca.CaInitClient;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.ca.CaAction;
import com.higgs.trust.slave.model.bo.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author WangQuanzhou
 * @desc ca init service
 * @date 2018/6/5 15:48
 */
@Service @Slf4j public class CaInitServiceImpl implements CaInitService {

    @Autowired private ConfigRepository configRepository;
    @Autowired private NodeState nodeState;
    @Autowired private CaInitClient caClient;
    @Autowired private ClusterInfo clusterInfo;
    @Autowired private CaInitHandler caInitHandler;
    @Autowired private NodeProperties nodeProperties;

    /**
     * @param
     * @return
     * @desc
     */
    @Override public void initKeyPair() {

        List<Action> caActionList = acquirePubKeys(nodeProperties.getStartupRetryTime());

        // construct genius block and insert into db
        try {
            log.info("[CaInitServiceImpl.initKeyPair] start to generate genius block");

            // sort caActionList by user
            Collections.sort(caActionList, new Comparator<Action>() {
                @Override public int compare(Action caAction1, Action caAction2) {
                    return ((CaAction)caAction1).getUser().compareTo(((CaAction)caAction2).getUser());
                }
            });

            if (log.isDebugEnabled()) {
                log.debug("[CaInitServiceImpl.initKeyPair] user ={}, caActionList={}", nodeState.getNodeName(),
                    caActionList.toString());
            }

            caInitHandler.process(caActionList);
            log.info("[CaInitServiceImpl.initKeyPair] end generate genius block");

        } catch (Throwable e) {
            log.error("[CaInitServiceImpl.initKeyPair] cluster init CA error", e);
            throw new SlaveException(SlaveErrorEnum.SLAVE_CA_INIT_ERROR,
                "[CaInitServiceImpl.initKeyPair] cluster init CA error", e);
        }

    }

    private List<Action> acquirePubKeys(int retryCount) {
        List<String> nodeList = clusterInfo.clusterNodeNames();
        Set<String> nodeSet = new HashSet<>();
        List<Action> caActionList = new LinkedList<>();
        log.info(
            "[CaInitServiceImpl.acquirePubKeys] start to acquire all nodes' pubKey, nodeList size = {}, nodeList = {}",
            nodeList.size(), nodeList.toString());

        int i = 0;
        do {
            if (nodeSet.size() == nodeList.size()) {
                log.info("[CaInitServiceImpl.acquirePubKeys]  acquired all nodes' pubKey, nodeSet size = {}",
                    nodeSet.size());
                return caActionList;
            }
            // acquire all nodes' pubKey
            nodeList.forEach((nodeName) -> {
                try {
                    synchronized (CaInitServiceImpl.class) {
                        if (!nodeSet.contains(nodeName)) {
                            RespData<List<Config>> resp = caClient.caInit(nodeName);
                            if (resp.isSuccess()) {
                                for (Config config : resp.getData()) {
                                    CaAction caAction = new CaAction();
                                    caAction.setType(ActionTypeEnum.CA_INIT);
                                    caAction.setUser(nodeName);
                                    caAction.setPubKey(config.getPubKey());
                                    caAction.setUsage(config.getUsage());
                                    log.info("user={}, pubKey={},usage={}", nodeName, config.getPubKey(),
                                        config.getUsage());
                                    caActionList.add(caAction);
                                }
                                nodeSet.add(nodeName);
                            }
                        }
                    }
                } catch (Throwable e) {
                    log.warn("[CaInitServiceImpl.acquirePubKeys] acquire pubKey error, node={}", nodeName);
                }
            });
            if (caActionList.size() < (nodeList.size() * 2)) {
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    log.warn("acquire pubKey error.", e);
                }
            }
        } while (caActionList.size() < (nodeList.size() * 2) && ++i < retryCount);

        if (caActionList.size() < nodeList.size() * 2) {
            log.error(
                "[CaInitServiceImpl.acquirePubKeys]  error acquire all nodes' pubKey, caActionList size = {},caActionList={}, nodeList.size={},nodeList={}",
                caActionList.size(), caActionList.toString(), nodeList.size(), nodeList.toString());
            MonitorLogUtils.logTextMonitorInfo(MonitorTargetEnum.SLAVE_ACQUIRE_PUBKEY_ERROR, 1);
            throw new SlaveException(SlaveErrorEnum.SLAVE_CA_INIT_ERROR,
                "[CaInitServiceImpl.acquirePubKeys] cluster init CA error, can not acquire enough pubKeys");
        }

        log.info(
            "[CaInitServiceImpl.acquirePubKeys]  end acquire all nodes' pubKey, caActionList size = {}, nodeSet size={}, nodeList.size()={}, caActionList = {}",
            caActionList.size(), nodeSet.size(), nodeList.size(), caActionList);
        return caActionList;
    }

    @Override public RespData<List<Config>> initCaTx() {
        // acquire pubKey from DB
        List<Config> list = configRepository.getConfig(new Config(nodeState.getNodeName()));
        if (null == list) {
            log.info("[CaInitServiceImpl.initCaTx] nodeName={}, key pair not exist", nodeState.getNodeName());
            return new RespData<>(RespCodeEnum.DATA_NOT_EXIST);
        }
        if (list.size() != 2) {
            log.info("[CaInitServiceImpl.initCaTx] nodeName={}, pub/priKey pairs not equal 2, list size={}",
                nodeState.getNodeName(), list.size());
            return new RespData<>(RespCodeEnum.SYS_FAIL);
        }
        log.info("[CaInitServiceImpl.initCaTx] nodeName={}, KeyPair list={}", nodeState.getNodeName(), list);
        // erase priKey
        for (Config config : list) {
            config.setPriKey("");
        }
        RespData<List<Config>> resp = new RespData<>();
        resp.setData(list);
        return resp;
    }

}
