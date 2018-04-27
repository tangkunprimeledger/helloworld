package com.higgs.trust.slave.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.higgs.trust.common.utils.OkHttpClientManager;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.account.FundDirectionEnum;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.core.service.action.account.TestDataMaker;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.TxOut;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author liuyu
 * @description
 * @date 2018-04-20
 */
public class AccountControllerTest {
    private static final String priKey1 ="MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIsMM2JkqnQACN+RUqRF4UkW58XnjpjA+RP4mnA4uD9KCjlJeTxvY0yHDqsvIaiXr7Ge0QB1VAKq0xit2BWHfVU/XlO1tqK+ea7YxooeoglvOkddiJiYTZUNjKM5AhttG950PpzrmeUcl9YGEZ/DwKKee+8tqaDWdIEHBnplO6mVAgMBAAECgYBtrWwCmoDRCw30uv5C0VQIgObE9gdGekB9/kRjbHn4ggBae5gDkaDzxjxNztlv0GYnZqxY/jML/46PEuE06jBzGcOlBuobQJJ38pTg0pnNVHbkTckxfUIr1MYUDhtO18tJZUZuMbYMwwgZ9K9E0N8kjKXk+rRx+BDjlbxNPds6KQJBAMLS/HCXjAfJlzSEWqkBavAKoW+bBhZlkTH+DoNk/KidASgdFBqtPUf5w3U+j8dK4nvt8R9X7zGxRAYXpDGHUucCQQC2tZlmL858suIA/+XfQVGoKOEvLlI5tGNDLXlDaKldY8UZGqxcyKaOsqEWMQnJCUy/0zTariN7kNssptYm04wjAkB3qVlt2lcizVn24rhAh+NjzlO7le8WQIn+t7m4UIWzFsQIHFwlynQSSkEYOTXcRY14avwnsT30Opm6WDj8Rs7PAkBytzqFSmbfLIFyFzmBH0Xhyyj3sqG10WixeQ+2HzSXiljqFjE6YFETL1yszkVSkCA8IKQC2Ws13hF+y5GR9yj5AkBAeUJj/a8wpdxJCufDpoaVUsB/XGK9XCqlGZvSy2TrjWLLBjZ3jiyjTlqIssfqI/IiJ4H2peocDaHXjFT0m+Av";
    private static final String priKey2 ="MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJE5lYYWx1AE5E0XaxmQvg2putYOrya+Azd+QCHVA0rE8vUiXAuFia7TgjGQjhFO66SrcOJ3W2xSeHw6F1ApvZ8kfLB3ZsZT1e54QaWKU0ae0fcmk9dgrkpaniLCbd3PP8A+UUjwNeexIDjThYSzzatNGg06qdxgSCMc9bX7jwPlAgMBAAECgYA5Vtcmvk+r1IKfvaNX0MJ5eo5+fgXB8jwq6PpBYW2PU/vptctJ8UvPb0t0bnLpepOnzNkhUacTOezAf988k35+gw9Vrh6rXG4x7cZ65qbQOP+Xh0sx3YElyZKUBJzl5CMjNzT5ANc/QdpCD8LOOiPF4xpcHqKih74NGXc8hQv1AQJBAMNm1i6AC/oJM7XDnXPyswNVyjjIG+wi9xBgMGlt8JLH3c7/HblyAPHS/sFnljroVsHOyvoi45aZlIluhZsh6tUCQQC+QygmlnViImHq+MgL6bWaTLjKyTpH6k7zGQxvOxqJlioeWh73wxHAq/depKi8ElMrkEhMBA05ReCJl4Nb8xzRAkA28/HiS/KSVAot4SCj3iqIEpV3mJd5tm+jNFoJHHke3oS71TWH1M79M2if/cDbOkJD6SNea3d0ACcs6185vLUtAkBtrWLw05z5JB7UB/Oxwli4iO+hnlxlZnF6e38Kg8SpeZHwCz18z8tlCPzBZyQJvnqJS1QR1egVku18A4Zqs/txAkAg5kjdDw0v9QoQMr4oHZSuHxaG9I91SRkCPspN8urIg1Wu7cdTKfPdaAtSoU/xp/qpzfX+CPkhv7DoGlBLeo8e";

    @Before
    public void before() {
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
    }

