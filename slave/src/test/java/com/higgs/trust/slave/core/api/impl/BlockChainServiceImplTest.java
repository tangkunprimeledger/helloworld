package com.higgs.trust.slave.core.api.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.BlockChainService;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.account.FundDirectionEnum;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.account.OpenAccount;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.action.DataIdentityAction;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.manage.Policy;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.TxOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author tangfashuang
 * @date 2018/04/14 16:54
 */
public class BlockChainServiceImplTest {

    private static final List<SignedTransaction> signedTxList = new ArrayList<>();

    private static final String priKey1 =
        "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBALhZZQEKaaZOsD4z+m1AoBlVnSZFD9mafaRAt9PVHtgkFJViV8a3s+2bFEkxVIxLSqdyWVCqZCOm1+6jbWmk4pN5yY4AaZBCPM6kHkzl/o5ctGXqkIf9s8WxFAmKpMEi7qP6SCitaoMy1sC+IS/vcOphzJZ3NbP3kG0FyCR5EvGdAgMBAAECgYAf7HMeRARZpWTF0NB8HOXcnUPSfcEp6KP7Tq3GxDBMM6tQ1y/mHKfO7L0Nk7pVdTBfYODwpCElP15DWA+5bLFDl2GzCY3gBoMrgrpIaaD672Qf12ikcVf6q/FSNJAvvDPSpLQKYJKG4Aa8/0mFZB9JU1KM2+wDl4Fgf/Lf+vM/zQJBAN1X5nvjy4nwTXlHWvtB+PG9ptAsGo9baEtGW2UrDhPYObghJpk4Slxo+r7l4fWNnwEP6kMGBlFMH41oxNezYLcCQQDVNqiBJBVUgIVGaLSb3ksyXerOf9w1g7JTpSoZmf6SOMAZQ3kqky9Ik4LGwnlFaolUEA/fcTev2rC8iBFt00RLAkB09I5H5jzVXRFCxQ5w9xIYghKTqso5952rMLj4QwDEQZt2DKY9jb3VCG990TBNNJDQ2dz5n0RVTrjZWoOwSgsPAkBvFcArMIKQeTl22pymzOV+w2HP3tv7YbcqT1Yk6o+w3TJwty/M18x90qUDK1WFriEIlCnA77rku1rzjy0NfFILAkBnbOFY4ECXdFysok3n5AgrdHBr8bRSRvzBpdjUL9eH0Cs/37aqwZynwCg6DX5/I2gIwX78ysnujB4mY6lFOG5f";

    private static final String priKey2 =
        "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKQwv8kaTmN15Z7c6gz3/7a8wmtuJqgn25uWAkBO5vTs6DpfB0Nf7N5jOH2pMhkgqkdiOlpNpTb+zoJZ+DNy28mHHbpb99GDEoa5zvcXxypU5yrNhmrch1bJbKZQiGoX/5NAia9t/Kltxdcs6EmWuOQB79fLhLDgwHeUDzYOdM13AgMBAAECgYAFWznGe78262+0QQy5o5WKBpppGszUC4jUiI5GPsy2DMx+qv73qbd2gdIj91MVEsW7Um8I5yOOqb1e70RzmTmmSgmIbc7L2ogkEVa/AWdnmFIqVV7EOokc7pExc0UMlIBXCiNynrQic0YtxV65JjaE/JAFomCCAUBbsP9TSs/ZMQJBANRq8rBvR1PCA9pwzqfwalKAAzpwsOs0tavP8XF80xm7XKNZrnOIIiLSj+ME630ECYJZ2XTKF1g/TblIHV8zAYMCQQDF4LQcqKuNbeUeu0Xf3VX0TXPImIdB3ZbbQyPuynhk5D0Fx72q29gRKUZifrm1Kog6fvrwN1IyuoZem3oijEX9AkApkKPckmnKofRPEjPd+NVVP2diUBrOa4oBDLeaFWrZZihCbpIMWV8UoU82hQfvdpLFxv8eM01OH1T+JHZa4ogxAkA2WEs/H7fV5NurQAWlwPUNXoQxEGr9VO1MlLj2qRa9ps13m+7kUPKba/mPrXw1XFQDtMIYXSkvE3k53HuDp4DFAkAxhxi9veGOKa24Fp+4MFSF3L9UdR6MROqIYVGgE0gHj7r+NIuCqk/l9acw9W4E5gAN03P3RAKpjmcqxOkZyj7h";

