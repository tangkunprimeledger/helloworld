package com.higgs.trust.slave;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.higgs.trust.common.crypto.KeyPair;
import com.higgs.trust.common.crypto.ecc.EccCrypto;
import com.higgs.trust.evmcontract.crypto.HashUtil;
import com.higgs.trust.evmcontract.facade.compile.CompileManager;
import com.higgs.trust.evmcontract.solidity.Abi;
import com.higgs.trust.evmcontract.solidity.compiler.CompilationResult;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.TxTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignInfo;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.contract.ContractCreationV2Action;
import com.higgs.trust.slave.model.bo.contract.ContractInvokeV2Action;
import it.unisa.dia.gas.plaf.jpbc.util.io.Base64;
import okhttp3.OkHttpClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.fastjson.FastJsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author Chen Jiawei
 * @date 2018-12-18
 */
public class BatchTransactionSenderTest {
    private static EccCrypto crypto;
    private static Pattern addressPattern;
    private static Retrofit retrofit;
    private static IPostSignedTransaction signedTransactionSender;

    @BeforeClass
    public static void startUp() {
        configJsonSerializer();

        crypto = EccCrypto.getSingletonInstance();
        addressPattern = Pattern.compile("^[0-9a-fA-F]{40}$");
        retrofit = HttpClient.getRetrofit("127.0.0.1", 7000);
        signedTransactionSender = retrofit.create(IPostSignedTransaction.class);
    }

    private static class HttpClient {
        private static Retrofit getRetrofit(String serverIp, int serverPort) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(60L, TimeUnit.SECONDS)
                    .writeTimeout(60L, TimeUnit.SECONDS)
                    .readTimeout(60L, TimeUnit.SECONDS)
                    .build();

            return new Retrofit.Builder()
                    .baseUrl(String.format("http://%s:%s/", serverIp, serverPort))
                    .addConverterFactory(FastJsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
        }
    }

    private interface IPostSignedTransaction {
        @POST("/transaction/post")
        Call<RespData> post(@Body SignedTransaction signedTransaction);
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

    @AfterClass
    public static void tearDown() {
        signedTransactionSender = null;
        retrofit = null;
        addressPattern = null;
        crypto = null;
    }

    @Test
    public void testSendTransactions() {
        sendTransactionsWithContractCreation();
        sendTransactionsWithContractInvocation();
    }

