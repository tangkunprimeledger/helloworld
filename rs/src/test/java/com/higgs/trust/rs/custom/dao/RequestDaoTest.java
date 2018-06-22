package com.higgs.trust.rs.custom.dao;

import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.rs.core.dao.RequestDao;
import com.higgs.trust.rs.custom.api.enums.RequestEnum;
import com.higgs.trust.rs.core.dao.po.RequestPO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * RequestDao test
 *
 * @author lingchao
 * @create 2018年05月14日14:21
 */
public class RequestDaoTest extends IntegrateBaseTest {
    @Autowired
    private RequestDao requestDao;
    @Test
   public void testAdd(){
        RequestPO requestPO = new RequestPO();
        requestPO.setStatus(RequestEnum.PROCESS.getCode());
        requestPO.setRespMsg("2312");
        requestPO.setRespCode("123123");
        requestPO.setRequestId("123123123");
        requestPO.setData("sdads");
        System.out.println(requestDao.add(requestPO));
    }

    @Test
    public void testUpdate(){
        RequestPO requestPO = new RequestPO();
        requestPO.setStatus(RequestEnum.PROCESS.getCode());
        requestPO.setRespMsg("2312");
        requestPO.setRespCode("123123");
        requestPO.setRequestId("123123123");
        requestPO.setData("sdads");
        System.out.println(requestDao.updateStatusByRequestId("123123123",RequestEnum.PROCESS.getCode(), RequestEnum.DONE.getCode(),"1231231","ok"));
    }

    @Test
    public void testQueryByRequestId(){
        System.out.println(requestDao.queryByRequestId("123123123"));
    }

}
