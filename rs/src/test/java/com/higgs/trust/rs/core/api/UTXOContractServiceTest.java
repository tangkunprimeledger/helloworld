package com.higgs.trust.rs.core.api;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.common.crypto.Crypto;
import com.higgs.trust.common.crypto.KeyPair;
import com.higgs.trust.common.enums.CryptoTypeEnum;
import com.higgs.trust.config.crypto.CryptoUtil;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.utxo.Sign;
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
    public void processTest() {
        UTXOAction utxoAction = new UTXOAction();

        List<TxIn> inputList = org.testng.collections.Lists.newArrayList();
        TxIn txIn = new TxIn();
        txIn.setTxId("beb4b682576e6813c72743f0ab3d587715b48a381cc3606e53d38abcedfba160");
        txIn.setActionIndex(0);
        txIn.setIndex(0);

        inputList.add(txIn);


        List<TxOut> outputList = Lists.newArrayList();
        TxOut txOut = new TxOut();
        txOut.setIdentity("BRFodQbQuHK9cXJhh3w9gdoPE7T4PnwW7nnV3NeW9Zc4CrBR");
        txOut.setIndex(0);
        txOut.setActionIndex(0);
        JSONObject state = new JSONObject();
        state.put("currency", "BUC");
        state.put("amount", new BigDecimal("999999999999999999997.000000000").toPlainString());
        txOut.setState(state);

        TxOut txOut1 = new TxOut();
        txOut1.setIdentity("BRFodQbQuHK9cXJZUrg6Hbungu4GE4X9AVzif3ToBwuWdW8z");
        txOut1.setIndex(0);
        txOut1.setActionIndex(0);
        JSONObject state1 = new JSONObject();
        state1.put("currency", "BUC");
        state1.put("amount", new BigDecimal("1.000000000"));
        txOut1.setState(state1);

        outputList.add(txOut);
        outputList.add(txOut1);

        utxoAction.setIndex(0);
        utxoAction.setType(ActionTypeEnum.UTXO);
        utxoAction.setInputList(inputList);
        utxoAction.setOutputList(outputList);
        utxoAction.setStateClass("com.alibaba.fastjson.JSONObject");
        utxoAction.setUtxoActionType(UTXOActionTypeEnum.NORMAL);
        utxoAction.setContractAddress("123456780");
        List<Action> actionList = Lists.newArrayList(utxoAction);
        CoreTransaction coreTransaction = new CoreTransaction();

        coreTransaction.setActionList(actionList);
        System.out.println("contract resault:" + rsBlockChainService.processContract(coreTransaction));
    }


    @Test
    public void Test() {
        UTXOAction utxoAction = new UTXOAction();

        List<TxIn> inputList = org.testng.collections.Lists.newArrayList();
        TxIn txIn = new TxIn();
        txIn.setTxId("beb4b682576e6813c72743f0ab3d587715b48a381cc3606e53d38abcedfba160");
        txIn.setActionIndex(0);
        txIn.setIndex(0);

        inputList.add(txIn);


        List<TxOut> outputList = Lists.newArrayList();
        TxOut txOut = new TxOut();
        txOut.setIdentity("BRFodQbQuHK9cXJhh3w9gdoPE7T4PnwW7nnV3NeW9Zc4CrBR");
        txOut.setIndex(0);
        txOut.setActionIndex(0);
        JSONObject state = new JSONObject();
        state.put("currency", "BUC");
        state.put("amount", new BigDecimal("999999997.000000000").toPlainString());
        txOut.setState(state);

        TxOut txOut1 = new TxOut();
        txOut1.setIdentity("BRFodQbQuHK9cXJZUrg6Hbungu4GE4X9AVzif3ToBwuWdW8z");
        txOut1.setIndex(0);
        txOut1.setActionIndex(0);
        JSONObject state1 = new JSONObject();
        state1.put("currency", "BUC");
        state1.put("amount", new BigDecimal("1.000000000"));
        txOut1.setState(state1);

        outputList.add(txOut);
        outputList.add(txOut1);

        utxoAction.setIndex(0);
        utxoAction.setType(ActionTypeEnum.UTXO);
        utxoAction.setInputList(inputList);
        utxoAction.setOutputList(outputList);
        utxoAction.setStateClass("com.alibaba.fastjson.JSONObject");
        utxoAction.setUtxoActionType(UTXOActionTypeEnum.NORMAL);
        utxoAction.setContractAddress("123456780");
        Crypto crypto = CryptoUtil.getBizCrypto(CryptoTypeEnum.ECC.getCode());
        KeyPair keyPair = crypto.generateKeyPair();
        String signature = crypto.sign("ling", keyPair.getPriKey());
        Sign sign = new Sign(keyPair.getPubKey(), signature, CryptoTypeEnum.ECC.getCode());
        List<Sign> signList = Lists.newArrayList(sign);
        utxoAction.setSignList(signList);
        List<Action> actionList = Lists.newArrayList(utxoAction);

        CoreTransaction coreTransaction = new CoreTransaction();

        coreTransaction.setActionList(actionList);
        System.out.println("contract resault:" + rsBlockChainService.processContract(coreTransaction));
    }
}