    private static final String priKey3 = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANDzTWjIRJ6Y3dKT4Z08/QuUMjj3OFSgt8qD9ZFgT3TXik44olP7O0gVJiL+tBtCuqsW6nU2BWt2S/1/SmGVq1dxco1VSCU/Dk7ReBTMRyZBOxfzdMnaTWMbiO+ETodJl3eQbK1miJyVbg7hLe7s/8xiH7AGsKkppW6GC7Kpb4zJAgMBAAECgYBORbYLuGmsF4uQ5ICxjDUmbz9ZA5MAcKwomsIU0UUyecN/hcuZNhWA7Rs6JLuHMroGeTEe8zuYg9n3fgV5BL4H96z3SBSrY+BsCf1CxYGXEVCHzlt6g8575MqtxIlqPXnpKr9S1663EtsCCJ93t5rZmMA7z8bUbFRTcrUsajYzAQJBAPynP0a6Pk5JlF0TW5vbzusZb3CsEdPTp39NxlHEx9v/2xuREti1CSVMhdm8ZDdC5hDoETZn4DTiBAF0Z5it6pkCQQDTt9uSFv16v+62yJIz0KE9EUZrLua1BlfTIyvgBZQ6Lp5ORS2S9iVzfOS77mufysbfGSpmD6Oc5ElY2coUy8GxAkAlFB5zMM4IC0Bc0IR3QTECy77RGE+deMhyJGXghjKWlNwBFa9gYmEvOiXCqKVEfurovEYaZ/A9kpXn6L9zZsKxAkAuym+IdfRHcKu9Uc6eDPnVmT/K6G6si15Vl2xW8mS0ByGNgtRzqlrUj0GuFx9KDXKuU81/CO3L+tgK/vceaXnBAkEAk+OjzXA0KXZGKm+O8/Vl8yiJQpuvpuO4cxy4E7nEAjevFip88p4tO03DVxjyq2Az7457q/T+C/Ohr1X9uS/v/Q==";

    @Autowired
    private BlockChainService blockChainService;

    @BeforeMethod
    public void setUp() throws Exception {
        //JSON auto detect class type
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        //JSON不做循环引用检测
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.DisableCircularReferenceDetect.getMask();
        //JSON输出NULL属性
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteMapNullValue.getMask();
        //toJSONString的时候对一级key进行按照字母排序
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.SortField.getMask();
        //toJSONString的时候对嵌套结果进行按照字母排序
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.MapSortField.getMask();
        //toJSONString的时候记录Class的name
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteClassName.getMask();

        SignedTransaction signedTx1 = new SignedTransaction();

        CoreTransaction coreTx1 = new CoreTransaction();

        coreTx1.setTxId("pending-tx-test-12");
        //test registerPolicy
        //        coreTx1.setActionList(registerPolicyList);
        //        coreTx1.setPolicyId("000000");

        // test openAccount
        coreTx1.setActionList(initPoilicy());
        coreTx1.setPolicyId("000000");
        coreTx1.setLockTime(new Date());
        coreTx1.setBizModel(new JSONObject());
        coreTx1.setSender("rs-test1");
        coreTx1.setVersion("1.0.0");

        System.out.println(JSON.toJSONString(coreTx1));

        String sign1 = SignUtils.sign(JSON.toJSONString(coreTx1), priKey1);
        //        String sign2 = SignUtils.sign(JSON.toJSONString(coreTx1), priKey2);
        List<String> signList = new ArrayList<>();
        signList.add(sign1);
        //        signList.add(sign2);
        signedTx1.setCoreTx(coreTx1);
        signedTx1.setSignatureList(signList);

        signedTxList.add(signedTx1);
    }

