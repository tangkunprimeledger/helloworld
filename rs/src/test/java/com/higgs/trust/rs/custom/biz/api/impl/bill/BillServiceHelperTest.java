package com.higgs.trust.rs.custom.biz.api.impl.bill;

import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.rs.custom.vo.BillCreateVO;
import com.higgs.trust.rs.custom.vo.BillTransferVO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

public class BillServiceHelperTest extends IntegrateBaseTest {
    @Autowired
    private BillServiceHelper billServiceHelper;

    @Test
    public void requestIdempotentTest() {
        System.out.println("-------------------------" + billServiceHelper.requestIdempotent("123123123"));
        System.out.println("--------------no requestId-----------" + billServiceHelper.requestIdempotent("1231233"));
    }

    @Test
    public void insertRequestTest() {

        BillCreateVO billCreateVO = new BillCreateVO();
        billCreateVO.setAmount(new BigDecimal("1231231"));
        billCreateVO.setBillId("12312312312");
        billCreateVO.setBizModel("23231231");
        billCreateVO.setDueDate("qw3eqweqw");
        billCreateVO.setFinalPayerId("q23eqwewq");
        billCreateVO.setHolder("lingchao");
        billCreateVO.setRequestId("lingchaoddd");

        System.out.println("-------------------------" + billServiceHelper.insertRequest(billCreateVO));
        System.out.println("-------------------------" + billServiceHelper.insertRequest(billCreateVO));
    }


    @Test
    public void insertBillTestCreate() {

        BillCreateVO billCreateVO = new BillCreateVO();
        billCreateVO.setAmount(new BigDecimal("1231231"));
        billCreateVO.setBillId("12312312312");
        billCreateVO.setBizModel("23231231");
        billCreateVO.setDueDate("qw3eqweqw");
        billCreateVO.setFinalPayerId("q23eqwewq");
        billCreateVO.setHolder("lingchao");
        billCreateVO.setRequestId("lingchaoddd");

        billServiceHelper.insertBill(billCreateVO, 0L, 0L);
        billServiceHelper.insertBill(billCreateVO, 0L, 0L);
    }


    @Test
    public void insertRequestTest2() {

        BillTransferVO billTransferVO = new BillTransferVO();
        billTransferVO.setBillId("1231dddd2");
        billTransferVO.setBizModel("12312");
        billTransferVO.setNextHolder("lingchao");
        billTransferVO.setRequestId("132123dfscsdwada");

        System.out.println("-------------------------" + billServiceHelper.insertRequest(billTransferVO));
        System.out.println("-------------------------" + billServiceHelper.insertRequest(billTransferVO));
    }

    @Test
    public void insertBillTestTransfer() {

        BillTransferVO billTransferVO = new BillTransferVO();
        billTransferVO.setBillId("12345");
        billTransferVO.setBizModel("12312");
        billTransferVO.setNextHolder("lingchao");
        billTransferVO.setRequestId("132123dfscsdwada");


        billServiceHelper.insertBill(billTransferVO, 0L, 0L);
        billServiceHelper.insertBill(billTransferVO, 0L, 0L);
    }

}