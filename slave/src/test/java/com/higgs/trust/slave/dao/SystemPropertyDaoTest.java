package com.higgs.trust.slave.dao;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.dao.config.SystemPropertyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * SystemPropertyDao test
 *
 * @author lingchao
 * @create 2018年09月17日11:11
 */
public class SystemPropertyDaoTest extends BaseTest {

    @Autowired
    private SystemPropertyDao systemPropertyDao;

    @Test
    public void update(){

        System.out.println(systemPropertyDao.update("CHAIN_OWNER", "lingchao", null));
        System.out.println(systemPropertyDao.update("CHAIN_OWNER", "lingao", "0000"));
    }

}
