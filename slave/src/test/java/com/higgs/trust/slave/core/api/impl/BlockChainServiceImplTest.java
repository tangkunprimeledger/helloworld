package com.higgs.trust.slave.core.api.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.BlockChainService;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.api.enums.account.FundDirectionEnum;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.api.vo.BlockVO;
import com.higgs.trust.slave.api.vo.CoreTransactionVO;
import com.higgs.trust.slave.api.vo.QueryBlockVO;
import com.higgs.trust.slave.api.vo.QueryTxVO;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.account.OpenAccount;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.action.DataIdentityAction;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.TxOut;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author tangfashuang
 * @date 2018/04/14 16:54
 */
@Slf4j
public class BlockChainServiceImplTest extends BaseTest{

    private static final List<SignedTransaction> signedTxList = new ArrayList<>();

    private static final SignedTransaction signedTx = new SignedTransaction();

    private static final String priKey1 ="MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIsMM2JkqnQACN+RUqRF4UkW58XnjpjA+RP4mnA4uD9KCjlJeTxvY0yHDqsvIaiXr7Ge0QB1VAKq0xit2BWHfVU/XlO1tqK+ea7YxooeoglvOkddiJiYTZUNjKM5AhttG950PpzrmeUcl9YGEZ/DwKKee+8tqaDWdIEHBnplO6mVAgMBAAECgYBtrWwCmoDRCw30uv5C0VQIgObE9gdGekB9/kRjbHn4ggBae5gDkaDzxjxNztlv0GYnZqxY/jML/46PEuE06jBzGcOlBuobQJJ38pTg0pnNVHbkTckxfUIr1MYUDhtO18tJZUZuMbYMwwgZ9K9E0N8kjKXk+rRx+BDjlbxNPds6KQJBAMLS/HCXjAfJlzSEWqkBavAKoW+bBhZlkTH+DoNk/KidASgdFBqtPUf5w3U+j8dK4nvt8R9X7zGxRAYXpDGHUucCQQC2tZlmL858suIA/+XfQVGoKOEvLlI5tGNDLXlDaKldY8UZGqxcyKaOsqEWMQnJCUy/0zTariN7kNssptYm04wjAkB3qVlt2lcizVn24rhAh+NjzlO7le8WQIn+t7m4UIWzFsQIHFwlynQSSkEYOTXcRY14avwnsT30Opm6WDj8Rs7PAkBytzqFSmbfLIFyFzmBH0Xhyyj3sqG10WixeQ+2HzSXiljqFjE6YFETL1yszkVSkCA8IKQC2Ws13hF+y5GR9yj5AkBAeUJj/a8wpdxJCufDpoaVUsB/XGK9XCqlGZvSy2TrjWLLBjZ3jiyjTlqIssfqI/IiJ4H2peocDaHXjFT0m+Av";
    private static final String priKey2 ="MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJE5lYYWx1AE5E0XaxmQvg2putYOrya+Azd+QCHVA0rE8vUiXAuFia7TgjGQjhFO66SrcOJ3W2xSeHw6F1ApvZ8kfLB3ZsZT1e54QaWKU0ae0fcmk9dgrkpaniLCbd3PP8A+UUjwNeexIDjThYSzzatNGg06qdxgSCMc9bX7jwPlAgMBAAECgYA5Vtcmvk+r1IKfvaNX0MJ5eo5+fgXB8jwq6PpBYW2PU/vptctJ8UvPb0t0bnLpepOnzNkhUacTOezAf988k35+gw9Vrh6rXG4x7cZ65qbQOP+Xh0sx3YElyZKUBJzl5CMjNzT5ANc/QdpCD8LOOiPF4xpcHqKih74NGXc8hQv1AQJBAMNm1i6AC/oJM7XDnXPyswNVyjjIG+wi9xBgMGlt8JLH3c7/HblyAPHS/sFnljroVsHOyvoi45aZlIluhZsh6tUCQQC+QygmlnViImHq+MgL6bWaTLjKyTpH6k7zGQxvOxqJlioeWh73wxHAq/depKi8ElMrkEhMBA05ReCJl4Nb8xzRAkA28/HiS/KSVAot4SCj3iqIEpV3mJd5tm+jNFoJHHke3oS71TWH1M79M2if/cDbOkJD6SNea3d0ACcs6185vLUtAkBtrWLw05z5JB7UB/Oxwli4iO+hnlxlZnF6e38Kg8SpeZHwCz18z8tlCPzBZyQJvnqJS1QR1egVku18A4Zqs/txAkAg5kjdDw0v9QoQMr4oHZSuHxaG9I91SRkCPspN8urIg1Wu7cdTKfPdaAtSoU/xp/qpzfX+CPkhv7DoGlBLeo8e";