    private List<Action> initAccount() {
        OpenAccount accountBO1 = new OpenAccount();
        accountBO1.setType(ActionTypeEnum.OPEN_ACCOUNT);
        accountBO1.setIndex(1);
        accountBO1.setAccountNo("account_no_003");
        accountBO1.setChainOwner("IOV_CHAIN");
        accountBO1.setDataOwner("RS_001");
        accountBO1.setCurrency("IOV");
        //        accountBO.setAmount(new BigDecimal("10"));
        accountBO1.setFundDirection(FundDirectionEnum.DEBIT);

        OpenAccount accountBO2 = new OpenAccount();
        accountBO2.setType(ActionTypeEnum.OPEN_ACCOUNT);
        accountBO2.setIndex(1);
        accountBO2.setAccountNo("account_no_004");
        accountBO2.setChainOwner("BTC_CHAIN");
        accountBO2.setDataOwner("RS_001");
        accountBO2.setCurrency("BTC");
//        accountBO2.setAmount(new BigDecimal("1924774.34834"));
        accountBO2.setFundDirection(FundDirectionEnum.DEBIT);

        List<Action> openAccounts = new ArrayList<>();
        openAccounts.add(accountBO1);
        openAccounts.add(accountBO2);

        return openAccounts;
    }

    private List<Action> initPoilicy() {
        RegisterPolicy registerPolicy = new RegisterPolicy();
        registerPolicy.setPolicyId("test-policy-1");
        registerPolicy.setPolicyName("测试注册policy-1");

        Set<String> rsIds = new HashSet<>();
        rsIds.add("rs-test1");
        registerPolicy.setRsIdSet(rsIds);
        registerPolicy.setType(ActionTypeEnum.REGISTER_POLICY);
        registerPolicy.setIndex(0);

        List<Action> registerPolicies = new ArrayList<>();
        registerPolicies.add(registerPolicy);
        return registerPolicies;
    }

    private List<Action> initRS() {
        List<Action> registerRSList = new ArrayList<>();
        RegisterRS registerRS = new RegisterRS();
        registerRS.setType(ActionTypeEnum.REGISTER_RS);
        registerRS.setIndex(0);
        registerRS.setPubKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCkML/JGk5jdeWe3OoM9/+2vMJrbiaoJ9ublgJATub07Og6XwdDX+zeYzh9qTIZIKpHYjpaTaU2/s6CWfgzctvJhx26W/fRgxKGuc73F8cqVOcqzYZq3IdWyWymUIhqF/+TQImvbfypbcXXLOhJlrjkAe/Xy4Sw4MB3lA82DnTNdwIDAQAB");
        registerRS.setDesc("rs-test3-desc");
        registerRS.setRsId("rs-test3");

        registerRSList.add(registerRS);
        return registerRSList;
    }

    @Test
    public void createIdentityTx() throws Exception {
        SignedTransaction signedTx1 = new SignedTransaction();


        CoreTransaction coreTx1 = new CoreTransaction();

        coreTx1.setTxId("createIdentity-123456789");

        coreTx1.setActionList(initUTXOIdentityList());
        coreTx1.setPolicyId("test-policy-1");
        coreTx1.setLockTime(new Date());
        coreTx1.setSender("rs-test1");
        coreTx1.setVersion("1.0.0");

        String sign1 = SignUtils.sign(JSON.toJSONString(coreTx1), priKey1);
        String sign2 = SignUtils.sign(JSON.toJSONString(coreTx1), priKey2);
        List<String> signList = new ArrayList<>();
        signList.add(sign1);
        signList.add(sign2);
        signedTx1.setCoreTx(coreTx1);
        signedTx1.setSignatureList(signList);

        signedTxList.add(signedTx1);

        System.out.println("create  identity tx:"+ JSONObject.toJSONString(signedTxList));
    }



