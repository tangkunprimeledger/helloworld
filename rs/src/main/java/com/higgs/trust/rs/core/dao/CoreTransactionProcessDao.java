package com.higgs.trust.rs.core.dao;

import com.higgs.trust.common.mybatis.BaseDao;
import com.higgs.trust.rs.core.dao.po.CoreTransactionProcessPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CoreTransactionProcessDao extends BaseDao<CoreTransactionProcessPO> {

    /**
     * query by tx id
     *
     * @param txId
     * @return
     */
    CoreTransactionProcessPO queryByTxId(@Param("txId") String txId, @Param("status") String status);

    /**
     * query by status rowNum and count
     *
     * @param status
     * @param rowNum
     * @param count
     * @return
     */
    List<CoreTransactionProcessPO> queryByStatus(@Param("status") String status, @Param("rowNum") int rowNum, @Param("count") int count);

    /**
     * update status by form->to
     *
     * @param txId
     * @param from
     * @param to
     * @return
     */
    int updateStatus(@Param("txId") String txId, @Param("from") String from, @Param("to") String to);


    /**
     * delete coreTxProcess for END status
     *
     * @return
     */
    // TODO int not means the recordes be delete
    int deleteEnd();
}
