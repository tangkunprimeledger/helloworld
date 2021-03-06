package com.higgs.trust.rs.tx;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.higgs.trust.common.crypto.ecc.EccCrypto;
import com.higgs.trust.evmcontract.crypto.HashUtil;
import com.higgs.trust.evmcontract.solidity.Abi;
import com.higgs.trust.evmcontract.solidity.compiler.CompilationResult;
import com.higgs.trust.evmcontract.solidity.compiler.SolidityCompiler;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.TxTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignInfo;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.contract.ContractCreationV2Action;
import com.higgs.trust.slave.model.bo.contract.ContractInvokeV2Action;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.higgs.trust.evmcontract.solidity.compiler.SolidityCompiler.Options.*;

/**
 * @author duhongming
 * @date 2018/12/6
 */
@Slf4j
public class TransactionBuilder extends AutoTestContext {


    public static final String STANDARD_TOKEN_CONTRACT_ADDRESS = "095e7baea6a6c7c4c2dfeb977efac316af552989";

    private static final String PRIVATE_KEY = "78d4646b8baa8cfbe4c02b9d245e4ee2406af092955d705d91e83238e012b707";
    //合约目录
    private static final String BASE_PATH = Paths.get("src/test/resources/contracts").toFile().getAbsolutePath();
    //冻结合约名称
    private final static String CONTRACT_NAME_OF_FROZE = "Froze";
    //标准币合约名称
    private final static String CONTRACT_NAME_OF_STANDARD_CURRENCY = "StandardCurrency";
    //STO合约名称
    private final static String CONTRACT_NAME_OF_STANDARD_STO = "StandardSTO";
    //服务地址
    private final static String SERVICE_URL = "http://localhost:7070/transaction/post";

    private final static String QUERY_SERVICE_URL = "http://localhost:7070/transaction/result/%s";
    //批次计数器
    public static int count = 1;
    //
    private static String frozeOfferFromAddress = "7f03989fbc86c7c0bf93deb35a27a5cf22848f93";

    private CoreTransaction coreTx;
    private String privateKey;