    private static final String priKey3 = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANDzTWjIRJ6Y3dKT4Z08/QuUMjj3OFSgt8qD9ZFgT3TXik44olP7O0gVJiL+tBtCuqsW6nU2BWt2S/1/SmGVq1dxco1VSCU/Dk7ReBTMRyZBOxfzdMnaTWMbiO+ETodJl3eQbK1miJyVbg7hLe7s/8xiH7AGsKkppW6GC7Kpb4zJAgMBAAECgYBORbYLuGmsF4uQ5ICxjDUmbz9ZA5MAcKwomsIU0UUyecN/hcuZNhWA7Rs6JLuHMroGeTEe8zuYg9n3fgV5BL4H96z3SBSrY+BsCf1CxYGXEVCHzlt6g8575MqtxIlqPXnpKr9S1663EtsCCJ93t5rZmMA7z8bUbFRTcrUsajYzAQJBAPynP0a6Pk5JlF0TW5vbzusZb3CsEdPTp39NxlHEx9v/2xuREti1CSVMhdm8ZDdC5hDoETZn4DTiBAF0Z5it6pkCQQDTt9uSFv16v+62yJIz0KE9EUZrLua1BlfTIyvgBZQ6Lp5ORS2S9iVzfOS77mufysbfGSpmD6Oc5ElY2coUy8GxAkAlFB5zMM4IC0Bc0IR3QTECy77RGE+deMhyJGXghjKWlNwBFa9gYmEvOiXCqKVEfurovEYaZ/A9kpXn6L9zZsKxAkAuym+IdfRHcKu9Uc6eDPnVmT/K6G6si15Vl2xW8mS0ByGNgtRzqlrUj0GuFx9KDXKuU81/CO3L+tgK/vceaXnBAkEAk+OjzXA0KXZGKm+O8/Vl8yiJQpuvpuO4cxy4E7nEAjevFip88p4tO03DVxjyq2Az7457q/T+C/Ohr1X9uS/v/Q==";

    private static final String priKey4 ="MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIsMM2JkqnQACN+RUqRF4UkW58XnjpjA+RP4mnA4uD9KCjlJeTxvY0yHDqsvIaiXr7Ge0QB1VAKq0xit2BWHfVU/XlO1tqK+ea7YxooeoglvOkddiJiYTZUNjKM5AhttG950PpzrmeUcl9YGEZ/DwKKee+8tqaDWdIEHBnplO6mVAgMBAAECgYBtrWwCmoDRCw30uv5C0VQIgObE9gdGekB9/kRjbHn4ggBae5gDkaDzxjxNztlv0GYnZqxY/jML/46PEuE06jBzGcOlBuobQJJ38pTg0pnNVHbkTckxfUIr1MYUDhtO18tJZUZuMbYMwwgZ9K9E0N8kjKXk+rRx+BDjlbxNPds6KQJBAMLS/HCXjAfJlzSEWqkBavAKoW+bBhZlkTH+DoNk/KidASgdFBqtPUf5w3U+j8dK4nvt8R9X7zGxRAYXpDGHUucCQQC2tZlmL858suIA/+XfQVGoKOEvLlI5tGNDLXlDaKldY8UZGqxcyKaOsqEWMQnJCUy/0zTariN7kNssptYm04wjAkB3qVlt2lcizVn24rhAh+NjzlO7le8WQIn+t7m4UIWzFsQIHFwlynQSSkEYOTXcRY14avwnsT30Opm6WDj8Rs7PAkBytzqFSmbfLIFyFzmBH0Xhyyj3sqG10WixeQ+2HzSXiljqFjE6YFETL1yszkVSkCA8IKQC2Ws13hF+y5GR9yj5AkBAeUJj/a8wpdxJCufDpoaVUsB/XGK9XCqlGZvSy2TrjWLLBjZ3jiyjTlqIssfqI/IiJ4H2peocDaHXjFT0m+Av";
    private static final String priKey5 ="MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIrjgTKcxdzf4pJf97wOrG6NixxPCIxQ+BLsIHLBgacwuKh5gDYhxRcuq2XCE/HX/Xi4RhL18x7CQ95OU6q4oPFfpevX6zxuBqWzQ7F0FH8KMAc82cbUZmXGV803wgjIVqBtwYkPznftx1v2deRnQVigLHoZlJtr4mgyPFkFF5WJAgMBAAECgYBZf/Gpo8z9YGio2p7R2MLVGvEh9OwVP7gcuXzPdlMOYh8cse8k8u4G2lQo5r/jgMQeHuFJJqTclWMWxnKz+PX8nsWvrjl0VFB9kZGWvcAHZXky0DQ8yActFYuxMZZT8eyFjHJ6kynjLGKpsvyYzDHH7PFPFUbDAMr4FIqt2zGngQJBAN5v9dMklCVGTHZIs5bm3+MINTZmaGa/TYFDj+p7R0oosThx0+uVFTtEhurzMN4V8RNieBleKjIEQNMp6y4lxO0CQQCf2FQSyK26oncARWEvCyjkIhisKRgidoVzJMiX3PTxiz14xDI4zpycEkoG3Y5idKdsoUwC5JUprt3n58nMFbuNAkAwLA0P0f8nZ2cNwsbp6kwYTeHAS0NW5R0y3l/fhx00SUXAFJ6xiVLUyA1z+oDdx+CyswORctwugs9LK+vbzaAtAkAkienmBVOZNywmtrVZcJ6fT5/+MsKeliM5R+5GsK6ZTG/33DlyvOAV2SRs31Z98RaYgWKDwsbKKXv2WAjMCye9AkAMfoZc2AKhgY/URsALVPOggcRybpZiNAo6iZSMSu7JG0Vkc0l2JPje2MTN6NpulikPfghoSDr0fpLbkR4CiVIQ";

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

