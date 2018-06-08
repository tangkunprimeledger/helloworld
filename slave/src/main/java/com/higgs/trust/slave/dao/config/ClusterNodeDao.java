package com.higgs.trust.slave.dao.config;

import com.higgs.trust.slave.dao.po.config.ClusterNodePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/5 14:17
 */
@Mapper public interface ClusterNodeDao {

    /**
     * @param clusterNodePO
     * @return
     * @desc insert clusterNode information into db
     */
    void insertClusterNode(ClusterNodePO clusterNodePO);

    /**
     * @param clusterNodePO
     * @return
     * @desc update clusterNode
     */
    void updateClusterNode(ClusterNodePO clusterNodePO);

    /**
     * @param nodeName
     * @return ClusterConfigPO
     * @desc get clusterNode by node name
     */
    ClusterNodePO getClusterNode(String nodeName);

    /** 
     * @desc acquire node num
     * @param
     * @return   
     */  
    int getNodeNum();
}
