package com.higgs.trust.slave.model.convert;

import com.higgs.trust.slave.api.enums.utxo.UTXOStatusEnum;
import com.higgs.trust.slave.dao.po.utxo.TxOutPO;
import com.higgs.trust.slave.model.bo.action.UTXOAction;
import com.higgs.trust.slave.model.bo.context.ActionData;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.TxOut;
import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.List;

/**
 * Convetor
 *
 * @author lingchao
 * @create 2018年04月03日15:01
 */
public class UTXOConvert {

    /**
     * UTXO builder
     *
     * @param txOut
     * @param actionData
     * @return
     */
    public static TxOutPO UTXOBuilder(TxOut txOut, ActionData actionData) {
        UTXOAction utxoAction = (UTXOAction)actionData.getCurrentAction();
        TxOutPO txOutPO = new TxOutPO();
        BeanUtils.copyProperties(txOut, txOutPO);
        txOutPO.setState(txOut.getState().toJSONString());
        txOutPO.setStateClass(utxoAction.getStateClass());
        txOutPO.setContractAddress(utxoAction.getContractAddress());
        txOutPO.setTxId(actionData.getCurrentTransaction().getCoreTx().getTxId());
        txOutPO.setStatus(UTXOStatusEnum.UNSPENT.getCode());
        return txOutPO;
    }

    /**
     * STXO
     *
     * @param txIn
     * @param actionData
     * @return
     */
    public static TxOutPO STXOBuilder(TxIn txIn, ActionData actionData) {
        TxOutPO txOutPO = new TxOutPO();
        BeanUtils.copyProperties(txIn, txOutPO);
        txOutPO.setSTxId(actionData.getCurrentTransaction().getCoreTx().getTxId());
        txOutPO.setStatus(UTXOStatusEnum.SPENT.getCode());
        return txOutPO;
    }

    public static String toTxInString(List<TxIn> listTxIn){
        Collections.sort(listTxIn,(a,b)->{
            if(!a.getActionIndex().equals(b.getActionIndex())){
                return a.getActionIndex() - b.getActionIndex();
            }
            return a.getIndex() - b.getIndex();
        });
        StringBuffer sb = new StringBuffer();
        listTxIn.forEach(a->sb.append(a.toString()));
        return sb.toString();
    }

}
