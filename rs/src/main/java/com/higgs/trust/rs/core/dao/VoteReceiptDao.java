package com.higgs.trust.rs.core.dao;

import com.higgs.trust.common.mybatis.BaseDao;
import com.higgs.trust.rs.core.dao.po.VoteReceiptPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper public interface VoteReceiptDao extends BaseDao<VoteReceiptPO> {

    /**
     * query by transaction id,return list
     *
     * @param txId
     * @return
     */
    List<VoteReceiptPO> queryByTxId(@Param("txId") String txId);

    /**
     * query vote-receipt by transaction-id and voter rs-name
     *
     * @param txId
     * @param voter
     * @return
     */
    VoteReceiptPO queryForVoter(@Param("txId") String txId, @Param("voter") String voter);

    /**
     * batch insert datas
     *
     * @param list
     * @return
     */
    int batchAdd(List<VoteReceiptPO> list);
}
