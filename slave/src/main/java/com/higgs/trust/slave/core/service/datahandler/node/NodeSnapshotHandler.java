package com.higgs.trust.slave.core.service.datahandler.node;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.slave.core.service.snapshot.agent.CaSnapshotAgent;
import com.higgs.trust.slave.dao.po.config.ClusterConfigPO;
import com.higgs.trust.slave.dao.po.config.ClusterNodePO;
import com.higgs.trust.slave.model.bo.config.ClusterConfig;
import com.higgs.trust.slave.model.bo.config.ClusterNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/6 10:54
 */
@Service @Slf4j public class NodeSnapshotHandler implements NodeHandler {

    @Autowired private CaSnapshotAgent caSnapshotAgent;
    @Autowired private NodeState nodeState;

    @Override public void nodeJoin(ClusterNode clusterNode) {
        clusterNode.setP2pStatus(true);
        clusterNode.setRsStatus(false);
        if (log.isDebugEnabled()) {
            log.debug("[nodeJoin] start to update clusterNodeInfo, clusterNode={}", JSON.toJSONString(clusterNode));
        }

        ClusterNodePO clusterNodeCache = caSnapshotAgent.getClusterNode(clusterNode.getNodeName());

        if (null == clusterNodeCache) {
            caSnapshotAgent.saveClusterNode(clusterNode);
        } else {
            caSnapshotAgent.updateClusterNode(clusterNode);
        }

        if (null == clusterNodeCache || clusterNodeCache.isP2pStatus() == false) {
            ClusterConfigPO clusterConfigPO = caSnapshotAgent.getClusterConfig(nodeState.getClusterName());
            ClusterConfig clusterConfig = new ClusterConfig();
            clusterConfig.setClusterName(clusterConfigPO.getClusterName());
            clusterConfig.setNodeNum(clusterConfigPO.getNodeNum() + 1);
            clusterConfig.setFaultNum(clusterConfigPO.getNodeNum() / 3);
            if (log.isDebugEnabled()) {
                log.debug("[nodeJoin] start to update clusterConfigInfo, clusterConfig={}",
                    JSON.toJSONString(clusterConfig));
            }
            caSnapshotAgent.updateClusterConfig(clusterConfig);
        }

    }

    @Override public void nodeLeave(ClusterNode clusterNode) {

        ClusterNodePO clusterNodeCache = caSnapshotAgent.getClusterNode(clusterNode.getNodeName());

        if (log.isDebugEnabled()) {
            log.debug("[nodeLeave] start to update clusterNodeInfo, clusterNodeCache={}",
                JSON.toJSONString(clusterNodeCache));
        }

        if (clusterNodeCache.isP2pStatus() == true) {
            clusterNode.setP2pStatus(false);
            clusterNode.setRsStatus(false);
            if (log.isDebugEnabled()) {
                log.debug("[nodeLeave] start to update clusterNodeInfo, clusterNode={}",
                    JSON.toJSONString(clusterNode));
            }
            caSnapshotAgent.updateClusterNode(clusterNode);

            ClusterConfigPO clusterConfigPO = caSnapshotAgent.getClusterConfig(nodeState.getClusterName());
            ClusterConfig clusterConfig = new ClusterConfig();
            clusterConfig.setClusterName(clusterConfigPO.getClusterName());
            clusterConfig.setNodeNum(clusterConfigPO.getNodeNum() - 1);
            clusterConfig.setFaultNum((clusterConfig.getNodeNum() - 1) / 3);
            if (log.isDebugEnabled()) {
                log.debug("[nodeLeave] start to update clusterConfigInfo, clusterConfig={}",
                    JSON.toJSONString(clusterConfig));
            }
            caSnapshotAgent.updateClusterConfig(clusterConfig);
        }

    }
}
