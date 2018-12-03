package com.higgs.trust.rs.core.dao;

import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.rs.common.enums.RequestEnum;
import com.higgs.trust.rs.core.dao.po.RequestPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * @description: com.higgs.trust.rs.core.dao
 * @author: lingchao
 * @datetime:2018年12月03日14:32
 **/
public class RequestDaoTest extends IntegrateBaseTest {
    private String requestId = System.currentTimeMillis() + "";
    @Autowired
    private RequestDao requestDao;

    @Test
    public void add() {
        RequestPO requestPO = new RequestPO();
        requestPO.setRequestId(requestId);
        requestPO.setStatus(RequestEnum.PROCESS.getCode());
        requestPO.setRespCode("000009");
        requestPO.setRespMsg("INIT");
        System.out.println(requestDao.add(requestPO));
        System.out.println(requestDao.queryByRequestId(requestId));
        System.out.println(requestDao.updateStatusByRequestId(requestId,null, null, "000001", "PROCESS"));
        System.out.println(requestDao.queryByRequestId(requestId));

    }


    @Test
    public void testQueryByRequestId() {
        System.out.println(requestDao.queryByRequestId(requestId));
    }

    @Test
    public void testUpdateStatusByRequestId() {
        System.out.println(requestDao.updateStatusByRequestId(requestId, RequestEnum.PROCESS.getCode(), RequestEnum.DONE.getCode(), "000000", "OK"));
    }
}