package com.higgs.trust.slave.core.repository;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.dao.rocks.transaction.TransactionRocksDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

public class TransactionRepositoryTest extends BaseTest {
    @Autowired
    private TransactionRocksDao transactionRocksDao;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test public void testQueryTxIds() throws Exception {
        for (int i = 0; i < 1000; i++) {
//            List<String> txIds = transactionRocksDao.queryKeysByPrefix("tfs", 100);
//            for (int j = 0; j < 100; j++){
//                txIds.add("tfs-test-tx-id-" + System.currentTimeMillis());
//            }
//
//            long begin = System.currentTimeMillis();
//            List<String> resultTxIds1 = transactionRepository.queryTxIdsByIds(txIds);
//            System.out.println("multiGet: " + (System.currentTimeMillis() - begin));
//
//            List<String> resultTxIds2 = new ArrayList<>();
//            long begin2 = System.currentTimeMillis();
//            for (String txId : txIds) {
//                if (transactionRocksDao.keyMayExist(txId) && null !=transactionRocksDao.get(txId)) {
//                    resultTxIds2.add(txId);
//                }
//            }
//            System.out.println("keyMayExist: " + (System.currentTimeMillis() - begin2));
        }
    }

    @Test public void testQueryTxIdsByIds() throws Exception {
    }
}