    public TransactionBuilder() {

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

    @Test
    public void deploy() throws Exception {


        configJsonSerializer();

        TransactionBuilder builder = new TransactionBuilder();
        /**
         * 1:部署Froze
         * 2：部署StandardCurrency 依赖Froze 地址
         * 3：部署 StandardSTO 依赖Froze地址和StandardCurrency地址
         */
        //******************部署冻结合约**********************/
        String frozeAddress = generationAddress();
        Froze froze = new Froze(frozeOfferFromAddress, frozeAddress);
        setFroze(froze);
        log.info("frozeAddress is:{}", frozeAddress);

        Action action = builder.buildFrozeContractAction(CONTRACT_NAME_OF_FROZE, frozeAddress);
        SignedTransaction signedTx = builder.withAction(action).withPrivateKey(PRIVATE_KEY).build();
        log.info("froze contract deploy request:{}", JSON.toJSONString(signedTx, true));
        HttpUtils.postJson(SERVICE_URL, signedTx);


        //验证结果
//        TimeUnit.SECONDS.sleep(3);
//        Map queryResult = HttpUtils.get(String.format(QUERY_SERVICE_URL, signedTx.getCoreTx().getTxId()), Map.class);
//        log.info("froze contract deploy result:{}", queryResult);
//        Assert.assertNotNull(queryResult);
//        Assert.assertEquals(true, (Boolean) queryResult.get("success"));
//        Assert.assertEquals(frozeAddress, queryResult.get("createdAddress"));
        //******************批量部署STO合约**********************/
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for (int i = 0; i < getDeployNum(); i++) {
            executorService.submit((Runnable) () -> {
                //deploy currency contract
                String currencyFrom = generationAddress();
                String currencyTo = generationAddress();
                log.info("currencyFrom is:{},  currencyTo:{}", currencyFrom, currencyTo);
                Action action2 = builder.buildCreateContractAction(CONTRACT_NAME_OF_STANDARD_CURRENCY, frozeAddress, null, currencyFrom, currencyTo);
                SignedTransaction signedTx2 = builder.withAction(action2).withPrivateKey(PRIVATE_KEY).build();
                //log.info("standCurrency contract deploy request:{}", JSON.toJSONString(signedTx2, true));
                HttpUtils.postJson(SERVICE_URL, signedTx2);
                Currency cu = new Currency(currencyFrom, currencyTo, new Random().nextInt(100000) + 10000);

                //deploy sto contract
                String stoFrom = generationAddress();
                String stoTo = generationAddress();
                log.info("stoFrom is:{},  stoTo:{}", stoFrom, stoTo);
                action2 = builder.buildCreateContractAction(CONTRACT_NAME_OF_STANDARD_STO, frozeAddress, currencyTo, stoFrom, stoTo);
                signedTx2 = builder.withAction(action2).withPrivateKey(PRIVATE_KEY).build();
                HttpUtils.postJson(SERVICE_URL, signedTx2);
                //  log.info("sto contract deploy result:{}", JSON.toJSONString(signedTx2, true));

                STO sto = new STO(stoFrom, stoTo, new Random().nextInt(10000000) + 10000);
                cu.setSto(sto);
                froze.getCurrencyList().add(cu);

                count++;
            });
        }

        //执行转账操作
//        List<Currency> cuList = froze.getCurrencyList();
//        for (Currency c : cuList) {
//            String from = "81dac5ede88d38dfef6abb481449e5f9e84ce4db";
//            String to = "16792c325e746d5dd2e4e64f076e1ac11c3cb092";
//            String method = "(bool) transferFrom(address, address, uint256)";
//            String transTo = generationAddress();
//            int amount = 1;
//            Object[] argsObj = {from, transTo, amount};
//            Action action3 = builder.buildContractInvokeV2Action(from, to, method, argsObj);
//            SignedTransaction signedTx3 = builder.withAction(action).withPrivateKey(PRIVATE_KEY).build();
//            HttpUtils.postJson(SERVICE_URL, signedTx);
//
//            System.out.println(JSON.toJSONString(signedTx, true));
//        }

        while (!executorService.isShutdown()) {
        }
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

    private String getCode(String contractName, String frozeAddress, String standardCurrencyAddress, String from) {


        try {
            Path source = Paths.get(BASE_PATH + contractName + ".sol");
            SolidityCompiler.Option allowPathsOption = new SolidityCompiler.Options.AllowPaths(Collections.singletonList(source.getParent().getParent()));
            SolidityCompiler.Result res = SolidityCompiler.compile(source.toFile(), true, ABI, BIN, INTERFACE, METADATA, allowPathsOption);
            CompilationResult result = CompilationResult.parse(res.output);
            CompilationResult.ContractMetadata metadata = result.getContract(contractName);

            byte[] code = null;
            if (contractName.equals(CONTRACT_NAME_OF_FROZE)) {
                code = Abi.Constructor.of(contractName, Hex.decode(metadata.bin));
            } else if (contractName.equals(CONTRACT_NAME_OF_STANDARD_CURRENCY)) {
                code = getStandardCurrencyCode(Hex.decode(metadata.bin), frozeAddress, from);
            } else if (contractName.equals(CONTRACT_NAME_OF_STANDARD_STO)) {
                code = getSTOCode(Hex.decode(metadata.bin), standardCurrencyAddress, frozeAddress, from);
            }
            return Hex.toHexString(code);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Action buildCreateContractAction(String contractName, String frozeAddress, String standardCurrencyAddress, String from, String to) {
        ContractCreationV2Action action = new ContractCreationV2Action();
        action.setIndex(0);
        action.setType(ActionTypeEnum.CONTRACT_CREATION);
        action.setCode(getCode(contractName, frozeAddress, standardCurrencyAddress, from));
        action.setVersion("0.4.25");
        action.setFrom(from);
        action.setTo(to);
        return action;
    }

    private Action buildFrozeContractAction(String contractName, String currentContractAddress) {
        ContractCreationV2Action action = new ContractCreationV2Action();
        action.setIndex(0);
        action.setType(ActionTypeEnum.CONTRACT_CREATION);
        action.setCode(getCode(contractName, null, null, null));
        action.setVersion("0.4.25");
        action.setFrom(frozeOfferFromAddress);
        action.setTo(currentContractAddress);
        return action;
    }

    private CoreTransaction buildCoreTransaction() {
        CoreTransaction tx = new CoreTransaction();
        List<Action> actionList = new ArrayList<>();

        tx.setSender("TRUST-TEST0");
        tx.setSendTime(new Date());
        tx.setTxId(Hex.toHexString(HashUtil.randomHash()));
        tx.setPolicyId(InitPolicyEnum.CONTRACT_ISSUE.getPolicyId());
        tx.setVersion("1.0.0");
        tx.setTxType(TxTypeEnum.CONTRACT.getCode());
        tx.setActionList(actionList);
        return tx;
    }

    public TransactionBuilder withAction(Action action) {
        coreTx = buildCoreTransaction();
        privateKey = PRIVATE_KEY;
        coreTx.getActionList().add(action);
        return this;
    }

    public TransactionBuilder withTxType(TxTypeEnum txType) {
        coreTx.setTxType(txType.getCode());
        return this;
    }

    public TransactionBuilder withPolicyId(String policyId) {
        coreTx.setPolicyId(policyId);
        return this;
    }

    public TransactionBuilder withSender(String sender) {
        coreTx.setSender(sender);
        return this;
    }

    public TransactionBuilder withTxId(String txId) {
        coreTx.setTxId(txId);
        return this;
    }

    public TransactionBuilder withPrivateKey(String privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    public SignedTransaction build() {
        SignedTransaction signedTransaction = new SignedTransaction();
        signedTransaction.setCoreTx(coreTx);

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

    private byte[] getStandardCurrencyCode(byte[] code, String FROZEN_CONTRACT_ADDRESS, String standardOfferAddress) {
        byte[] bytes = null;
        String contractName = "StandardCurrency(address,string,string,uint,uint8,address)";
        String tokenName = "standard_token";
        String tokenSymbol = "STA";
        int initNum = 1000;
        int decimals = 8;

        bytes = Abi.Constructor.of(contractName, code,
                standardOfferAddress, tokenName, tokenSymbol, decimals, initNum, FROZEN_CONTRACT_ADDRESS);
        return bytes;
    }

    private byte[] getSTOCode(byte[] code, String STANDARD_TOKEN_CONTRACT_ADDRESS, String FROZEN_CONTRACT_ADDRESS, String from) {
        byte[] bytes = null;
        String contractName = "StandardSTO(address,string,string,uint8,uint," +
                "string,uint32,uint8,uint32,uint,uint,uint32,uint32,uint16,address,address)";
        String tokenName = "security_token";
        String tokenSymbol = "STO_1";
        int decimals = 0;
        int totalSupply = 100000;
        String standardCurrencySymbol = "STA";
        int exchangeRatio = 100;
        int exchangeRateDecimals = 8;
        int lowestShareNum = 10;
        int maxShareNum = 100;
        int totalSubscribedNum = 80000;
        //2018-12-15 15:30:00  时间单位：秒
        long subScriptionStartDate = 1544772600;
        //2018-12-14 16:30:00
        long subScriptionEndDate = 1544776200;
        int lockupPeriodDay = 2;
        bytes = Abi.Constructor.of(contractName, code,
                from, tokenName, tokenSymbol, decimals,
                totalSupply, standardCurrencySymbol, exchangeRatio, exchangeRateDecimals,
                lowestShareNum, maxShareNum, totalSubscribedNum, subScriptionStartDate,
                subScriptionEndDate, lockupPeriodDay, STANDARD_TOKEN_CONTRACT_ADDRESS, FROZEN_CONTRACT_ADDRESS);
        return bytes;
    }
}
