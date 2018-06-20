package com.higgs.trust.slave.dao.dataIdentity;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.dao.po.DataIdentityPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

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

    @Test public void batchInsertTest() {
        List<DataIdentityPO> dataIdentityList = new ArrayList<>();
        for (int i=0 ;i<5; i++) {
            DataIdentityPO dataIdentityPO = new DataIdentityPO();
            dataIdentityPO.setIdentity("123wew123" + System.currentTimeMillis()+new Random().nextInt());
            dataIdentityPO.setDataOwner("wangxinlicai-rsid");
            dataIdentityPO.setChainOwner("bitUn");
            dataIdentityList.add(dataIdentityPO);
        }
        System.out.println("batchInsertTest：" + dataIdentityDao.batchInsert(dataIdentityList));
    }

}
