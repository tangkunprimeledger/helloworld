package com.higgs.trust.slave.core.service.node;

import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.consensus.config.NodeProperties;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.consensus.core.ConsensusStateMachine;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.config.ClusterConfigRepository;
import com.higgs.trust.slave.core.repository.config.ClusterNodeRepository;
import com.higgs.trust.slave.integration.node.NodeClient;
import com.higgs.trust.slave.model.bo.config.ClusterConfig;
import com.higgs.trust.slave.model.bo.config.ClusterNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author WangQuanzhou
 * @desc node consensus service
 * @date 2018/7/5 11:38
 */
@Service @Slf4j public class NodeConsensusService {

    @Autowired private ConsensusStateMachine consensusStateMachine;
    @Autowired private NodeState nodeState;
    @Autowired private ClusterNodeRepository clusterNodeRepository;
    @Autowired private ClusterConfigRepository clusterConfigRepository;
    @Autowired private ClusterInfo clusterInfo;
    @Autowired private NodeClient nodeClient;
    @Autowired private NodeProperties nodeProperties;

    /**
     * @param
     * @return
     * @desc join consensus layer
     */
    public void joinConsensus() {

        log.info("[joinConsensus] start to join consensus layer");
        consensusStateMachine.joinConsensus();

        log.info("[joinConsensus] start to transform node status from offline to running");
        nodeState.changeState(NodeStateEnum.Offline, NodeStateEnum.SelfChecking);
        nodeState.changeState(NodeStateEnum.SelfChecking, NodeStateEnum.AutoSync);
        nodeState.changeState(NodeStateEnum.AutoSync, NodeStateEnum.Running);

        log.info("[joinConsensus] start to update cluster config info");
        sendJoinToEveryNode(nodeProperties.getStartupRetryTime(), nodeState.getNodeName());

        log.info("[joinConsensus] end join consensus layer and transform node status");

    }

    /**
     * @param
     * @return
     * @desc leave consensus layer
     */
    public void leaveConsensus() {

        log.info("[leaveConsensus] start to update cluster config info");
        sendLeaveToEveryNode(nodeProperties.getStartupRetryTime(), nodeState.getNodeName());

        log.info("[leaveConsensus] start to leave consensus layer");
        consensusStateMachine.leaveConsensus();

        log.info("[leaveConsensus] start to transform node status from running to offline");
        nodeState.changeState(NodeStateEnum.Running, NodeStateEnum.Offline);

        log.info("[leaveConsensus] end leave consensus layer and transform node status");

    }

    private void sendLeaveToEveryNode(int retryCount, String user) {
        List<String> nodeList = clusterInfo.clusterNodeNames();
        Set<String> nodeSet = new CopyOnWriteArraySet<>();
        int i = 0;
        do {
            if (nodeSet.size() == nodeList.size()) {
                return;
            }
            // send leave consensus to every node
            nodeList.forEach((nodeName) -> {
                try {
                    if (!nodeSet.contains(nodeName)) {
                        RespData<String> resp = nodeClient.nodeLeave(nodeName, user);
                        if (resp.isSuccess()) {
                            nodeSet.add(nodeName);
                        }
                    }
                } catch (Throwable e) {
                    log.warn("[sendLeaveToEveryNode] send leave consensus to every node error, node={}", nodeName);
                }
            });
            if (nodeSet.size() < nodeList.size()) {
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    log.warn("send leave consensus to every node error.", e);
                }
            }
        } while (nodeSet.size() < nodeList.size() && ++i < retryCount);

        if (nodeSet.size() < nodeList.size()) {
            log.info("[sendLeaveToEveryNode]  send leave consensus to every node error, nodeSet = {}, nodeList={}",
                nodeSet.toString(), nodeList.toString());
            throw new SlaveException(SlaveErrorEnum.SLAVE_LEAVE_CONSENSUS_ERROR,
                "[sendLeaveToEveryNode] send leave consensus to every node error");
        }

        clusterInfo.setRefresh();
        log.info("[sendLeaveToEveryNode] set cluster info refresh");

