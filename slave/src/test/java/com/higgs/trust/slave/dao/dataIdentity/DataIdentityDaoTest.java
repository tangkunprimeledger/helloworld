package com.higgs.trust.slave.dao.dataIdentity;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.dao.DataIdentityDao;
import com.higgs.trust.slave.dao.po.DataIdentityPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.Date;

/**
 * DataIdentityDao test
 *
 * @author lingchao
 * @create 2018年03月31日23:55
 */
public class DataIdentityDaoTest extends BaseTest {
    @Autowired private DataIdentityDao dataIdentityDao;

    @Test
    public void queryByIdentityTest() {
        System.out.println("queryByIdentity:" + dataIdentityDao.queryByIdentity("12312312"));
    }

    @Test public void addTest() {
        DataIdentityPO dataIdentityPO = new DataIdentityPO();
        dataIdentityPO.setIdentity("123wew123"+new Date());
        dataIdentityPO.setDataOwner("wangxinlicai-rsid");
        dataIdentityPO.setChainOwner("bitUn");
        System.out.println("add：" + dataIdentityDao.add(dataIdentityPO));
    }

}
