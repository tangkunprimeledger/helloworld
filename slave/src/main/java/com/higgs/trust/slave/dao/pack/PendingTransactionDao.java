package com.higgs.trust.slave.dao.pack;

import com.higgs.trust.slave.dao.BaseDao;
import com.higgs.trust.slave.dao.po.pack.PendingTransactionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper public interface PendingTransactionDao extends BaseDao<PendingTransactionPO> {

    /**
     * query pending transactions by status
     *
     * @param status
     * @param limit  the num of result
     * @return
     */
    List<PendingTransactionPO> queryByStatus(@Param("status") String status, @Param("limit") int limit);

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
     * update status from fromStatus to toStatus
     * and update height if height column is null
     * @param txId
     * @param fromStatus
     * @param toStatus
     * @param height
     *
     * @return
     */
    int updateStatus(@Param("txId") String txId, @Param("fromStatus") String fromStatus,
        @Param("toStatus") String toStatus, @Param("height") Long height);
}
