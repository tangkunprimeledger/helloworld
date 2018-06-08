package com.higgs.trust.slave.core.repository.config;

import com.higgs.trust.slave.dao.config.ClusterNodeDao;
import com.higgs.trust.slave.dao.po.config.ClusterNodePO;
import com.higgs.trust.slave.model.bo.config.ClusterNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/5 16:11
 */
@Repository @Slf4j public class ClusterNodeRepository {

    @Autowired private ClusterNodeDao clusterNodeDao;

    /**
     * @param clusterNode
     * @return
     * @desc insert clusterNode information into db
     */
    public void insertClusterNode(ClusterNode clusterNode) {
        ClusterNodePO clusterNodePO = new ClusterNodePO();
        BeanUtils.copyProperties(clusterNode, clusterNodePO);
        clusterNodeDao.insertClusterNode(clusterNodePO);
    }

    /**
     * @param clusterNode
     * @return
     * @desc update clusterNode
     */
    public void updateClusterNode(ClusterNode clusterNode) {

    }

    /**
     * @param nodeName
     * @return ClusterConfigPO
     * @desc get clusterNode by node name
     */
    public ClusterNode getClusterNode(String nodeName) {
        ClusterNode clusterNode = new ClusterNode();
        ClusterNodePO clusterNodePO = new ClusterNodePO();
        clusterNodePO.setNodeName(nodeName);
        clusterNodePO = clusterNodeDao.getClusterNode(nodeName);
        BeanUtils.copyProperties(clusterNodePO,clusterNode);
        return clusterNode;
    }

    public int getNodeNum() {
        return clusterNodeDao.getNodeNum();
    }
}
