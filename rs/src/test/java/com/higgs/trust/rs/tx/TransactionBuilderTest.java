package com.higgs.trust.rs.tx;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.higgs.trust.common.crypto.ecc.EccCrypto;
import com.higgs.trust.evmcontract.crypto.HashUtil;
import com.higgs.trust.rs.core.bo.ContractQueryRequestV2;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.TxTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignInfo;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.contract.ContractInvokeV2Action;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.spongycastle.util.encoders.Hex;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author kongyu
 * @date 2018-12-07
 * @desc
 */
@Slf4j
public class TransactionBuilderTest {

    //服务地址
    private final static String SERVICE_URL = "http://localhost:7070/transaction/post";
    private final static String QUERY_SERVICE_URL = "http://localhost:7070//contract/query2";

    private final static String RESULT_SERVICE_URL = "http://localhost:7070/transaction/result/%s";
    private static final String PUBLIC_KEY = "0487491467d359059f73d18444f964c3b795ee91e3ccafca7b3bc58d1459a8610105132999a09e0cef35747d7a6ee86a20f14c79ffaf9c0453cc020a3ced310fda";
    private static final String PRIVATE_KEY = "78d4646b8baa8cfbe4c02b9d245e4ee2406af092955d705d91e83238e012b707";
    private static final String STO_CONTRACT_ADDRESS = "095e7baea6a6c7c4c1dfeb977efac316af552989";
    private CoreTransaction coreTx;
    private String privateKey;


    public TransactionBuilderTest() {

    }

    private static void configJsonSerializer() {
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
    }

    public static void main(String[] args) throws Exception {
        configJsonSerializer();

        TransactionBuilderTest builder = new TransactionBuilderTest();
        String from = "81dac5ede88d38dfef6abb481449e5f9e84ce4db";
        String to = "16792c325e746d5dd2e4e64f076e1ac11c3cb092";
        String method = "(bool) transferFrom(address, address, uint256)";
        String transTo = generationAddress();
        int amount = 10;
        Object[] argsObj = {from, transTo, amount};
        Action action = builder.buildContractInvokeV2Action(from, to, method, argsObj);
        SignedTransaction signedTx = builder.withAction(action).withPrivateKey(PRIVATE_KEY).build();
        HttpUtils.postJson(SERVICE_URL, signedTx);

        System.out.println(JSON.toJSONString(signedTx, true));
        //验证结果
        TimeUnit.SECONDS.sleep(3);

        String requestMethod = "(uint,uint) balanceOf(address)";
        Object[] parameters = {transTo};
        ContractQueryRequestV2 contractQueryRequestV2 = new ContractQueryRequestV2();
        contractQueryRequestV2.setBlockHeight(-1);
        contractQueryRequestV2.setAddress(to);
        contractQueryRequestV2.setMethodSignature(requestMethod);
        contractQueryRequestV2.setParameters(parameters);
        //log.info("验证请求参数:{}", );
        String jsonResult = HttpUtils.postJson(QUERY_SERVICE_URL, contractQueryRequestV2);
        JSONObject result = JSONObject.parseObject(jsonResult);
        Assert.assertEquals(true, result.getBoolean("success"));
        Assert.assertEquals(amount, ((List) result.get("data")).get(0));


//        Map queryResult = HttpUtils.get(String.format(RESULT_SERVICE_URL, signedTx.getCoreTx().getTxId()), Map.class);
//        log.info("froze contract deploy result:{}", queryResult);
//        Assert.assertNotNull(queryResult);
//        Assert.assertEquals(true, (Boolean) queryResult.get("success"));


        //  action.setMethodSignature("(bool) transferFrom(address, address, uint256)");
        //  action.setMethodSignature("(uint) getBalance(address)");

        // action.setMethodSignature("(bool) transferToContract(address, uint, uint)");
//        action.setMethodSignature("(bool) transfer(address,uint)");
//        action.setMethodSignature("(bool) freeze(address,uint)");
//        action.setMethodSignature("(bool) unfreeze(address,uint)");
//        action.setMethodSignature("(bool) frozeAccount(address,uint,uint)");
//        action.setMethodSignature("(bool) unfrozeAccount(address,uint)");
//        action.setMethodSignature("(bool) additionalIssue(uint)");
//        action.setMethodSignature("(bool) addAllowedAddr(address[])");
//        action.setMethodSignature("(address[], uint[], string, uint) getUserInfo()");
//        action.setMethodSignature("(address[], uint[], string, uint) getUserInfo()");
        //
//        action.setArgs(transferToContractArgs());
//        action.setArgs(addAllowedAddr());
        // action.setArgs(new Object[]{"397b2a371e6d98a120e5f139420832af33369aec", "ba1172564007825d0777f740da2efeab3cc78aee", 1});
        //action.setArgs(new Object[]{STO_CONTRACT_ADDRESS, 1000, 10});
//        action.setArgs(new Object[]{"6c218d6856e5182d33e00813d9e861255f527da2",3,1644839806});
    }
    