    private List<Action> initPolicy() {
        RegisterPolicy registerPolicy = new RegisterPolicy();
        registerPolicy.setPolicyId("test-policy-2");
        registerPolicy.setPolicyName("测试注册policy-1");

        List<String> rsIds = new ArrayList<>();
        rsIds.add("TRUST-NODE33");
        registerPolicy.setRsIds(rsIds);
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
    public void createPolicyTx() throws Exception {



        SignedTransaction signedTx1 = new SignedTransaction();

        CoreTransaction coreTx1 = new CoreTransaction();

        coreTx1.setTxId("pending-tx-test-2");
        coreTx1.setActionList(initPolicy());
        coreTx1.setPolicyId("000000");
        coreTx1.setLockTime(new Date());
        coreTx1.setBizModel(new JSONObject());
        coreTx1.setSender("TRUST-NODE31");
        coreTx1.setVersion(VersionEnum.V1.getCode());

        System.out.println(JSON.toJSONString(coreTx1));

        String sign1 = SignUtils.sign(JSON.toJSONString(coreTx1), priKey4);
        String sign2 = SignUtils.sign(JSON.toJSONString(coreTx1), priKey5);
        List<String> signList = new ArrayList<>();
        signList.add(sign1);
        signList.add(sign2);
        signedTx1.setCoreTx(coreTx1);
        signedTx1.setSignatureList(signList);

        System.out.println("signedTx1: " + JSONObject.toJSONString(signedTx1));

//        System.out.println(JSON.toJSONString(signedTx));

//        signedTx1.setCoreTx(coreTx1);
//        signedTx1.setSignatureList(signList);
        signedTxList.add(signedTx1);

        System.out.println("create  Policy tx: "+ JSONObject.toJSONString(signedTxList));
    }


    @Test
    public void createIdentityTx() throws Exception {
        SignedTransaction signedTx1 = new SignedTransaction();


        CoreTransaction coreTx1 = new CoreTransaction();

        coreTx1.setTxId("createIdentity-123456789"+System.currentTimeMillis());

        coreTx1.setActionList(initUTXOIdentityList());
        coreTx1.setPolicyId("test-policy-1");
        coreTx1.setLockTime(new Date());
        coreTx1.setSender("TRUST-NODE97");
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
        dataIdentityAction.setDataOwner("TRUST-NODE97");
        dataIdentityAction.setIdentity("ling");
        dataIdentityAction.setIndex(0);
        dataIdentityAction.setType(ActionTypeEnum.CREATE_DATA_IDENTITY);


        DataIdentityAction dataIdentityAction1 = new DataIdentityAction();
        dataIdentityAction1.setChainOwner("TRUST");
        dataIdentityAction1.setDataOwner("TRUST-NODE97");
        dataIdentityAction1.setIdentity("chao");
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
        utxoAction.setContractAddress(code);
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
        utxoAction.setContractAddress(code);
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
        utxoAction.setContractAddress(code);
        utxoAction.setType(ActionTypeEnum.UTXO);
        utxoAction.setStateClass("amount");
        List<Action> utxoActionList = new ArrayList<>();
        utxoActionList.add(utxoAction);
        return utxoActionList;
    }


    @Test
    public void testSubmitTransaction() {
        try {
            System.out.println(JSON.toJSONString(signedTx));
//            RespData respData = blockChainService.submitTransaction(signedTx);
//            Assert.assertEquals("000000", respData.getRespCode());
//            Assert.assertEquals("success", respData.getMsg());
//            System.out.println(respData);
        } catch (Throwable e) {
            System.out.println(e);
        }
    }

    @Test
    public void testQueryBlocksWithCondition() {
        QueryBlockVO req = new QueryBlockVO();
        req.setPageNum(1);
        req.setPageSize(10);
        req.setHeight(2L);
//        req.setBlockHash("48f662666b5ad8869c21026d588ba5024d47cdaa67334ce83bd088cad55b58f4");

        List<BlockVO> list = blockChainService.queryBlocks(req);
        for (BlockVO blockVO : list) {
            System.out.println(blockVO);
        }
    }

    @Test
    public void testQueryTxsWithCondition() {
        QueryTxVO req = new QueryTxVO();
        req.setTxId("tx_id_OPEN_ACCOUNT_0_1526047995917main");
        req.setSender("TRUST-NODE97");
        req.setPageNum(2);
        req.setPageSize(50);

        req.setBlockHeight(82L);
        List<CoreTransactionVO> list = blockChainService.queryTransactions(req);
        log.error("list-size={}", list.size());
        System.out.println();
        list.forEach(coreTxVo -> {
            System.out.println(coreTxVo);
        });
    }

}