        log.info("[sendLeaveToEveryNode] end send leave consensus to every node, nodeSet = {}, nodeList={}",
            nodeSet.toString(), nodeList.toString());
    }

    private void sendJoinToEveryNode(int retryCount, String user) {
        List<String> nodeList = clusterInfo.clusterNodeNames();
        Set<String> nodeSet = new CopyOnWriteArraySet<>();
        int i = 0;
        do {
            if (nodeSet.size() == nodeList.size()) {
                return;
            }
            // send leave consensus to every node
            nodeList.forEach((nodeName) -> {
                try {
                    if (!nodeSet.contains(nodeName)) {
                        RespData<String> resp = nodeClient.nodeJoin(nodeName, user);
                        if (resp.isSuccess()) {
                            nodeSet.add(nodeName);
                        }
                    }
                } catch (Throwable e) {
                    log.warn("[sendJoinToEveryNode] send join consensus to every node error, node={}", nodeName);
                }
            });
            if (nodeSet.size() < nodeList.size()) {
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    log.warn("send join consensus to every node error.", e);
                }
            }
        } while (nodeSet.size() < nodeList.size() && ++i < retryCount);

        if (nodeSet.size() < nodeList.size()) {
            log.info("[sendJoinToEveryNode]  send join consensus to every node error, nodeSet = {}, nodeList={}",
                nodeSet.toString(), nodeList.toString());
            throw new SlaveException(SlaveErrorEnum.SLAVE_LEAVE_CONSENSUS_ERROR,
                "[sendJoinToEveryNode] send join consensus to every node error");
        }

        clusterInfo.setRefresh();
        log.info("[sendJoinToEveryNode] set cluster info refresh");

        log.info("[sendJoinToEveryNode] end send join consensus to every node, nodeSet = {}, nodeList={}",
            nodeSet.toString(), nodeList.toString());
    }

    public RespData updateConfigForJoin(String user) {
        // every node update table cluster_node and cluster_config
        if (log.isDebugEnabled()) {
            log.debug("[updateConfigForJoin] start to update clusterNode info");
        }
        ClusterNode clusterNode = new ClusterNode();
        clusterNode.setNodeName(user);
        clusterNode.setP2pStatus(true);
        clusterNode.setRsStatus(false);
        clusterNodeRepository.insertClusterNode(clusterNode);
        if (log.isDebugEnabled()) {
            log.debug("[updateConfigForJoin] clusterNode={}", clusterNode.toString());
        }

        if (log.isDebugEnabled()) {
            log.debug("[updateConfigForJoin] start to update clusterConfig info");
        }
        ClusterConfig clusterConfig = clusterConfigRepository.getClusterConfig(nodeState.getClusterName());
        clusterConfig.setNodeNum(clusterConfig.getNodeNum() + 1);
        clusterConfig.setFaultNum(clusterConfig.getNodeNum() / 3);
        clusterConfigRepository.insertClusterConfig(clusterConfig);
        if (log.isDebugEnabled()) {
            log.debug("[updateConfigForJoin] clusterConfig={}", clusterConfig.toString());
        }
        return new RespData();
    }

    public RespData updateConfigForLeave(String user) {
        // every node update table cluster_node and cluster_config
        if (log.isDebugEnabled()) {
            log.debug("[updateConfigForLeave] start to update clusterNode info");
        }
        ClusterNode clusterNode = new ClusterNode();
        clusterNode.setNodeName(user);
        clusterNode.setP2pStatus(false);
        clusterNode.setRsStatus(false);
        clusterNodeRepository.insertClusterNode(clusterNode);
        if (log.isDebugEnabled()) {
            log.debug("[updateConfigForLeave] clusterNode={}", clusterNode.toString());
        }

        if (log.isDebugEnabled()) {
            log.debug("[updateConfigForLeave] start to update clusterConfig info");
        }
        ClusterConfig clusterConfig = clusterConfigRepository.getClusterConfig(nodeState.getClusterName());
        clusterConfig.setNodeNum(clusterConfig.getNodeNum() - 1);
        clusterConfig.setFaultNum((clusterConfig.getNodeNum() - 1) / 3);
        clusterConfigRepository.insertClusterConfig(clusterConfig);
        if (log.isDebugEnabled()) {
            log.debug("[updateConfigForLeave] clusterConfig={}", clusterConfig.toString());
        }
        return new RespData();
    }

}
