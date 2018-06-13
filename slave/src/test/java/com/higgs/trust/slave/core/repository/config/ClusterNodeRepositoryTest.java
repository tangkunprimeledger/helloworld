package com.higgs.trust.slave.core.repository.config;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.dao.po.config.ClusterNodePO;
import org.apache.catalina.LifecycleState;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;

import static org.testng.Assert.*;

public class ClusterNodeRepositoryTest extends BaseTest{
    @Autowired private ClusterNodeRepository clusterNodeRepository;

    @Test public void testInsertClusterNode() throws Exception {
    }

    @Test public void testUpdateClusterNode() throws Exception {
    }

    @Test public void testGetClusterNode() throws Exception {
    }

    @Test public void testGetNodeNum() throws Exception {
    }

    @Test public void testBatchInsert() throws Exception {
        List list = new LinkedList();
        for (int i =0;i<5;i++){
            ClusterNodePO clusterNodePO = new ClusterNodePO();
            clusterNodePO.setNodeName("wqz"+i);
            clusterNodePO.setP2pStatus(true);
            clusterNodePO.setRsStatus(false);
            list.add(clusterNodePO);
        }
        clusterNodeRepository.batchInsert(list);
    }

    @Test public void testBatchUpdate() throws Exception {
        List list = new LinkedList();
        for (int i =0;i<2;i++){
            ClusterNodePO clusterNodePO = new ClusterNodePO();
            clusterNodePO.setNodeName("wqz"+i);
            clusterNodePO.setRsStatus(true);
            list.add(clusterNodePO);
        }
        clusterNodeRepository.batchUpdate(list);
    }
}