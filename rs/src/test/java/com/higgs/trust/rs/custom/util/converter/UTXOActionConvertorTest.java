package com.higgs.trust.rs.custom.util.converter;

import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.rs.custom.vo.BillCreateVO;
import com.higgs.trust.rs.custom.vo.BillTransferVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.math.BigDecimal;

public class UTXOActionConvertorTest extends IntegrateBaseTest {
    @Autowired
    private UTXOActionConvertor utxoActionConvertor;

    @Test
    public void buildCreateBillWithIdentityActionList(){
        BillCreateVO billCreateVO = new BillCreateVO();
        billCreateVO.setAmount(new BigDecimal("1231231"));
        billCreateVO.setBillId("12312312312");
        billCreateVO.setBizModel("{\"@type\":\"com.alibaba.fastjson.JSONObject\",\"amount\":1231231.,\"billId\":\"12312312312\",\"dueDate\":\"qw3eqweqw\",\"finalPayerId\":\"q23eqwewq\"}");
        billCreateVO.setDueDate("qw3eqweqw");
        billCreateVO.setFinalPayerId("q23eqwewq");
        billCreateVO.setHolder("lingchao");
        billCreateVO.setRequestId("lingchaoddd");

        System.out.println("----------buildCreateBillWithIdentityActionList-------------------"+utxoActionConvertor.buildCreateBillWithIdentityActionList(billCreateVO));
    }


    @Test
    public void buildCreateBillActionList(){
        BillCreateVO billCreateVO = new BillCreateVO();
        billCreateVO.setAmount(new BigDecimal("1231231"));
        billCreateVO.setBillId("12312312312");
        billCreateVO.setBizModel("23231231");
        billCreateVO.setDueDate("qw3eqweqw");
        billCreateVO.setFinalPayerId("q23eqwewq");
        billCreateVO.setHolder("lingchao");
        billCreateVO.setRequestId("lingchaoddd");

        System.out.println("----------buildCreateBillActionList-------------------"+utxoActionConvertor.buildCreateBillActionList(billCreateVO));
    }


    @Test
    public void buildTransferBillWithIdentityActionList(){
        BillTransferVO billTransferVO = new BillTransferVO();
        billTransferVO.setBillId("12312312312");
        billTransferVO.setBizModel("12312");
        billTransferVO.setNextHolder("lingchao");
        billTransferVO.setRequestId("132123dfscsdwada");

        System.out.println("----------buildTransferBillWithIdentityActionList-------------------"+utxoActionConvertor.buildTransferBillWithIdentityActionList(billTransferVO));
    }


    @Test
    public void buildTransferBillActionList(){
        BillTransferVO billTransferVO = new BillTransferVO();
        billTransferVO.setBillId("12312312312");
        billTransferVO.setBizModel("12312");
        billTransferVO.setNextHolder("lingchao");
        billTransferVO.setRequestId("132123dfscsdwada");

        System.out.println("----------buildTransferBillWithIdentityActionList-------------------"+utxoActionConvertor.buildTransferBillActionList(billTransferVO));
    }

}