package com.higgs.trust.rs.custom.controller.outter.v1;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.higgs.trust.common.utils.OkHttpClientManager;
import com.higgs.trust.rs.custom.vo.BillCreateVO;
import com.higgs.trust.rs.custom.vo.BillTransferVO;
import com.higgs.trust.rs.custom.vo.TransferDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.math.BigDecimal;

@Slf4j
public class BillControllerTest {

    private static final String url = "http://10.200.173.33:7070/bill/";

    @Test
    public void testCreate() throws Exception {

        BillCreateVO billCreateVO = new BillCreateVO();
        billCreateVO.setAmount(new BigDecimal("100000000000000"));
        billCreateVO.setBillId("1234567893444530");
        billCreateVO.setBizModel("23231231");
        billCreateVO.setDueDate("2018-05-15 03:25:00");
        billCreateVO.setFinalPayerId("chao");
        billCreateVO.setHolder("ling");
        billCreateVO.setRequestId("lingchao1234567" + System.currentTimeMillis());

        String params = JSON.toJSONString(billCreateVO);

        System.out.println("request.params:" + params);

        String res = OkHttpClientManager.postAsString(url + "create", params);

        System.out.println("res.data:" + res);
    }


    @Test
    public void testTransfer() throws Exception {

        BillTransferVO billTransferVO = new BillTransferVO();
        billTransferVO.setBillId("1234567893444530");
        billTransferVO.setHolder("ling");
        billTransferVO.setBizModel("dasdasdas");
        billTransferVO.setRequestId("billTransfer" + System.currentTimeMillis());
        billTransferVO.setTransferList(Lists.newArrayList());

        TransferDetailVO transferDetailVO = new TransferDetailVO();
        transferDetailVO.setNextHolder("chao1");
        transferDetailVO.setAmount(new BigDecimal("40000000000000"));
        billTransferVO.getTransferList().add(transferDetailVO);

        transferDetailVO = new TransferDetailVO();
        transferDetailVO.setNextHolder("chao2");
        transferDetailVO.setAmount(new BigDecimal("60000000000000"));
        billTransferVO.getTransferList().add(transferDetailVO);

        String params = JSON.toJSONString(billTransferVO);

          System.out.println("request.params:" + params);
        long start = System.currentTimeMillis();
      //  String res = OkHttpClientManager.postAsString(url + "transfer", params);
        long end = System.currentTimeMillis();
        log.info("total cost : {} ms", end - start);
     //   log.info("res.data:{}", res);


    }


}