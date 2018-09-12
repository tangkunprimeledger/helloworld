package com.higgs.trust.slave.core.repository.config;

import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.common.dao.RocksUtils;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.model.bo.config.SystemProperty;
import org.rocksdb.Transaction;
import org.rocksdb.WriteOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

public class SystemPropertyRepositoryTest extends BaseTest{
    @Autowired
    private SystemPropertyRepository systemPropertyRepository;

    @Test public void testQueryByKey() throws Exception {
        SystemProperty bo = systemPropertyRepository.queryByKey(Constant.MAX_BLOCK_HEIGHT);
        System.out.println(bo);
    }

    @Test public void testAdd() throws Exception {
        systemPropertyRepository.add(Constant.MAX_BLOCK_HEIGHT, "1", "max block height");
        SystemProperty bo = systemPropertyRepository.queryByKey(Constant.MAX_BLOCK_HEIGHT);
        System.out.println(bo);
    }

    @Test public void testUpdate() throws Exception {
    }

    @Test public void testSaveWithTransaction() throws Exception {
        Transaction tx = RocksUtils.beginTransaction(new WriteOptions());
        try {
            ThreadLocalUtils.putRocksTx(tx);

            systemPropertyRepository.saveWithTransaction(Constant.MAX_PACK_HEIGHT, "1", "max package height");

            RocksUtils.txCommit(tx);
        } finally {
            ThreadLocalUtils.clearRocksTx();;
        }

        SystemProperty bo = systemPropertyRepository.queryByKey(Constant.MAX_PACK_HEIGHT);
        System.out.println(bo);
    }
}