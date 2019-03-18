package com.higgs.trust.slave.dao.mysql.pack;

import com.higgs.trust.common.mybatis.BaseDao;
import com.higgs.trust.slave.dao.po.pack.PendingTransactionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper public interface PendingTransactionDao extends BaseDao<PendingTransactionPO> {

    /**
     * query pending transaction by tx id
     *
     * @param txId
     * @return
     */
    PendingTransactionPO queryByTxId(@Param("txId") String txId);

    /**
     * query pending transaction list by height
     * @param height
     * @return
     */
    List<PendingTransactionPO> queryByHeight(@Param("height") Long height);

    /**
     * batch insert
     * @param list
     * @return
     */
    int batchInsert(List<PendingTransactionPO> list);

    /**
     * query pending transaction
     * @param txIds
     * @return
     */
    List<PendingTransactionPO> queryByTxIds(@Param("txIds") List<String> txIds);
    /**
     * delete by less than height
     *
     * @param height
     * @return
     */
    int deleteLessThanHeight(@Param("height")Long height);

    /**
     * delete by height
     * @param height
     * @return
     */
    int deleteByHeight(@Param("height")Long height);
}