    private void sendTransactionsWithContractCreation() {
        String contractSenderAddress = "44140ed117f968181823ca021394152800b51214";
        String transactionSenderId = "TRUST-TEST0";
        String contractFileAbsolutePath = "C:\\MyDesktop\\code\\trust\\slave\\src\\test\\resources\\contracts\\STO.sol";
        String contractName = "STO";
        String constructorSignature = "STO()";
        Object[] constructorArgs = new Object[0];

        SignedTransaction signedTransaction = generateSignedTransactionWithContractCreation(
                contractSenderAddress, transactionSenderId, contractFileAbsolutePath,
                contractName, constructorSignature, constructorArgs);

        try {
            System.out.println(JSON.toJSONString(signedTransaction, true));
            for (int i = 0; i < 100; i++) {
                RespData respData = signedTransactionSender.post(signedTransaction).execute().body();
                System.out.println(respData.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendTransactionsWithContractInvocation() {
        String contractSenderAddress = "44140ed117f968181823ca021394152800b51214";
        String contractReceiverAddress = "ae117ed148181523ca0394122801051214f9680b";
        String transactionSenderId = "TRUST-TEST0";
        String contractMethodSignature = "(uint) update(address, uint)";
        Object[] methodArgs = new Object[]{"02ed117f90021416818ca415b594411281823340", 54};

        SignedTransaction signedTransaction = generateSignedTransactionWithContractInvocation(
                contractSenderAddress, contractReceiverAddress, transactionSenderId, contractMethodSignature, methodArgs);

        try {
            System.out.println(JSON.toJSONString(signedTransaction, true));
            for (int i = 0; i < 100; i++) {
                RespData respData = signedTransactionSender.post(signedTransaction).execute().body();
                System.out.println(respData.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SignedTransaction generateSignedTransactionWithContractInvocation(
            String contractSenderAddress,
            String contractReceiverAddress,
            String transactionSenderId,
            String contractMethodSignature,
            Object... methodArgs) {
        ContractInvokeV2Action contractInvokeV2Action = new ContractInvokeV2Action();
        contractInvokeV2Action.setType(ActionTypeEnum.CONTRACT_INVOKED);
        contractInvokeV2Action.setIndex(0);
        contractInvokeV2Action.setValue(new BigDecimal(0));
        contractInvokeV2Action.setMethodSignature(getContractMethodSignature(contractMethodSignature));
        contractInvokeV2Action.setArgs(methodArgs);
        contractInvokeV2Action.setFrom(getContractSenderAddress(contractSenderAddress));
        contractInvokeV2Action.setTo(getContractReceiverAddress(contractReceiverAddress));


        CoreTransaction coreTransaction = new CoreTransaction();
        coreTransaction.setTxId(Hex.toHexString(generateTransactionId()));
        coreTransaction.setPolicyId(InitPolicyEnum.CONTRACT_INVOKE.getPolicyId());
        coreTransaction.setActionList(Arrays.asList(new ContractInvokeV2Action[]{contractInvokeV2Action}));
        coreTransaction.setBizModel(null);
        coreTransaction.setLockTime(null);
        coreTransaction.setSendTime(new Date());
        coreTransaction.setSender(getTransactionSenderId(transactionSenderId));
        coreTransaction.setVersion("1.0.0");
        coreTransaction.setTxType(TxTypeEnum.INVOKECONTRACT.getCode());


        SignInfo signInfo = new SignInfo();
        signInfo.setOwner(getSignOwner(transactionSenderId));
        signInfo.setSign(crypto.sign(JSON.toJSONString(coreTransaction), generatePrivateKey()));
        signInfo.setSignType(SignInfo.SignTypeEnum.CONSENSUS);

        List<SignInfo> signInfoList = new ArrayList<>();
        signInfoList.add(signInfo);

        SignedTransaction signedTransaction = new SignedTransaction();
        signedTransaction.setCoreTx(coreTransaction);
        signedTransaction.setSignatureList(signInfoList);


        return signedTransaction;
    }

    private String getContractMethodSignature(String contractMethodSignature) {
        System.out.println("contractMethodSignature" + contractMethodSignature);
        return contractMethodSignature;
    }

    private SignedTransaction generateSignedTransactionWithContractCreation(
            String contractSenderAddress,
            String transactionSenderId,
            String contractFileAbsolutePath,
            String contractName,
            String constructorSignature,
            Object... constructorArgs) {
        ContractCreationV2Action contractCreationV2Action = new ContractCreationV2Action();
        contractCreationV2Action.setType(ActionTypeEnum.CONTRACT_CREATION);
        contractCreationV2Action.setIndex(0);
        contractCreationV2Action.setVersion("0.4.24");
        contractCreationV2Action.setCode(Hex.toHexString(generateDeployByteCode(
                contractFileAbsolutePath, contractName, constructorSignature, constructorArgs)));
        contractCreationV2Action.setFrom(getContractSenderAddress(contractSenderAddress));
        contractCreationV2Action.setTo(null);


        CoreTransaction coreTransaction = new CoreTransaction();
        coreTransaction.setTxId(Hex.toHexString(generateTransactionId()));
        coreTransaction.setPolicyId(InitPolicyEnum.CONTRACT_ISSUE.getPolicyId());
        coreTransaction.setActionList(Arrays.asList(new ContractCreationV2Action[]{contractCreationV2Action}));
        coreTransaction.setBizModel(null);
        coreTransaction.setLockTime(null);
        coreTransaction.setSendTime(new Date());
        coreTransaction.setSender(getTransactionSenderId(transactionSenderId));
        coreTransaction.setVersion("1.0.0");
        coreTransaction.setTxType(TxTypeEnum.CREATECONTRACT.getCode());


        SignInfo signInfo = new SignInfo();
        signInfo.setOwner(getSignOwner(transactionSenderId));
        signInfo.setSign(crypto.sign(JSON.toJSONString(coreTransaction), generatePrivateKey()));
        signInfo.setSignType(SignInfo.SignTypeEnum.CONSENSUS);

        List<SignInfo> signInfoList = new ArrayList<>();
        signInfoList.add(signInfo);

        SignedTransaction signedTransaction = new SignedTransaction();
        signedTransaction.setCoreTx(coreTransaction);
        signedTransaction.setSignatureList(signInfoList);


        return signedTransaction;
    }

    private String getSignOwner(String signOwner) {
        System.out.println("SignOwner: " + signOwner);
        return signOwner;
    }

    private String getTransactionSenderId(String transactionSenderId) {
        System.out.println("TransactionSenderId: " + transactionSenderId);
        return transactionSenderId;
    }

    private String getContractReceiverAddress(String contractReceiverAddress) {
        if (!addressPattern.matcher(contractReceiverAddress).matches()) {
            throw new IllegalArgumentException("Contract address must be hex string of 40 characters");
        }

        System.out.println("ContractReceiverAddress: " + contractReceiverAddress);
        return contractReceiverAddress;
    }

    private String getContractSenderAddress(String contractSenderAddress) {
        if (!addressPattern.matcher(contractSenderAddress).matches()) {
            throw new IllegalArgumentException("Contract sender address must be hex string of 40 characters");
        }

        System.out.println("ContractSenderAddress: " + contractSenderAddress);
        return contractSenderAddress;
    }

    private byte[] generateTransactionId() {
        byte[] transactionId = HashUtil.randomHash();
        System.out.println("TransactionId: " + Hex.toHexString(transactionId));

        return transactionId;
    }

    private byte[] generateDeployByteCode(String contractFileAbsolutePath,
                                          String contractName, String constructorSignature, Object... constructorArgs) {
        System.out.println("ContractFileAbsolutePath: " + contractFileAbsolutePath);
        System.out.println("ContractName: " + contractName);
        System.out.println("ConstructorSignature: " + constructorSignature);
        System.out.println("ConstructorArgs: " + Arrays.asList(constructorArgs));

        CompilationResult compilationResult = null;
        try {
            compilationResult = CompileManager.getCompilationResultByFile(contractFileAbsolutePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (compilationResult == null) {
            throw new NullPointerException("Compile contract results with null");
        }

        CompilationResult.ContractMetadata metadata = compilationResult.getContract(contractName);
        byte[] byteCode = Abi.Constructor.of(constructorSignature, Hex.decode(metadata.bin), constructorArgs);
        System.out.println("ByteCode: " + Hex.toHexString(byteCode));

        return byteCode;

    }

    private String generatePrivateKey() {
        KeyPair keyPair = crypto.generateKeyPair();
        String privateKey = base64ToHex(keyPair.getPriKey());
        String publicKey = base64ToHex(keyPair.getPubKey());
        System.out.println("KeyPair [privateKey=" + privateKey + ", publicKey" + publicKey + "]");
        return privateKey;
    }

    private String base64ToHex(String base64) {
        String hex = null;
        try {
            hex = Hex.toHexString(Base64.decode(base64));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hex;
    }
}
