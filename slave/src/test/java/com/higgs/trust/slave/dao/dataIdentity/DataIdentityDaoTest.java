package com.higgs.trust.slave.dao.dataIdentity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.dao.po.DataIdentityPO;
import com.higgs.trust.slave.model.bo.snapshot.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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


    @Test public void loadCache() throws InterruptedException {
        LoadingCache<Object, Object> bizCache = CacheBuilder.newBuilder().initialCapacity(10).maximumSize(5).refreshAfterWrite(1, TimeUnit.MICROSECONDS).build(new com.google.common.cache.CacheLoader<Object, Object>() {
            @Override
            public Object load(Object bo) throws Exception {
                return "12321"+new Random().nextInt();
            }
        });

        for(int i =0; i<20; i++){
            bizCache.put(i+"",i);
        }
        Thread.sleep(10000);
        System.out.println("================================================");
        for(Map.Entry<Object, Object> innerEntry : bizCache.asMap().entrySet()){
            System.out.println("Key="+innerEntry.getKey()+ "     value ="+ innerEntry.getValue());
        }
    }

}
