package com.higgs.trust.rs.custom.controller.outter.v1;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.OkHttpClientManager;
import com.higgs.trust.rs.custom.vo.BillCreateVO;
import com.higgs.trust.rs.custom.vo.BillTransferVO;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Random;

public class BillControllerTest {


    @Test
    public void testCreate() throws Exception {
        String url = "http://127.0.0.1:7070/create";
        BillCreateVO billCreateVO = new BillCreateVO();
        billCreateVO.setAmount(new BigDecimal("1231231"));
        billCreateVO.setBillId("12312312312123");
        billCreateVO.setBizModel("23231231");
        billCreateVO.setDueDate("qw3eqweqw");
        billCreateVO.setFinalPayerId("q23eqwewq");
        billCreateVO.setHolder("lingchao");
        billCreateVO.setRequestId("lingchaoddddddwqeqw");

        String params = JSON.toJSONString(billCreateVO);

        System.out.println("request.params:" + params);

        String res = OkHttpClientManager.postAsString(url, params);

        System.out.println("res.data:" + res);
    }


    @Test
    public void testTransfer() throws Exception {
        String url = "http://127.0.0.1:7070/transfer";
        while (true){
            BillTransferVO billTransferVO = new BillTransferVO();
            billTransferVO.setBillId("12312312312");
            billTransferVO.setBizModel("12312");
            billTransferVO.setNextHolder("lingchao22");
            billTransferVO.setRequestId("132123dfscsdwadass"+System.currentTimeMillis()+ new Random());

            String params = JSON.toJSONString(billTransferVO);

            System.out.println("request.params:" + params);

            String res = OkHttpClientManager.postAsString(url, params);

            System.out.println("res.data:" + res);
        }

    }



}