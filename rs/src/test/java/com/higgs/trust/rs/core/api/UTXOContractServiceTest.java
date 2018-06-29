package com.higgs.trust.rs.core.api;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.TxOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.List;


public class UTXOContractServiceTest extends IntegrateBaseTest {
    @Autowired
    private UTXOContractService utxoContractService;

    @Test
    public void processTest() {
        UTXOAction utxoAction = new UTXOAction();

        List<TxIn> inputList = org.testng.collections.Lists.newArrayList();
        TxIn  txIn = new TxIn();
        txIn.setTxId("123123");
        txIn.setActionIndex(1);
        txIn.setIndex(0);
        inputList.add(txIn);

        List<TxOut> outputList = Lists.newArrayList();
        TxOut txOut  = new TxOut();
        txOut.setIdentity("lingchao");
        txOut.setIndex(0);
        txOut.setActionIndex(0);
        JSONObject state = new JSONObject();
        state.put("currency", "BUC");
        state.put("amount", 1000);
        txOut.setState(state);

        outputList.add(txOut);

        utxoAction.setIndex(0);
        utxoAction.setType(ActionTypeEnum.UTXO);
        utxoAction.setInputList(inputList);
        utxoAction.setOutputList(outputList);
        utxoAction.setStateClass("2342");
        utxoAction.setUtxoActionType(UTXOActionTypeEnum.NORMAL);
        utxoAction.setContractAddress("1234567890");


        System.out.println("contract resault:"+utxoContractService.process(utxoAction, "1234567890"));
    }
}