    /**
     * generation  contract address
     *
     * @return contract address
     */
    public static String generationAddress() {
        int length = 40;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            Integer rand = new Random().nextInt(16);
            sb.append(Integer.toHexString(rand));
        }
        log.info("contract address:{}", sb.toString());
        return sb.toString();
    }

    private Action buildContractInvokeV2Action(String from, String to, String method, Object[] args) {
        coreTx = buildCoreTransaction();
        privateKey = PRIVATE_KEY;
        ContractInvokeV2Action action = new ContractInvokeV2Action();
        action.setIndex(0);
        action.setType(ActionTypeEnum.CONTRACT_INVOKED);
        action.setFrom(from);
        action.setTo(to);
        action.setMethodSignature(method);
        action.setArgs(args);
        action.setValue(new BigDecimal("0"));
        return action;
    }

    private CoreTransaction buildCoreTransaction() {
        CoreTransaction tx = new CoreTransaction();
        List<Action> actionList = new ArrayList<>();

        tx.setSender("TRUST-TEST0");
        tx.setSendTime(new Date());
        tx.setTxId(Hex.toHexString(HashUtil.randomHash()));
        tx.setPolicyId(InitPolicyEnum.CONTRACT_INVOKE.getPolicyId());
        tx.setVersion("1.0.0");
        tx.setTxType(TxTypeEnum.INVOKECONTRACT.getCode());
        tx.setActionList(actionList);
        return tx;
    }

    public TransactionBuilderTest withAction(Action action) {
        coreTx.getActionList().add(action);
        return this;
    }

    public TransactionBuilderTest withTxType(TxTypeEnum txType) {
        coreTx.setTxType(txType.getCode());
        return this;
    }

    public TransactionBuilderTest withPolicyId(String policyId) {
        coreTx.setPolicyId(policyId);
        return this;
    }

    public TransactionBuilderTest withSender(String sender) {
        coreTx.setSender(sender);
        return this;
    }

    public TransactionBuilderTest withTxId(String txId) {
        coreTx.setTxId(txId);
        return this;
    }

    public TransactionBuilderTest withPrivateKey(String privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    public SignedTransaction build() {
        SignedTransaction signedTransaction = new SignedTransaction();
        signedTransaction.setCoreTx(coreTx);
        System.out.println(JSON.toJSONString(coreTx, true));
        System.out.println("--------------------------------------------");

        String sign = EccCrypto.getSingletonInstance().sign(JSON.toJSONString(coreTx), privateKey);
        List<SignInfo> signInfos = new ArrayList<>();
        SignInfo signInfo = new SignInfo();
        signInfo.setOwner("TRUST-TEST0");
        signInfo.setSign(sign);
        signInfo.setSignType(SignInfo.SignTypeEnum.CONSENSUS);
        signInfos.add(signInfo);
        signedTransaction.setSignatureList(signInfos);

        return signedTransaction;
    }

    private Object[] transferToContractArgs() {
        return new Object[]{STO_CONTRACT_ADDRESS, 1000, 10};
    }

    private Object[] addAllowedAddr() {
        ArrayList<Object> list = new ArrayList<>(1);
//        list.add(standardOfferAddress);
        list.add("6c218d6856e5182d33e00813d9e861255f527da9");
        return new Object[]{list};
    }
}
