package com.higgs.trust.slave.core.repository.ca;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.model.bo.ca.Ca;
import com.higgs.trust.tester.dbunit.DataBaseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class CaRepositoryTest extends BaseTest {

    DataBaseManager dataBaseManager = new DataBaseManager();

    @Autowired private CaRepository caRepository;

    String url =
        "jdbc:mysql://localhost:3306/trust?user=root&password=root&useUnicode=true&characterEncoding=UTF8&allowMultiQueries=true&useAffectedRows=true";
    String sql = "truncate table ca;";

    @Test public void testInsertCa() throws Exception {
        Ca ca = new Ca();
        ca.setPeriod(new Date());
        ca.setPubKey("123");
        ca.setUsage("consensus");
        ca.setUser("wqz");
        ca.setValid(true);
        ca.setVersion(VersionEnum.V1.getCode());
        caRepository.insertCa(ca);
    }

    @Test public void testUpdateCa() throws Exception {
        Ca ca = new Ca();
        ca.setUser("wqz");
        ca.setPeriod(new Date());
        ca.setPubKey("456");
        caRepository.updateCa(ca);
    }

    @Test public void testGetCa() throws Exception {
        Ca ca = caRepository.getCaForBiz("wqz");
        System.out.println(ca.toString());
    }

    @Test public void testGetAllCa() throws Exception {
        List list = caRepository.getAllCa();
        System.out.println(list.toString());
    }

    @Test public void testBatchInsert() throws Exception {
        List list =new LinkedList();
        for (int i =10;i<11;i++){
            Ca ca = new Ca();
            ca.setPeriod(new Date());
            ca.setPubKey("test"+i);
            ca.setUsage("consensus");
            ca.setUser("lf");
            ca.setValid(true);
            ca.setVersion(VersionEnum.V1.getCode());
            list.add(ca);
        }
        caRepository.batchInsert(list);
        System.out.println("结束");
    }

    @Test public void testBatchUpdate() throws Exception {
        List list =new LinkedList();
        for (int i =0;i<1;i++){
            Ca ca = new Ca();
            ca.setPeriod(new Date());
            ca.setPubKey("test"+1000);
            ca.setUser("wqz");
            list.add(ca);
        }
        Ca ca = new Ca();
        ca.setPeriod(new Date());
        ca.setPubKey("test"+10000);
        ca.setUser("lf");
        list.add(ca);
        caRepository.batchUpdate(list);
        System.out.println("结束");
    }
}