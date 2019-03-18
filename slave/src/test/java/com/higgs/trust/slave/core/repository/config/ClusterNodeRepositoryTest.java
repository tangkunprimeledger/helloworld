package com.higgs.trust.slave.core.repository.config;

import com.higgs.trust.common.dao.RocksUtils;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.common.config.InitConfig;
import com.higgs.trust.slave.dao.po.config.ClusterNodePO;
import com.higgs.trust.slave.model.bo.config.ClusterNode;
import org.rocksdb.Transaction;
import org.rocksdb.WriteOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;

public class ClusterNodeRepositoryTest extends BaseTest{
    @Autowired private ClusterNodeRepository clusterNodeRepository;
    @Autowired private InitConfig initConfig;

    @Test public void testInsertClusterNode() throws Exception {
        ClusterNode clusterNode = new ClusterNode();
        clusterNode.setNodeName("TRUST-TEST-TEST");
        clusterNode.setP2pStatus(true);
        clusterNode.setRsStatus(false);

        Transaction tx = RocksUtils.beginTransaction(new WriteOptions());
        ThreadLocalUtils.putRocksTx(tx);
        clusterNodeRepository.insertClusterNode(clusterNode);
        RocksUtils.txCommit(tx);
        ThreadLocalUtils.clearRocksTx();

        List<ClusterNode> clusterNodes = clusterNodeRepository.getAllClusterNodes();
        System.out.println(clusterNode);

    }

    @Test public void testGetClusterNode() throws Exception {
        ClusterNode clusterNode = clusterNodeRepository.getClusterNode("TRUST-TEST-TEST");
        System.out.println(clusterNode);
    }

    @Test public void testBatchInsert() throws Exception {
        List list = new LinkedList();
        for (int i =0;i<5;i++){
            ClusterNodePO clusterNodePO = new ClusterNodePO();
            clusterNodePO.setNodeName("TRUST"+i);
            clusterNodePO.setP2pStatus(false);
            clusterNodePO.setRsStatus(false);
            list.add(clusterNodePO);
        }
        if (!initConfig.isUseMySQL()) {
            try {
                Transaction tx = RocksUtils.beginTransaction(new WriteOptions());
                ThreadLocalUtils.putRocksTx(tx);
                clusterNodeRepository.batchInsert(list);
                RocksUtils.txCommit(tx);
            } finally {
                ThreadLocalUtils.clearRocksTx();
            }
        } else {
            clusterNodeRepository.batchInsert(list);
        }


        List<ClusterNode> clusterNodes = clusterNodeRepository.getAllClusterNodes();
        System.out.println(clusterNodes);
    }

    @Test public void testBatchUpdate() throws Exception {
        List list = new LinkedList();
        for (int i =0;i<2;i++){
            ClusterNodePO clusterNodePO = new ClusterNodePO();
            clusterNodePO.setNodeName("TRUST"+i);
            clusterNodePO.setRsStatus(false);
            list.add(clusterNodePO);
        }
        if (initConfig.isUseMySQL()) {
            clusterNodeRepository.batchUpdate(list);
        } else {
            try {
                Transaction tx = RocksUtils.beginTransaction(new WriteOptions());
                ThreadLocalUtils.putRocksTx(tx);
                clusterNodeRepository.batchUpdate(list);
                RocksUtils.txCommit(tx);
            } finally {
                ThreadLocalUtils.clearRocksTx();
            }
        }

        List<ClusterNode> clusterNodes = clusterNodeRepository.getAllClusterNodes();
        System.out.println(clusterNodes);
    }
}