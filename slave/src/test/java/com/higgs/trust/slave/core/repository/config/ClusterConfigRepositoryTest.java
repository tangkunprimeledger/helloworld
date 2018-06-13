package com.higgs.trust.slave.core.repository.config;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.dao.po.config.ClusterConfigPO;
import com.higgs.trust.slave.model.bo.config.ClusterConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.prefs.BackingStoreException;

import static org.testng.Assert.*;

public class ClusterConfigRepositoryTest extends BaseTest{

    @Autowired private ClusterConfigRepository clusterConfigRepository;
    @Test public void testInsertClusterConfig() throws Exception {
    }

    @Test public void testUpdateClusterConfig() throws Exception {
    }

    @Test public void testGetClusterConfig() throws Exception {
    }

    @Test public void testBatchInsert() throws Exception {
        List list = new LinkedList();
        for (int i = 0;i<1;i++){
            ClusterConfigPO clusterConfigPO = new ClusterConfigPO();
            clusterConfigPO.setClusterName("TRUST");
            clusterConfigPO.setFaultNum(5);
            clusterConfigPO.setNodeNum(16);
            list.add(clusterConfigPO);
        }
        clusterConfigRepository.batchInsert(list);
    }

    @Test public void testBatchUpdate() throws Exception {
        List list = new LinkedList();
        for (int i = 0;i<1;i++){
            ClusterConfigPO clusterConfigPO = new ClusterConfigPO();
            clusterConfigPO.setClusterName("TRUST");
            clusterConfigPO.setFaultNum(1);
            clusterConfigPO.setNodeNum(4);
            list.add(clusterConfigPO);
        }
        clusterConfigRepository.batchUpdate(list);
    }
}