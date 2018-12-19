package com.higgs.trust.rs.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.evmcontract.solidity.Abi;
import com.higgs.trust.evmcontract.solidity.compiler.CompilationResult;
import com.higgs.trust.evmcontract.solidity.compiler.SolidityCompiler;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.RsBlockChainService;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.account.IssueCurrency;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.action.DataIdentityAction;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.contract.ContractCreationV2Action;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.TxOut;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import static com.higgs.trust.evmcontract.solidity.compiler.SolidityCompiler.Options.*;

/**
 * CoreTransaction Convertor
 *
 * @author lingchao
 * @create 2018年06月27日22:58
 */
@Slf4j @Service public class CoreTransactionConvertor {
    @Autowired private NodeState nodeState;
    @Autowired private RsBlockChainService rsBlockChainService;

    /**
     * build core transaction
     *
     * @param txId
     * @param actionList
     * @return
     */
    public CoreTransaction buildCoreTransaction(String txId, JSONObject bizModel, List<Action> actionList,
        String policyId) {
        CoreTransaction coreTransaction = new CoreTransaction();
        coreTransaction.setTxId(txId);
        coreTransaction.setBizModel(bizModel);
        coreTransaction.setActionList(actionList);
        coreTransaction.setVersion(VersionEnum.V1.getCode());
        coreTransaction.setSender(nodeState.getNodeName());
        coreTransaction.setSendTime(new Date());
        coreTransaction.setPolicyId(policyId);
        return coreTransaction;
    }

    /**
     * build txIn
     *
     * @param txId
     * @param actionIndex
     * @param index
     * @return
     */
    public TxIn buildTxIn(String txId, Integer actionIndex, Integer index) {
        TxIn txIn = new TxIn();
        txIn.setTxId(txId);
        txIn.setActionIndex(actionIndex);
        txIn.setIndex(index);
        return txIn;
    }

    /**
     * build txOut
     *
     * @param identity
     * @param actionIndex
     * @param index
     * @param state
     * @return
     */
    public TxOut buildTxOut(String identity, Integer actionIndex, Integer index, JSONObject state) {
        TxOut txOut = new TxOut();
        txOut.setIdentity(identity);
        txOut.setActionIndex(actionIndex);
        txOut.setIndex(index);
        txOut.setState(state);
        return txOut;
    }

    /**
     * build dataIdentityAction
     *
     * @param identity
     * @param index
     * @return
     */
    public DataIdentityAction buildDataIdentityAction(String identity, int index) {
        DataIdentityAction dataIdentityAction = new DataIdentityAction();
        dataIdentityAction.setDataOwner(nodeState.getNodeName());
        dataIdentityAction.setChainOwner(rsBlockChainService.queryChainOwner());
        dataIdentityAction.setIdentity(identity);
        dataIdentityAction.setIndex(index);
        dataIdentityAction.setType(ActionTypeEnum.CREATE_DATA_IDENTITY);
        return dataIdentityAction;
    }

    /**
     * build UTXOAction
     *
     * @param utxoActionTypeEnum
     * @param contractAddress
     * @param stateClass
     * @param index
     * @param inputList
     * @param txOutList
     * @return
     */
    public UTXOAction buildUTXOAction(UTXOActionTypeEnum utxoActionTypeEnum, String contractAddress, String stateClass,
        int index, List<TxIn> inputList, List<TxOut> txOutList) {
        UTXOAction utxoAction = new UTXOAction();
        utxoAction.setInputList(inputList);
        utxoAction.setOutputList(txOutList);
        utxoAction.setContractAddress(contractAddress);
        utxoAction.setType(ActionTypeEnum.UTXO);
        utxoAction.setStateClass(stateClass);
        utxoAction.setUtxoActionType(utxoActionTypeEnum);
        utxoAction.setIndex(index);
        return utxoAction;
    }

    /**
     * build currency action
     *
     * @param currency
     * @param index
     * @param remark
     * @return
     */
    public IssueCurrency buildIssueCurrencyAction(String currency, int index, String contractAddress,
        String homomorphicPk, String remark) {
        IssueCurrency currencyAction = new IssueCurrency();
        currencyAction.setCurrencyName(currency);
        currencyAction.setIndex(index);
        currencyAction.setContractAddress(contractAddress);
        currencyAction.setHomomorphicPk(homomorphicPk);
        currencyAction.setRemark(remark);
        currencyAction.setType(ActionTypeEnum.ISSUE_CURRENCY);
        return currencyAction;
    }

    /**
     * build currency action
     *
     * @param currency
     * @param index
     * @param remark
     * @return
     */
    public IssueCurrency buildIssueCurrencyAction(String currency, int index, String remark) {
        IssueCurrency currencyAction = new IssueCurrency();
        currencyAction.setCurrencyName(currency);
        currencyAction.setRemark(remark);
        currencyAction.setType(ActionTypeEnum.ISSUE_CURRENCY);
        currencyAction.setIndex(index);
        return currencyAction;
    }

    /**
     * build contract create v2 action
     *
     * @param fromAddress
     * @param contractAddress
     * @param contractHexCode
     * @param actionIndex
     * @return
     */
    public ContractCreationV2Action buildContractCreationV2Action(String fromAddress, String contractAddress,
        String contractHexCode, int actionIndex) {
        ContractCreationV2Action contractCreationV2Action = new ContractCreationV2Action();
        contractCreationV2Action.setFrom(fromAddress);
        contractCreationV2Action.setTo(contractAddress);
        contractCreationV2Action.setCode(contractHexCode);
        contractCreationV2Action.setVersion(VersionEnum.V1.getCode());
        contractCreationV2Action.setIndex(actionIndex);
        contractCreationV2Action.setType(ActionTypeEnum.CONTRACT_CREATION);
        return contractCreationV2Action;
    }

    public String buildContractCode(InputStream in, String contractor, Object... contractInitArgs) {
        try {
            String sourceCode = IOUtils.toString(in, Charsets.UTF_8);
            return buildContractCode(sourceCode, contractor, contractInitArgs);
        } catch (IOException e) {
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_CONTRACT_READ_ERROR, e);
        }
    }

    public String buildContractCode(String sourceCode, String contractor, Object... contractInitArgs) {
        try {
            SolidityCompiler.Result res =
                SolidityCompiler.compile(sourceCode.getBytes(), true, ABI, BIN, INTERFACE, METADATA);
            CompilationResult result = CompilationResult.parse(res.output);
            CompilationResult.ContractMetadata metadata = result.getContract(Abi.Function.of(contractor).name);
            byte[] codeBytes = Abi.Constructor.of(contractor, Hex.decode(metadata.bin), contractInitArgs);
            return Hex.toHexString(codeBytes);
        } catch (Throwable e) {
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_CONTRACT_BUILD_ERROR, e);
        }
    }

}
