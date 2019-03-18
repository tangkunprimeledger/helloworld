package com.higgs.trust.rs.tx.sender;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.higgs.trust.common.crypto.ecc.EccCrypto;
import com.higgs.trust.evmcontract.crypto.HashUtil;
import com.higgs.trust.evmcontract.facade.compile.CompileManager;
import com.higgs.trust.evmcontract.solidity.Abi;
import com.higgs.trust.evmcontract.solidity.compiler.CompilationResult;
import com.higgs.trust.rs.core.bo.ContractQueryRequestV2;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.TxTypeEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignInfo;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.contract.ContractCreationV2Action;
import com.higgs.trust.slave.model.bo.contract.ContractInvokeV2Action;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class TransactionBuilder {
    private static final EccCrypto CRYPTO = EccCrypto.getSingletonInstance();
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^[0-9a-fA-F]{40}$");

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


    public static ContractQueryRequestV2 generateContractQueryRequestV2(long height, String methodSignature, Object[] parameters, String receiverAddress) {
        ContractQueryRequestV2 contractQueryRequestV2 = new ContractQueryRequestV2();

        contractQueryRequestV2.setBlockHeight(height);
        contractQueryRequestV2.setMethodSignature(methodSignature);
        contractQueryRequestV2.setParameters(parameters);
        contractQueryRequestV2.setAddress(receiverAddress);

        return contractQueryRequestV2;
    }

    public static SignedTransaction generateSignedTransactionWithContractInvocation(
            String contractSenderAddress,
            String contractReceiverAddress,
            String transactionSenderId,
            String contractMethodSignature,
            String privateKey,
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
        signInfo.setSign(CRYPTO.sign(JSON.toJSONString(coreTransaction), getPrivateKey(privateKey)));
        signInfo.setSignType(SignInfo.SignTypeEnum.CONSENSUS);

        List<SignInfo> signInfoList = new ArrayList<>();
        signInfoList.add(signInfo);

        SignedTransaction signedTransaction = new SignedTransaction();
        signedTransaction.setCoreTx(coreTransaction);
        signedTransaction.setSignatureList(signInfoList);


        return signedTransaction;
    }

    public static SignedTransaction generateSignedTransactionWithContractCreation(
            String contractSenderAddress,
            String transactionSenderId,
            String contractFileAbsolutePath,
            String contractName,
            String constructorSignature,
            String privateKey,
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
        signInfo.setSign(CRYPTO.sign(JSON.toJSONString(coreTransaction), getPrivateKey(privateKey)));
        signInfo.setSignType(SignInfo.SignTypeEnum.CONSENSUS);

        List<SignInfo> signInfoList = new ArrayList<>();
        signInfoList.add(signInfo);

        SignedTransaction signedTransaction = new SignedTransaction();
        signedTransaction.setCoreTx(coreTransaction);
        signedTransaction.setSignatureList(signInfoList);


        return signedTransaction;
    }


    private static String getContractMethodSignature(String contractMethodSignature) {
        log.info("contractMethodSignature: " + contractMethodSignature);
        return contractMethodSignature;
    }

    private static String getReceiverAddress() {
        byte[] bytes = HashUtil.randomHash();
        String receiverAddress = Hex.toHexString(HashUtil.calcNewAddr(bytes, bytes));
        log.info("ReceiverAddress: " + receiverAddress);
        return receiverAddress;
    }

    private static String getSignOwner(String signOwner) {
        log.info("SignOwner: " + signOwner);
        return signOwner;
    }

    private static String getTransactionSenderId(String transactionSenderId) {
        log.info("TransactionSenderId: " + transactionSenderId);
        return transactionSenderId;
    }

    private static String getContractReceiverAddress(String contractReceiverAddress) {
        if (!ADDRESS_PATTERN.matcher(contractReceiverAddress).matches()) {
            throw new IllegalArgumentException("Contract address must be hex string of 40 characters");
        }

        log.info("ContractReceiverAddress: " + contractReceiverAddress);
        return contractReceiverAddress;
    }

    private static String getContractSenderAddress(String contractSenderAddress) {
        if (!ADDRESS_PATTERN.matcher(contractSenderAddress).matches()) {
            throw new IllegalArgumentException("Contract sender address must be hex string of 40 characters");
        }

        log.info("ContractSenderAddress: " + contractSenderAddress);
        return contractSenderAddress;
    }

    private static byte[] generateTransactionId() {
        byte[] transactionId = HashUtil.randomHash();
        log.info("TransactionId: " + Hex.toHexString(transactionId));

        return transactionId;
    }

    private static byte[] generateDeployByteCode(String contractFileAbsolutePath,
                                                 String contractName, String constructorSignature, Object... constructorArgs) {
        log.info("ContractFileAbsolutePath: " + contractFileAbsolutePath);
        log.info("ContractName: " + contractName);
        log.info("ConstructorSignature: " + constructorSignature);
        log.info("ConstructorArgs: " + Arrays.asList(constructorArgs));

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
        log.info("ByteCode: " + Hex.toHexString(byteCode));

        return byteCode;
    }

    private static String getPrivateKey(String privateKey) {
        log.info("PrivateKey: " + privateKey);
        return privateKey;
    }
}