    @Test
    public void createIssueUTXOTX() throws Exception {
        SignedTransaction signedTx1 = new SignedTransaction();


        CoreTransaction coreTx1 = new CoreTransaction();

        coreTx1.setTxId("issueUTXO-12345678901");

        coreTx1.setActionList(initIssueUTXOActionList());
        coreTx1.setPolicyId("000001");
        coreTx1.setLockTime(new Date());
        coreTx1.setSender("rs-test1");
        coreTx1.setVersion("1.0.0");

        String sign1 = SignUtils.sign(JSON.toJSONString(coreTx1), priKey1);
        String sign2 = SignUtils.sign(JSON.toJSONString(coreTx1), priKey2);
        List<String> signList = new ArrayList<>();
        signList.add(sign1);
        signList.add(sign2);
        signedTx1.setCoreTx(coreTx1);
        signedTx1.setSignatureList(signList);

        signedTxList.add(signedTx1);

        System.out.println("issue UTXO tx:"+JSONObject.toJSONString(signedTxList));
    }



    @Test
    public void createNormalUTXOTX() throws Exception {
        SignedTransaction signedTx1 = new SignedTransaction();


        CoreTransaction coreTx1 = new CoreTransaction();

        coreTx1.setTxId("normalUTXO-12345678901");

        coreTx1.setActionList(initNormalUTXOActionList());
        coreTx1.setPolicyId("test-policy-1");
        coreTx1.setLockTime(new Date());
        coreTx1.setSender("rs-test1");
        coreTx1.setVersion("1.0.0");

        String sign1 = SignUtils.sign(JSON.toJSONString(coreTx1), priKey1);
     //   String sign2 = SignUtils.sign(JSON.toJSONString(coreTx1), priKey2);
        List<String> signList = new ArrayList<>();
        signList.add(sign1);
      //  signList.add(sign2);
        signedTx1.setCoreTx(coreTx1);
        signedTx1.setSignatureList(signList);

        signedTxList.add(signedTx1);

        System.out.println("issue UTXO tx:"+JSONObject.toJSONString(signedTxList));
    }


    @Test
    public void creatDestoyUTXOTX() throws Exception {
        SignedTransaction signedTx1 = new SignedTransaction();


        CoreTransaction coreTx1 = new CoreTransaction();

        coreTx1.setTxId("destructionUTXO-123456789");

        coreTx1.setActionList(initDestroyUTXOActionList());
        coreTx1.setPolicyId("000002");
        coreTx1.setLockTime(new Date());
        coreTx1.setSender("rs-test1");
        coreTx1.setVersion("1.0.0");

        String sign1 = SignUtils.sign(JSON.toJSONString(coreTx1), priKey1);
        String sign2 = SignUtils.sign(JSON.toJSONString(coreTx1), priKey2);
        List<String> signList = new ArrayList<>();
        signList.add(sign1);
         signList.add(sign2);
        signedTx1.setCoreTx(coreTx1);
        signedTx1.setSignatureList(signList);

        signedTxList.add(signedTx1);

        System.out.println("issue UTXO tx:"+JSONObject.toJSONString(signedTxList));
    }


    private List<Action> initUTXOIdentityList() {
        DataIdentityAction dataIdentityAction = new DataIdentityAction();
        dataIdentityAction.setChainOwner("TRUST");
        dataIdentityAction.setDataOwner("ts-test1");
        dataIdentityAction.setIdentity("lingchao");
        dataIdentityAction.setIndex(0);
        dataIdentityAction.setType(ActionTypeEnum.CREATE_DATA_IDENTITY);


        DataIdentityAction dataIdentityAction1 = new DataIdentityAction();
        dataIdentityAction1.setChainOwner("TRUST");
        dataIdentityAction1.setDataOwner("ts-test1");
        dataIdentityAction1.setIdentity("yiyi");
        dataIdentityAction1.setIndex(1);
        dataIdentityAction1.setType(ActionTypeEnum.CREATE_DATA_IDENTITY);

        List<Action> dataIdentityActionList = new ArrayList<>();
        dataIdentityActionList.add(dataIdentityAction);
        dataIdentityActionList.add(dataIdentityAction1);
        return dataIdentityActionList;
    }



