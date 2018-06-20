package com.higgs.trust.slave.core.service.datahandler.utxo;


import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.service.snapshot.agent.UTXOSnapshotAgent;
import com.higgs.trust.slave.dao.po.utxo.TxOutPO;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.UTXO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * UTXO snapshot handler
 *
 * @author lingchao
 * @create 2018年04月17日15:19
 */
@Service
@Slf4j
public class UTXOSnapshotHandler implements UTXOHandler{

    @Autowired
    private UTXOSnapshotAgent utxoSnapshotAgent;


    /**
     * query UTXO by txId, index and actionIndex
     *
     * @param txId
     * @param index
     * @param actionIndex
     * @return
     */
    @Override
    public UTXO queryUTXO(String txId, Integer index, Integer actionIndex) {
        return utxoSnapshotAgent.queryUTXO(txId, index, actionIndex);
    }


    /**
     * query UTXO list
     *
     * @param inputList
     * @return
     */
    @Override
    public List<UTXO> queryUTXOList(List<TxIn> inputList){
        List<UTXO> utxoList = new ArrayList<>();
        for (TxIn txIn :inputList){
            UTXO utxo = queryUTXO(txIn.getTxId(), txIn.getIndex(), txIn.getActionIndex());
            if (null == utxo) {
                log.error("The input: {} is not existed in txOut", txIn);
                throw new SlaveException(SlaveErrorEnum.SLAVE_TX_OUT_NOT_EXISTS_ERROR);
            }
            if (null != utxo){
                utxoList.add(utxo);
            }
        }
        return utxoList;
    }

    /**
     * batch insert
     *
     * @param txOutPOList
     * @return
     */
    @Override
    public boolean batchInsert(List<TxOutPO> txOutPOList) {
        return utxoSnapshotAgent.batchInsertTxOut(txOutPOList);
    }

    /**
     * batch update
     *
     * @param txOutPOList
     * @return
     */
    @Override
    public boolean batchUpdate(List<TxOutPO> txOutPOList) {
        return utxoSnapshotAgent.bachUpdateTxOut(txOutPOList);
    }

}
