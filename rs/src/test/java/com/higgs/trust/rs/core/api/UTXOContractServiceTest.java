package com.higgs.trust.rs.core.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.TxOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.List;


public class UTXOContractServiceTest extends IntegrateBaseTest {
    @Autowired
    private RsBlockChainService rsBlockChainService;

    @Test
    public  void processTest() {
        UTXOAction utxoAction = new UTXOAction();

        List<TxIn> inputList = org.testng.collections.Lists.newArrayList();
        TxIn  txIn = new TxIn();
        txIn.setTxId("a2158bac58936fa0a8744c713b9612ff5850ed68c4e02af7683026525b55fcf6");
        txIn.setActionIndex(0);
        txIn.setIndex(0);

        TxIn  txIn1 = new TxIn();
        txIn1.setTxId("9cae0567e79470b6ddf80408d3a9b546905daf3c792bcc5dec2999f11ac9fcfa");
        txIn1.setActionIndex(0);
        txIn1.setIndex(0);


        TxIn  txIn2 = new TxIn();
        txIn2.setTxId("356ed53ffffe4029365c7d353f0fdedc62e3511ea5fc16d5bc394544a88a9d7a");
        txIn2.setActionIndex(0);
        txIn2.setIndex(0);

        inputList.add(txIn);
        inputList.add(txIn1);
        inputList.add(txIn2);


        List<TxOut> outputList = Lists.newArrayList();
        TxOut txOut  = new TxOut();
        txOut.setIdentity("d0c23bd696f4b7469ff2837a812127a9334ac73b7864a356f1572f5ea860d730");
        txOut.setIndex(0);
        txOut.setActionIndex(0);
        JSONObject state = new JSONObject();
        state.put("@type", "com.alibaba.fastjson.JSONObject");
        state.put("currency", "BUC");
        state.put("amount", new BigDecimal("54998920.0000000000"));
        txOut.setState(state);

        TxOut txOut1  = new TxOut();
        txOut1.setIdentity("a3018ae7cb59a430fedf6c5acf919401d013f9d5c4d172ff09ae7befc3a19f1e");
        txOut1.setIndex(0);
        txOut1.setActionIndex(0);
        JSONObject state1 = new JSONObject();
        state1.put("@type", "com.alibaba.fastjson.JSONObject");
        state1.put("currency", "BUC");
        state1.put("amount", new BigDecimal("100.0000000000"));
        txOut1.setState(state1);

        outputList.add(txOut);
        outputList.add(txOut1);

        utxoAction.setIndex(0);
        utxoAction.setType(ActionTypeEnum.UTXO);
        utxoAction.setInputList(inputList);
        utxoAction.setOutputList(outputList);
        utxoAction.setStateClass("2342");
        utxoAction.setUtxoActionType(UTXOActionTypeEnum.NORMAL);
        utxoAction.setContractAddress("1234567890");
        List<Action> actionList = Lists.newArrayList(utxoAction);
        CoreTransaction coreTransaction = new CoreTransaction();

        coreTransaction.setActionList(actionList);

        System.out.println(JSON.toJSONString(coreTransaction));
       System.out.println("contract resault:"+rsBlockChainService.processContract(coreTransaction));
    }

    @Test
    public void Test() {
        UTXOAction utxoAction = new UTXOAction();

        List<TxIn> inputList = org.testng.collections.Lists.newArrayList();
        TxIn  txIn = new TxIn();
        txIn.setTxId("b76c4bac8007792e0b565d121c7213cd0f22d84e2d07b5e14c4e47675be33582");
        txIn.setActionIndex(0);
        txIn.setIndex(0);
        inputList.add(txIn);


        List<TxOut> outputList = Lists.newArrayList();
        TxOut txOut  = new TxOut();
        txOut.setIdentity("8df8999bd048bef78f19ce7e9939b33a7e074b6ac6b49e15a7f50026db05b0d4");
        txOut.setIndex(0);
        txOut.setActionIndex(0);
        JSONObject state = new JSONObject();
        state.put("@type", "com.alibaba.fastjson.JSONObject");
        state.put("currency", "BUC");
        state.put("amount", new BigDecimal("1889989979.0129460000"));
        txOut.setState(state);

        TxOut txOut1  = new TxOut();
        txOut1.setIdentity("b00e88e3c85e6759f8032171a9c26144e23b31541576237c6cf7a981e6629404");
        txOut1.setIndex(1);
        txOut1.setActionIndex(0);
        JSONObject state1 = new JSONObject();
        state1.put("@type", "com.alibaba.fastjson.JSONObject");
        state1.put("currency", "BUC");
        state1.put("amount", new BigDecimal("9.9998000000"));
        txOut1.setState(state1);

        outputList.add(txOut);
        outputList.add(txOut1);

        utxoAction.setIndex(0);
        utxoAction.setType(ActionTypeEnum.UTXO);
        utxoAction.setInputList(inputList);
        utxoAction.setOutputList(outputList);
        utxoAction.setStateClass("2342");
        utxoAction.setUtxoActionType(UTXOActionTypeEnum.NORMAL);
        utxoAction.setContractAddress("1234567890");
        List<Action> actionList = Lists.newArrayList(utxoAction);
        CoreTransaction coreTransaction = new CoreTransaction();

        coreTransaction.setActionList(actionList);
        System.out.println("contract resault:"+rsBlockChainService.processContract(coreTransaction));
    }
}