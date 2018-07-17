package com.higgs.trust.rs.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.rs.core.api.RsBlockChainService;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.api.enums.utxo.UTXOActionTypeEnum;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.action.DataIdentityAction;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.TxOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * CoreTransaction Convertor
 *
 * @author lingchao
 * @create 2018年06月27日22:58
 */
@Service
public class CoreTransactionConvertor {
    @Autowired
    private NodeState nodeState;
    @Autowired
    private RsBlockChainService rsBlockChainService;

    /**
     * build core transaction
     *
     * @param txId
     * @param actionList
     * @return
     */
    public CoreTransaction buildCoreTransaction(String txId, JSONObject bizModel, List<Action> actionList, String policyId) {
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
    public TxOut buildTxOut(String identity,Integer actionIndex, Integer index, JSONObject state) {
        TxOut txOut = new TxOut();
        txOut.setIdentity(identity);
        txOut.setActionIndex(actionIndex);
        txOut.setIndex(index);
        txOut.setState(state);
        return txOut;
    }

    /**
     *build dataIdentityAction
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
     * @param utxoActionTypeEnum
     * @param contractAddress
     * @param stateClass
     * @param index
     * @param inputList
     * @param txOutList
     * @return
     */
    public UTXOAction  buildUTXOAction(UTXOActionTypeEnum utxoActionTypeEnum, String contractAddress, String stateClass, int index, List<TxIn> inputList, List<TxOut> txOutList){
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

}
