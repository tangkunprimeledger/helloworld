package com.higgs.trust.rs.custom.api.bill;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.rs.custom.vo.BillCreateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.math.BigDecimal;

public class BillServiceTest extends IntegrateBaseTest {
    @Autowired
    private BillService billService;
    @Test
    public void testCreate() throws Exception {
        BillCreateVO billCreateVO = new BillCreateVO();
        billCreateVO.setAmount(new BigDecimal("1231231"));
        billCreateVO.setBillId("12312312312");
        JSONObject bizModel = new JSONObject();
        bizModel.put("amount", 0);
        billCreateVO.setBizModel(JSON.toJSONString(bizModel));
        billCreateVO.setDueDate("qw3eqweqw");
        billCreateVO.setFinalPayerId("q23eqwewq");
        billCreateVO.setHolder("lingchao");
        billCreateVO.setRequestId("lingchaoddd");
        billService.create(billCreateVO);


    }

    public void testTransfer() throws Exception {
    }

}