    private List<Action> initIssueUTXOActionList() {
         UTXOAction utxoAction = new UTXOAction();
        List<TxOut> outputList = new ArrayList<>();
        TxOut  txOut = new TxOut();
        JSONObject state = new JSONObject();
        state.put("amount",1);
        txOut.setState(state);
        txOut.setIndex(0);
        txOut.setActionIndex(0);
        txOut.setIdentity("lingchaochao");

        TxOut  txOut1 = new TxOut();
        JSONObject state1 = new JSONObject();
        state1.put("amount",2);
        txOut1.setState(state1);
        txOut1.setIndex(1);
        txOut1.setActionIndex(0);
        txOut1.setIdentity("lingchaochao");


        outputList.add(txOut);
        outputList.add(txOut1);

        utxoAction.setOutputList(outputList);
        utxoAction.setUtxoActionType(UTXOActionTypeEnum.ISSUE);
        String code ="function verify() {\n" + "    var action = ctx.getAction();\n" + "\tvar actionType = action.utxoActionType;\n" + "\tvar issueActionType = ctx.getUTXOActionType('ISSUE');\n" + "\tvar normalActionType = ctx.getUTXOActionType('NORMAL');\n" + "\tvar destructionActionType = ctx.getUTXOActionType('DESTRUCTION');\n" + "\t\n" + "\t//issue utxo  action\n" + "\tif(actionType == issueActionType){\n" + "\t\treturn true;\n" + "\t}\n" + "\t\n" + "    //notmal utxo  action\n" + "    if(actionType == normalActionType){\n" + "\t  if(action.inputList.length == 0 || action.getOutputList().length == 0){\n" + "\t \treturn false;\n" + "\t  }\n" + "\tvar utxoList = ctx.queryTxOutList(action.inputList);\n" + "\tvar inputsAmount = 0;\n" + "\tvar outputsAmount = 0;\n" + "\tutxoList.forEach(function (input) {inputsAmount += JSON.parse(input.getState()).amount;});\n" + "\taction.getOutputList().forEach(function (input) {outputsAmount += input.getState().amount;});\n" + "\treturn inputsAmount == outputsAmount;\n" + "\t}\n" + "\t\n" + "\t //destruction utxo  action\n" + "\tif(actionType == destructionActionType){\n" + "\t\treturn true;\n" + "\t}\n" + "   \n" + "\treturn false;\n" + "}";
        utxoAction.setContract(code);
        utxoAction.setType(ActionTypeEnum.UTXO);
        utxoAction.setStateClass("amount");
        utxoAction.setIndex(0);

        List<Action> utxoActionList = new ArrayList<>();
        utxoActionList.add(utxoAction);
        return utxoActionList;
    }





