package com.higgs.trust.slave.core.service.datahandler.utxo;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.slave.core.repository.TxOutRepository;
import com.higgs.trust.slave.dao.po.utxo.TxOutPO;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.UTXO;
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
     * query UTXO by txId, index and actionIndex
     *
     * @param txId
     * @param index
     * @param actionIndex
     * @return
     */
    @Override
    public UTXO queryUTXO(String txId, Integer index, Integer actionIndex) {
        TxOutPO txOutPO = txOutRepository.queryTxOut(txId, index, actionIndex);
        UTXO utxo = BeanConvertor.convertBean(txOutPO, UTXO.class);
        if (null != txOutPO){
            utxo.setState(JSON.parseObject(txOutPO.getState()));
        }
        return utxo;
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
            //TODO lingchao 查看 txOUt 为 null 怎么处理好，这样处理感觉不对
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
