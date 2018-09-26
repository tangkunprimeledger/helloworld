package com.higgs.trust.slave.dao.mysql.config;

import com.higgs.trust.slave.dao.po.config.ClusterNodePO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

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


    /**
     * batch insert
     *
     * @param clusterNodePOList
     * @return
     */
    int batchInsert(List<ClusterNodePO> clusterNodePOList);

    /**
     * batch update
     *
     * @param clusterNodePOList
     * @return
     */
    int batchUpdate(List<ClusterNodePO> clusterNodePOList);

    List<ClusterNodePO> getAllClusterNodes();
}
