package com.higgs.trust.slave.core.service.datahandler.utxo;

import com.higgs.trust.slave.dao.po.utxo.TxOutPO;
import com.higgs.trust.slave.model.bo.utxo.TxIn;
import com.higgs.trust.slave.model.bo.utxo.UTXO;

import java.util.List;

/**
 * UTXO data Handler
 *
 * @author lingchao
 * @create 2018年03月27日16:53
 */
public interface UTXOHandler {

    /**
     * query UTXO by txId, index and actionIndex
     *
     * @param txId
     * @param index
     * @param actionIndex
     * @return
     */
    UTXO queryUTXO(String txId, Integer index, Integer actionIndex);


    /**
     * query UTXO list
     *
     * @param inputList
     * @return
     */
    List<UTXO> queryUTXOList(List<TxIn> inputList);

    /**
     * batch insert
     *
     * @param txOutPOList
     * @return
     */
    boolean batchInsert(List<TxOutPO> txOutPOList);

    /**
     * batch update
     *
     * @param txOutPOList
     * @return
     */
    boolean batchUpdate(List<TxOutPO> txOutPOList);

}
