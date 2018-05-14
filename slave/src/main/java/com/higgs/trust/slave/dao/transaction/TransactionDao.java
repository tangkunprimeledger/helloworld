package com.higgs.trust.slave.dao.transaction;

import com.higgs.trust.slave.api.vo.TransactionVO;
import com.higgs.trust.slave.dao.BaseDao;
import com.higgs.trust.slave.dao.po.transaction.TransactionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * TransactionPO data deal dao
 *
 * @author lingchao
 * @create 2018年03月27日20:10
 */
@Mapper public interface TransactionDao extends BaseDao<TransactionPO> {

    /**
     * query transaction by transaction id
     *
     * @param txId
     * @return
     */
    TransactionPO queryByTxId(String txId);

    /**
     * batch insert
     *
     * @param list
     * @return
     */
    int batchInsert(List<TransactionPO> list);

    /**
     * query by block height
     * @param blockHeight
     * @return
     */
    List<TransactionPO> queryByBlockHeight(@Param("blockHeight") Long blockHeight);

    /**
     * query by tx ids
     *
     * @param txIds
     * @return
     */
    List<TransactionPO> queryByTxIds(@Param("txIds") List<String> txIds);

    /**
     * query transactions with condition
     * @param blockHeight
     * @param txId
     * @param sender
     * @return
     */
    List<TransactionPO> queryTxWithCondition(@Param("height") Long blockHeight,
        @Param("txId") String txId, @Param("sender") String sender,
        @Param("start") Integer start, @Param("end") Integer end);

    /**
     * count transaction with condition
     * @param blockHeight
     * @param txId
     * @param sender
     * @return
     */
    long countTxWithCondition(@Param("height") Long blockHeight,
        @Param("txId") String txId, @Param("sender") String sender);
}
