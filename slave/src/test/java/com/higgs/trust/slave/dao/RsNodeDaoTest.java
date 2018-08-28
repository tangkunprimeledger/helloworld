package com.higgs.trust.slave.dao;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.dao.mysql.manage.RsNodeDao;
import com.higgs.trust.slave.dao.po.manage.RsNodePO;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * @author tangfashuang
 * @date 2018/04/13 22:19
 * @desc rs node dao test
 */
public class RsNodeDaoTest extends BaseTest {
    @Autowired RsNodeDao rsNodeDao;

    @Test public void testAdd1() {
        RsNodePO rsNodePO = new RsNodePO();
        rsNodePO.setRsId("rs-test1");
        rsNodePO.setDesc("rs-test1-desc");

        rsNodeDao.add(rsNodePO);
    }

    @Test public void testAdd2() {
        RsNodePO rsNodePO = new RsNodePO();
        rsNodePO.setRsId("rs-test2");
        rsNodePO.setDesc("rs-test2-desc");

        rsNodeDao.add(rsNodePO);
    }

    @Test public void testAdd3() {
        RsNodePO rsNodePO = new RsNodePO();
        rsNodePO.setRsId("rs-test3");
        rsNodePO.setDesc("rs-test3-desc");

        rsNodeDao.add(rsNodePO);
    }
}