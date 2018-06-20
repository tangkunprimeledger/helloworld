package com.higgs.trust.slave.dao.dataIdentity;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.dao.po.DataIdentityPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author lingchao
 * @create 2018年06月20日17:30
 */
public class DataIdentityJDBCDaoTest extends BaseTest {
    @Autowired
    private DataIdentityJDBCDao dataIdentityJDBCDao;


    @Test public void batchInsertTest() {
        List<DataIdentityPO> dataIdentityList = new ArrayList<>();
        for (int i=0 ;i<5; i++) {
            DataIdentityPO dataIdentityPO = new DataIdentityPO();
            dataIdentityPO.setIdentity("123wew123" + System.currentTimeMillis()+new Random().nextInt());
            dataIdentityPO.setDataOwner("wangxinlicai-rsid");
            dataIdentityPO.setChainOwner("bitUn");
            dataIdentityList.add(dataIdentityPO);
        }
        System.out.println("batchInsertTest：" + dataIdentityJDBCDao.batchInsert(dataIdentityList));
    }
}
