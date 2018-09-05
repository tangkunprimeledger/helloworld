package com.higgs.trust.slave.core.repository.config;

import com.higgs.trust.common.dao.RocksUtils;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.common.config.InitConfig;
import com.higgs.trust.slave.dao.po.config.ClusterConfigPO;
import com.higgs.trust.slave.model.bo.config.ClusterConfig;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;

public class ClusterConfigRepositoryTest extends BaseTest{

    @Autowired private ClusterConfigRepository clusterConfigRepository;
    @Autowired private InitConfig initConfig;

    @Test public void testInsertClusterConfig() throws Exception {

    }

    @Test public void testGetClusterConfig() throws Exception {
       ClusterConfig clusterConfig =  clusterConfigRepository.getClusterConfig("TRUST");
       System.out.println(clusterConfig);
    }

    @Test public void testBatchInsert() throws Exception {
        List list = new LinkedList();
        for (int i = 0;i<3;i++){
            ClusterConfigPO clusterConfigPO = new ClusterConfigPO();
            clusterConfigPO.setClusterName("TRUST" + i);
            clusterConfigPO.setFaultNum(2 + i);
            clusterConfigPO.setNodeNum(5 + i);
            list.add(clusterConfigPO);
        }
        if (!initConfig.isUseMySQL()) {
            ThreadLocalUtils.putWriteBatch(new WriteBatch());
            clusterConfigRepository.batchInsert(list);
            RocksUtils.batchCommit(new WriteOptions(), ThreadLocalUtils.getWriteBatch());
            ThreadLocalUtils.clearWriteBatch();
        } else {
            clusterConfigRepository.batchInsert(list);
        }
        for (int i = 0; i < 3; i++) {
            ClusterConfig clusterConfig = clusterConfigRepository.getClusterConfig("TRUST" + i);
            System.out.println(clusterConfig);
        }

    }

    @Test public void testBatchUpdate() throws Exception {
        List list = new LinkedList();
        for (int i = 0;i<3;i++){
            ClusterConfigPO clusterConfigPO = new ClusterConfigPO();
            clusterConfigPO.setClusterName("TRUST" + i);
            clusterConfigPO.setFaultNum(1 + i);
            clusterConfigPO.setNodeNum(4 + i);
            list.add(clusterConfigPO);
        }

        if (!initConfig.isUseMySQL()) {
            ThreadLocalUtils.putWriteBatch(new WriteBatch());
            clusterConfigRepository.batchUpdate(list);
            RocksUtils.batchCommit(new WriteOptions(), ThreadLocalUtils.getWriteBatch());
            ThreadLocalUtils.clearWriteBatch();
        } else {
            clusterConfigRepository.batchUpdate(list);
        }


        for (int i = 0; i < 6; i++) {
            ClusterConfig clusterConfig = clusterConfigRepository.getClusterConfig("TRUST" + i);
            System.out.println(clusterConfig);
        }
    }
}