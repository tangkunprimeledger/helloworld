package com.higgs.trust.slave.dao.transaction;

import com.higgs.trust.slave.IntegrateBaseTest;
import com.higgs.trust.slave.core.repository.TransactionRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author liuyu
 * @description
 * @date 2018-05-10
 */
public class TransactionRepositoryTest extends IntegrateBaseTest {

    @Autowired TransactionRepository transactionRepository;

    @Test public void test() {
        transactionRepository.isExist("12345");
    }
}