    @Test
    public void test() throws Exception {
        String url = "http://10.200.172.97:7070/transaction/submit";
// issue currency
      // List<SignedTransaction> txs = makeCurrencyTxs();
        //open account
//        List<SignedTransaction> txs = makeOpenAccountTxs();
        // transfer
       // List<SignedTransaction> txs = makeAccountingTxs();
        // freeze
        // List<SignedTransaction> txs = makeFreezeTxs();

        // unfreeze
      //  List<SignedTransaction> txs = makeUnFreezeTxs();

        // utxo issue and transfer
        // List<SignedTransaction> txs = makeCurrencyTxs();
        // utxo issue and  transfer
         List<SignedTransaction> txs = makeOpenAccountTxs();

        String params = JSON.toJSONString(txs);

        System.out.println("request.params:" + params);

        String res= OkHttpClientManager.postAsString(url,params);

        System.out.println("res.data:" + res);
    }


    public  List<SignedTransaction>  createPolicyTx() throws Exception {



        SignedTransaction signedTx1 = new SignedTransaction();

        CoreTransaction coreTx1 = new CoreTransaction();

        coreTx1.setTxId("pending-tx-test-1235");
        coreTx1.setActionList(initPoilicy());
        coreTx1.setPolicyId("000000");
        coreTx1.setLockTime(new Date());
        coreTx1.setBizModel(new JSONObject());
        coreTx1.setSender("TRUST-NODE97");
        coreTx1.setVersion("1.0.0");
        System.out.println(JSON.toJSONString(coreTx1));

        String sign1 = SignUtils.sign(JSON.toJSONString(coreTx1), priKey1);
                String sign2 = SignUtils.sign(JSON.toJSONString(coreTx1), priKey2);
        List<String> signList = new ArrayList<>();
        signList.add(sign1);
        signList.add(sign2);
        signedTx1.setCoreTx(coreTx1);
        signedTx1.setSignatureList(signList);
        List<SignedTransaction> txs = new ArrayList<>();
        txs.add(signedTx1);

        System.out.println("create  Policy tx:"+ JSONObject.toJSONString(txs));
        return txs;
    }




    private List<Action> initPoilicy() {

        RegisterPolicy registerPolicy = new RegisterPolicy();
        registerPolicy.setPolicyId("test-policy-1");
        registerPolicy.setPolicyName("测试注册policy-1");

        Set<String> rsIds = new HashSet<>();
        rsIds.add("TRUST-NODE97");
        registerPolicy.setRsIdSet(rsIds);
        registerPolicy.setType(ActionTypeEnum.REGISTER_POLICY);
        registerPolicy.setIndex(0);

        List<Action> registerPolicies = new ArrayList<>();
        registerPolicies.add(registerPolicy);
        return registerPolicies;
    }

    private List<SignedTransaction> makeCurrencyTxs() throws Exception {
        List<SignedTransaction> txs = new ArrayList<>();
        List<Action> actions = new ArrayList<>();
        Action action = TestDataMaker.makeCurrencyAction("CNY");
        actions.add(action);

        JSONObject bizModel = new JSONObject();
        bizModel.put("data",action);
        CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(actions, 0, "test-policy-1",bizModel);
        SignedTransaction tx = TestDataMaker.makeSignedTx(coreTransaction);

        txs.add(tx);

        return txs;
    }

    private List<SignedTransaction> makeOpenAccountTxs() throws Exception {
        List<SignedTransaction> txs = new ArrayList<>();
        List<Action> actions = new ArrayList<>();
        Action action = TestDataMaker.makeOpenAccountAction("test1101", FundDirectionEnum.DEBIT);
        actions.add(action);

        JSONObject bizModel = new JSONObject();
        bizModel.put("data",action);
        CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(actions, 0, "test-policy-1",bizModel);
        coreTransaction.setSender("TRUST-NODE97");
        SignedTransaction tx = TestDataMaker.makeSignedTx(coreTransaction);

        txs.add(tx);
        return txs;
    }


    private List<SignedTransaction> makeAccountingTxs() throws Exception {
        List<SignedTransaction> txs = new ArrayList<>();
        List<Action> actions = new ArrayList<>();
        Action action = TestDataMaker.makeOpertionAction("account_no_2_2", "account_no_1_1",new BigDecimal("100"));
        actions.add(action);

        JSONObject bizModel = new JSONObject();
        bizModel.put("data",action);
        CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(actions, 0, "test-policy-1",bizModel);
        SignedTransaction tx = TestDataMaker.makeSignedTx(coreTransaction);

        txs.add(tx);
        return txs;
    }


