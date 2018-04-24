package com.higgs.trust.slave.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.higgs.trust.common.utils.OkHttpClientManager;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.account.FundDirectionEnum;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.core.service.action.account.TestDataMaker;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.TxOut;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-04-20
 */
public class AccountControllerTest {

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
        String url = "http://10.200.172.99:7070/block/chain/submit_transaction";
// issue currency
//        List<SignedTransaction> txs = makeCurrencyTxs();
        //open account
    //     List<SignedTransaction> txs = makeOpenAccountTxs();
        // transfer
       // List<SignedTransaction> txs = makeAccountingTxs();
        // freeze
        // List<SignedTransaction> txs = makeFreezeTxs();

        // unfreeze
      //  List<SignedTransaction> txs = makeUnFreezeTxs();

        // utxo issue and  transfer
         List<SignedTransaction> txs = makeUTXO_TransferTxs();

        String params = JSON.toJSONString(txs);

        System.out.println("request.params:" + params);

        String res= OkHttpClientManager.postAsString(url,params);

        System.out.println("res.data:" + res);
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
        Action action = TestDataMaker.makeOpenAccountAction("account_no_2_2", FundDirectionEnum.DEBIT);
        actions.add(action);

        JSONObject bizModel = new JSONObject();
        bizModel.put("data",action);
        CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(actions, 0, "test-policy-1",bizModel);
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
