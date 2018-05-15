package com.higgs.trust.rs.custom.controller.outter.v1;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.OkHttpClientManager;
import com.higgs.trust.rs.custom.vo.BillCreateVO;
import com.higgs.trust.rs.custom.vo.BillTransferVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Random;

@Slf4j
public class BillControllerTest {

    private static final String url = "http://10.200.172.98:7070/bill/";

    @Test
    public void testCreate() throws Exception {

        BillCreateVO billCreateVO = new BillCreateVO();
        billCreateVO.setAmount(new BigDecimal("100000000000000"));
        billCreateVO.setBillId("1234567890");
        billCreateVO.setBizModel("23231231");
        billCreateVO.setDueDate("2018-05-15 03:25:00");
        billCreateVO.setFinalPayerId("chao");
        billCreateVO.setHolder("ling");
        billCreateVO.setRequestId("lingchao1234567");

        String params = JSON.toJSONString(billCreateVO);

        System.out.println("request.params:" + params);

        String res = OkHttpClientManager.postAsString(url + "create", params);

        System.out.println("res.data:" + res);
    }


    @Test
    public void testTransfer() throws Exception {

            BillTransferVO billTransferVO = new BillTransferVO();
            billTransferVO.setBillId("1234567890");
            billTransferVO.setBizModel("dasdasdas");
            billTransferVO.setNextHolder("chaoguo");
            billTransferVO.setRequestId("billTransfer" + System.currentTimeMillis());

            String params = JSON.toJSONString(billTransferVO);

            //  System.out.println("request.params:" + params);
            long start = System.currentTimeMillis();
            String res = OkHttpClientManager.postAsString(url + "transfer", params);
            long end = System.currentTimeMillis();
            log.info("total cost : {} ms", end - start);
            log.info("res.data:{}", res);



    }


}