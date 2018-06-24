package com.higgs.trust.rs.tx;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.TxOut;
import org.junit.Test;
import org.testng.collections.Lists;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * utxo tx test
 *
 * @author lingchao
 * @create 2018年06月23日11:05
 */
public class UTXOTxTest{

    @Test
    public void test() {

    }

    /**
     * UTXO流转:
     * 输入输出均不能为空     异常:输入输出均为空
     */
    @Test
    public void testNormalWithOutInputListAndOutputList() {

        UTXOAction utxoAction = new UTXOAction();
        utxoAction.setContractAddress("1234567");
        List<TxIn> inputList = new ArrayList<>();
        List<TxOut> outList = new ArrayList<>();
        utxoAction.setOutputList(outList);
        utxoAction.setInputList(inputList);
        utxoAction.setIndex(0);
        utxoAction.setUtxoActionType(UTXOActionTypeEnum.NORMAL);
        utxoAction.setStateClass("com.alibaba.fastjson.JSONObject");
        utxoAction.setType(ActionTypeEnum.UTXO);

        CoreTransaction rsCoreTxVO = CoreTxHelper.makeSimpleTx("tx_create_transfer_UTXO_exception" + System.currentTimeMillis(), Lists.newArrayList(utxoAction));
        CoreTxHelper.post(rsCoreTxVO);

    }

    /**
     * UTXO销毁
     * 输入不能为空list     异常:输入为空
     */
    @Test
    public void testDestructionWithOutInputListAndOutputList() {

        UTXOAction utxoAction = new UTXOAction();
        utxoAction.setContractAddress("1234567");
        List<TxIn> inputList = new ArrayList<>();
        List<TxOut> outList = new ArrayList<>();
        utxoAction.setOutputList(outList);
        utxoAction.setInputList(inputList);
        utxoAction.setIndex(0);

        utxoAction.setUtxoActionType(UTXOActionTypeEnum.DESTRUCTION);
        utxoAction.setStateClass("com.alibaba.fastjson.JSONObject");
        utxoAction.setType(ActionTypeEnum.UTXO);

        CoreTransaction rsCoreTxVO = CoreTxHelper.makeSimpleTx("tx_destruction_UTXO_exception" + System.currentTimeMillis(), InitPolicyEnum.UTXO_DESTROY.getPolicyId(), Lists.newArrayList(utxoAction), CoreTxHelper.SENDER);
        CoreTxHelper.post(rsCoreTxVO);

    }


    /**
     * UTXO销毁
     * 输出必须为空list     异常:输出不为空
     */
    @Test
    public void testDestructionWithOutputList() {

        UTXOAction utxoAction = new UTXOAction();
        utxoAction.setContractAddress("1234567");
        List<TxIn> inputList = new ArrayList<>();

        TxIn txIn = new TxIn();
        txIn.setIndex(0);
        txIn.setTxId("UTXOlingchaoyiyi凌超");
        txIn.setActionIndex(0);
        inputList.add(txIn);
        List<TxOut> outList = new ArrayList<>();

        TxOut txOut = new TxOut();
        JSONObject state = new JSONObject();
        state.put("amount", new BigDecimal("1"));
        txOut.setIndex(0);
        txOut.setActionIndex(0);
        txOut.setIdentity("12312312321");
        txOut.setState(state);
        outList.add(txOut);
        utxoAction.setOutputList(outList);
        utxoAction.setInputList(inputList);
        utxoAction.setIndex(0);

        utxoAction.setUtxoActionType(UTXOActionTypeEnum.DESTRUCTION);
        utxoAction.setStateClass("com.alibaba.fastjson.JSONObject");
        utxoAction.setType(ActionTypeEnum.UTXO);

        CoreTransaction rsCoreTx = CoreTxHelper.makeSimpleTx("tx_destruction_UTXO_exception" + System.currentTimeMillis(), InitPolicyEnum.UTXO_DESTROY.getPolicyId(), Lists.newArrayList(utxoAction), CoreTxHelper.SENDER);
        CoreTxHelper.post(rsCoreTx);

    }
}
