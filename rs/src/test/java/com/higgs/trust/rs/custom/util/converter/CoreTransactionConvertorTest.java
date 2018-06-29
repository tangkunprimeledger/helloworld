package com.higgs.trust.rs.custom.util.converter;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.rs.common.utils.CoreTransactionConvertor;
import com.higgs.trust.rs.custom.model.BizTypeConst;
import com.higgs.trust.rs.custom.vo.BillCreateVO;
import com.higgs.trust.slave.model.bo.action.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.List;

public class CoreTransactionConvertorTest extends IntegrateBaseTest {
    @Autowired
    private CoreTransactionConvertor coreTransactionConvertor;
    @Autowired
    private UTXOActionConvertor utxoActionConvertor;

    @Test
    public void testBuildBillCoreTransaction() throws Exception {

        BillCreateVO billCreateVO = new BillCreateVO();
        billCreateVO.setAmount(new BigDecimal("1231231"));
        billCreateVO.setBillId("12312312312");
        billCreateVO.setBizModel("{\"@type\":\"com.alibaba.fastjson.JSONObject\",\"amount\":1231231.,\"billId\":\"12312312312\",\"dueDate\":\"qw3eqweqw\",\"finalPayerId\":\"q23eqwewq\"}");
        billCreateVO.setDueDate("qw3eqweqw");
        billCreateVO.setFinalPayerId("q23eqwewq");
        billCreateVO.setHolder("lingchao");
        billCreateVO.setRequestId("lingchaoddd");

        List<Action> actionList = utxoActionConvertor.buildCreateBillWithIdentityActionList(billCreateVO);

        System.out.println("---------------------------" + coreTransactionConvertor.buildCoreTransaction("12312312", new JSONObject(), actionList,
            BizTypeConst.TRANSFER_UTXO));
    }

}