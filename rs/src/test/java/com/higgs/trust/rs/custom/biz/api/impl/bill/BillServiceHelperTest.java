package com.higgs.trust.rs.custom.biz.api.impl.bill;

import com.google.common.collect.Lists;
import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.rs.custom.util.converter.UTXOActionConvertor;
import com.higgs.trust.rs.custom.vo.BillCreateVO;
import com.higgs.trust.rs.custom.vo.BillTransferVO;
import com.higgs.trust.rs.custom.vo.TransferDetailVO;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.utxo.TxOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.List;

public class BillServiceHelperTest extends IntegrateBaseTest {
    @Autowired
    private BillServiceHelper billServiceHelper;

    @Autowired
    private UTXOActionConvertor utxoActionConvertor;

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
        billTransferVO.setBillId("12312312312");
        billTransferVO.setBizModel("12312");
        billTransferVO.setHolder("lingchao");
        billTransferVO.setRequestId("132123dfscsdwada");

        billTransferVO.setTransferList(Lists.newArrayList());

        TransferDetailVO transferDetailVO = new TransferDetailVO();
        transferDetailVO.setNextHolder("chao1");
        transferDetailVO.setAmount(new BigDecimal("1230000"));
        billTransferVO.getTransferList().add(transferDetailVO);

        transferDetailVO = new TransferDetailVO();
        transferDetailVO.setNextHolder("chao2");
        transferDetailVO.setAmount(new BigDecimal("1231"));
        billTransferVO.getTransferList().add(transferDetailVO);

        System.out.println("-------------------------" + billServiceHelper.insertRequest(billTransferVO));
        System.out.println("-------------------------" + billServiceHelper.insertRequest(billTransferVO));
    }

    @Test
    public void insertBillTestTransfer() {

        BillTransferVO billTransferVO = new BillTransferVO();
        billTransferVO.setBillId("12312312312");
        billTransferVO.setBizModel("12312");
        billTransferVO.setHolder("lingchao");
        billTransferVO.setRequestId("132123dfscsdwada");

        billTransferVO.setTransferList(Lists.newArrayList());

        TransferDetailVO transferDetailVO = new TransferDetailVO();
        transferDetailVO.setNextHolder("chao1");
        transferDetailVO.setAmount(new BigDecimal("1230000"));
        billTransferVO.getTransferList().add(transferDetailVO);

        transferDetailVO = new TransferDetailVO();
        transferDetailVO.setNextHolder("chao2");
        transferDetailVO.setAmount(new BigDecimal("1231"));
        billTransferVO.getTransferList().add(transferDetailVO);

        List<Action> actionList = utxoActionConvertor.buildTransferBillWithIdentityActionList(billTransferVO);

        //insert bill
        for (Action action : actionList) {
            if (action.getType() == ActionTypeEnum.UTXO) {
                UTXOAction utxoAction = (UTXOAction)action;
                List<TxOut> outputList = utxoAction.getOutputList();
                for (TxOut txOut : outputList) {
                    billServiceHelper.insertBill(billTransferVO.getRequestId(), utxoAction, txOut);
                }
            }
        }
    }

}