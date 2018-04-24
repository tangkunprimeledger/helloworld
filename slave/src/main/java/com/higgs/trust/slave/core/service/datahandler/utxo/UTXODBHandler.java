package com.higgs.trust.slave.core.service.datahandler.utxo;

import com.higgs.trust.slave.core.repository.TxOutRepository;
import com.higgs.trust.slave.dao.po.utxo.TxOutPO;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 * UTXO DB handler
 *
 * @author lingchao
 * @create 2018年04月17日15:18
 */
@Slf4j
@Service
public class UTXODBHandler implements UTXOHandler{

    @Autowired
    private TxOutRepository txOutRepository;


    /**
     * query txOut by txId, index and actionIndex
     *
     * @param txId
     * @param index
     * @param actionIndex
     * @return
     */
    @Override
    public TxOutPO queryTxOut(String txId, Integer index, Integer actionIndex) {
        return txOutRepository.queryTxOut(txId, index, actionIndex);
    }

    /**
     * query UTXO list
     *
     * @param inputList
     * @return
     */
    @Override
    public List<TxOutPO> queryTxOutList(List<TxIn> inputList){
        List<TxOutPO> utxoList = new ArrayList<>();
        for (TxIn txIn :inputList){
            TxOutPO txOut = queryTxOut(txIn.getTxId(), txIn.getIndex(), txIn.getActionIndex());
            //TODO lingchao 查看 txOUt 为 null 怎么处理好，这样处理感觉不对
            if (null != txOut){
                utxoList.add(txOut);
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
        return txOutRepository.batchInsert(txOutPOList);
    }

    /**
     * batch update
     *
     * @param txOutPOList
     * @return
     */
    @Override
    public boolean batchUpdate(List<TxOutPO> txOutPOList) {
        return txOutRepository.batchUpdate(txOutPOList);
    }



}