    private List<SignedTransaction> makeFreezeTxs() throws Exception {
        List<SignedTransaction> txs = new ArrayList<>();
        List<Action> actions = new ArrayList<>();
        Action action = TestDataMaker.makeFreezeAction("account_no_2_2", 0);
        actions.add(action);

        JSONObject bizModel = new JSONObject();
        bizModel.put("data",action);
        CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(actions, 0, "test-policy-1",bizModel);
        SignedTransaction tx = TestDataMaker.makeSignedTx(coreTransaction);

        txs.add(tx);
        return txs;
    }

    private List<SignedTransaction> makeUnFreezeTxs() throws Exception {
        List<SignedTransaction> txs = new ArrayList<>();
        List<Action> actions = new ArrayList<>();
        Action action = TestDataMaker.makeUnFreezeAction("account_no_2_2", "freeze_flow_no_1_0");
        actions.add(action);

        JSONObject bizModel = new JSONObject();
        bizModel.put("data",action);
        CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(actions, 0, "test-policy-1",bizModel);
        SignedTransaction tx = TestDataMaker.makeSignedTx(coreTransaction);

        txs.add(tx);
        return txs;
    }


    private List<SignedTransaction> makeUTXO_TransferTxs() throws Exception {
        List<SignedTransaction> txs = new ArrayList<>();
        List<Action> actions = new ArrayList<>();
        Action action = TestDataMaker.makeOpertionAction("account_no_2_2", "account_no_1_1",new BigDecimal("100"));
        actions.add(action);
        addUTXOActionList(actions);
        JSONObject bizModel = new JSONObject();
        bizModel.put("data",actions);
        CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(actions, 0, "test-policy-1",bizModel);
        SignedTransaction tx = TestDataMaker.makeSignedTx(coreTransaction);

        txs.add(tx);
        return txs;
    }


    private List<Action> addUTXOActionList(List<Action> actions) {
        UTXOAction utxoAction = new UTXOAction();

        List<TxIn> inputList = new ArrayList<>();
        TxIn txIn = new TxIn();
        txIn.setTxId("tx_id_ACCOUNTING_0_1524471486111");
        txIn.setIndex(0);
        txIn.setActionIndex(1);
        inputList.add(txIn);

        List<TxOut> outputList = new ArrayList<>();
        TxOut  txOut = new TxOut();
        JSONObject state = new JSONObject();
        state.put("amount",3);
        txOut.setState(state);
        txOut.setIndex(0);
        txOut.setActionIndex(0);
        txOut.setIdentity("yiyiliu");

        outputList.add(txOut);

        utxoAction.setInputList(inputList);
        utxoAction.setOutputList(outputList);
        utxoAction.setUtxoActionType(UTXOActionTypeEnum.NORMAL);
        String code ="function verify() {\n" + "    var action = ctx.getAction();\n" + "\tvar actionType = action.utxoActionType;\n" + "\tvar issueActionType = ctx.getUTXOActionType('ISSUE');\n" + "\tvar normalActionType = ctx.getUTXOActionType('NORMAL');\n" + "\tvar destructionActionType = ctx.getUTXOActionType('DESTRUCTION');\n" + "\t\n" + "\t//issue utxo  action\n" + "\tif(actionType == issueActionType){\n" + "\t\treturn true;\n" + "\t}\n" + "\t\n" + "    //notmal utxo  action\n" + "    if(actionType == normalActionType){\n" + "\t  if(action.inputList.length == 0 || action.getOutputList().length == 0){\n" + "\t \treturn false;\n" + "\t  }\n" + "\tvar utxoList = ctx.queryTxOutList(action.inputList);\n" + "\tvar inputsAmount = 0;\n" + "\tvar outputsAmount = 0;\n" + "\tutxoList.forEach(function (input) {inputsAmount += JSON.parse(input.getState()).amount;});\n" + "\taction.getOutputList().forEach(function (input) {outputsAmount += input.getState().amount;});\n" + "\treturn inputsAmount == outputsAmount;\n" + "\t}\n" + "\t\n" + "\t //destruction utxo  action\n" + "\tif(actionType == destructionActionType){\n" + "\t\treturn true;\n" + "\t}\n" + "   \n" + "\treturn false;\n" + "}";
        utxoAction.setContract(code);
        utxoAction.setType(ActionTypeEnum.UTXO);
        utxoAction.setStateClass("amount");
        utxoAction.setIndex(1);
        actions.add(utxoAction);
        return actions;
    }


}
