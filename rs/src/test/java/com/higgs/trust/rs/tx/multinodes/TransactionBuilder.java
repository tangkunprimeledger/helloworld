package com.higgs.trust.rs.tx.multinodes;

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
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignInfo;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.contract.ContractCreationV2Action;
import com.higgs.trust.slave.model.bo.contract.ContractInvokeV2Action;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Chen Jiawei
 * @date 2019-01-03
 */
public class TransactionBuilder {
    private static EccCrypto crypto;
    private static Pattern addressPattern;

    static {
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

    public TransactionBuilder() {
        crypto = EccCrypto.getSingletonInstance();
        addressPattern = Pattern.compile("^[0-9a-fA-F]{40}$");
    }

    public SignedTransaction generateSignedTransactionWithContractInvocation(
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

    public SignedTransaction generateSignedTransactionWithContractCreation(
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
        contractCreationV2Action.setTo(getReceiverAddress());


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

    private String getReceiverAddress() {
        byte[] bytes = HashUtil.randomHash();
        String receiverAddress = Hex.toHexString(HashUtil.calcNewAddr(bytes, bytes));
        System.out.println("ReceiverAddress: " + receiverAddress);
        return receiverAddress;
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
        String privateKey = keyPair.getPriKey();
        String publicKey = keyPair.getPubKey();
        System.out.println("KeyPair [privateKey=" + privateKey + ", publicKey" + publicKey + "]");
        return privateKey;
    }
}
