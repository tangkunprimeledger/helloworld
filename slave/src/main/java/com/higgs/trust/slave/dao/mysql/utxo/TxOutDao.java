package com.higgs.trust.slave.dao.mysql.utxo;

import com.higgs.trust.common.mybatis.BaseDao;
import com.higgs.trust.slave.dao.po.utxo.TxOutPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * TxOutBO data deal dao
 *
 * @author lingchao
 * @create 2018年03月27日20:10
 */
@Mapper public interface TxOutDao extends BaseDao<TxOutPO> {
    /**
     * query txOut by txId, index and actionIndex
     *
     * @param txId
     * @param index
     * @param actionIndex
     * @return
     */
    TxOutPO queryTxOut(@Param("txId") String txId, @Param("index") Integer index, @Param("actionIndex") Integer actionIndex);

    /**
     * batch insert
     *
     * @param txOutPOList
     * @return
     */
    int batchInsert(List<TxOutPO> txOutPOList);

    /**
     * batch update
     *
     * @param txOutPOList
     * @return
     */
    int batchUpdate(List<TxOutPO> txOutPOList);

    /**
     * query tx_out by tx_id
     * @param txId
     * @return
     */
    List<TxOutPO> queryByTxId(@Param("txId") String txId);

    /**
     * query tx_out by s_tx_id
     * @param sTxId
     * @return
     */
    List<TxOutPO> queryBySTxId(@Param("sTxId") String sTxId);
}
