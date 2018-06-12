package com.higgs.trust.slave.core.repository.config;

import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.config.ClusterNodeDao;
import com.higgs.trust.slave.dao.po.config.ClusterConfigPO;
import com.higgs.trust.slave.dao.po.config.ClusterNodePO;
import com.higgs.trust.slave.model.bo.config.ClusterNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.List;

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
        ClusterNodePO clusterNodePO = new ClusterNodePO();
        BeanUtils.copyProperties(clusterNode,clusterNodePO);
        clusterNodeDao.updateClusterNode(clusterNodePO);
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

    /**
     * batch insert
     *
     * @param clusterNodePOList
     * @return
     */
    public boolean batchInsert(List<ClusterNodePO> clusterNodePOList) {
        int affectRows = 0;
        try {
            affectRows = clusterNodeDao.batchInsert(clusterNodePOList);
        } catch (DuplicateKeyException e) {
            log.error("batch insert clusterNode fail, because there is DuplicateKeyException for clusterNodePOList:", clusterNodePOList);
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        }
        return affectRows == clusterNodePOList.size();
    }

    /**
     * batch update
     *
     * @param clusterNodePOList
     * @return
     */
    public boolean batchUpdate(List<ClusterNodePO> clusterNodePOList) {
        return clusterNodePOList.size() == clusterNodeDao.batchUpdate(clusterNodePOList);
    }
}