    private List<Action> initNormalUTXOActionList() {
        UTXOAction utxoAction = new UTXOAction();

        List<TxIn> inputList = new ArrayList<>();
        TxIn txIn = new TxIn();
        txIn.setTxId("issueUTXO-12345678901");
        txIn.setIndex(0);
        txIn.setActionIndex(0);
        inputList.add(txIn);

        TxIn txIn1 = new TxIn();
        txIn1.setTxId("issueUTXO-12345678901");
        txIn1.setIndex(1);
        txIn1.setActionIndex(0);
        inputList.add(txIn1);

        List<TxOut> outputList = new ArrayList<>();
        TxOut  txOut = new TxOut();
        JSONObject state = new JSONObject();
        state.put("amount",3);
        txOut.setState(state);
        txOut.setIndex(0);
        txOut.setActionIndex(0);
        txOut.setIdentity("yiyi");

        outputList.add(txOut);

        utxoAction.setInputList(inputList);
        utxoAction.setOutputList(outputList);
        utxoAction.setUtxoActionType(UTXOActionTypeEnum.NORMAL);
        String code ="function verify() {\n" + "    var action = ctx.getAction();\n" + "\tvar actionType = action.utxoActionType;\n" + "\tvar issueActionType = ctx.getUTXOActionType('ISSUE');\n" + "\tvar normalActionType = ctx.getUTXOActionType('NORMAL');\n" + "\tvar destructionActionType = ctx.getUTXOActionType('DESTRUCTION');\n" + "\t\n" + "\t//issue utxo  action\n" + "\tif(actionType == issueActionType){\n" + "\t\treturn true;\n" + "\t}\n" + "\t\n" + "    //notmal utxo  action\n" + "    if(actionType == normalActionType){\n" + "\t  if(action.inputList.length == 0 || action.getOutputList().length == 0){\n" + "\t \treturn false;\n" + "\t  }\n" + "\tvar utxoList = ctx.queryTxOutList(action.inputList);\n" + "\tvar inputsAmount = 0;\n" + "\tvar outputsAmount = 0;\n" + "\tutxoList.forEach(function (input) {inputsAmount += JSON.parse(input.getState()).amount;});\n" + "\taction.getOutputList().forEach(function (input) {outputsAmount += input.getState().amount;});\n" + "\treturn inputsAmount == outputsAmount;\n" + "\t}\n" + "\t\n" + "\t //destruction utxo  action\n" + "\tif(actionType == destructionActionType){\n" + "\t\treturn true;\n" + "\t}\n" + "   \n" + "\treturn false;\n" + "}";
        utxoAction.setContract(code);
        utxoAction.setType(ActionTypeEnum.UTXO);
        utxoAction.setStateClass("amount");
        List<Action> utxoActionList = new ArrayList<>();
        utxoActionList.add(utxoAction);
        return utxoActionList;
    }


    private List<Action> initDestroyUTXOActionList() {
        UTXOAction utxoAction = new UTXOAction();

        List<TxIn> inputList = new ArrayList<>();
        TxIn txIn = new TxIn();
        txIn.setTxId("1234567890");
        txIn.setIndex(0);
        txIn.setActionIndex(0);
        inputList.add(txIn);

        utxoAction.setInputList(inputList);
        utxoAction.setUtxoActionType(UTXOActionTypeEnum.DESTRUCTION);
        String code ="function verify() {\n" + "    var action = ctx.getAction();\n" + "\tvar actionType = action.utxoActionType;\n" + "\tvar issueActionType = ctx.getUTXOActionType('ISSUE');\n" + "\tvar normalActionType = ctx.getUTXOActionType('NORMAL');\n" + "\tvar destructionActionType = ctx.getUTXOActionType('DESTRUCTION');\n" + "\t\n" + "\t//issue utxo  action\n" + "\tif(actionType == issueActionType){\n" + "\t\treturn true;\n" + "\t}\n" + "\t\n" + "    //notmal utxo  action\n" + "    if(actionType == normalActionType){\n" + "\t  if(action.inputList.length == 0 || action.getOutputList().length == 0){\n" + "\t \treturn false;\n" + "\t  }\n" + "\tvar utxoList = ctx.queryTxOutList(action.inputList);\n" + "\tvar inputsAmount = 0;\n" + "\tvar outputsAmount = 0;\n" + "\tutxoList.forEach(function (input) {inputsAmount += JSON.parse(input.getState()).amount;});\n" + "\taction.getOutputList().forEach(function (input) {outputsAmount += input.getState().amount;});\n" + "\treturn inputsAmount == outputsAmount;\n" + "\t}\n" + "\t\n" + "\t //destruction utxo  action\n" + "\tif(actionType == destructionActionType){\n" + "\t\treturn true;\n" + "\t}\n" + "   \n" + "\treturn false;\n" + "}";
        utxoAction.setContract(code);
        utxoAction.setType(ActionTypeEnum.UTXO);
        utxoAction.setStateClass("amount");
        List<Action> utxoActionList = new ArrayList<>();
        utxoActionList.add(utxoAction);
        return utxoActionList;
    }


    @Test
    public void testSubmitTransaction() {
        try {
            System.out.println(JSON.toJSONString(signedTxList));
//            RespData respData = blockChainService.submitTransaction(signedTxList);
//            Assert.assertEquals("000000", respData.getRespCode());
//            Assert.assertEquals("success", respData.getMsg());
//            System.out.println(respData);
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

}