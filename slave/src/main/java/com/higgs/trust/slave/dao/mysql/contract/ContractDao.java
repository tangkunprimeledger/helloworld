package com.higgs.trust.slave.dao.mysql.contract;

import com.higgs.trust.common.mybatis.BaseDao;
import com.higgs.trust.slave.dao.po.contract.ContractPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper public interface ContractDao extends BaseDao<ContractPO> {

    /**
     * batch insert
     * @param list
     * @return
     */
    int batchInsert(List<ContractPO> list);

    /**
     * query contract by address
     * @param address
     * @return
     */
    ContractPO queryByAddress(@Param("address") String address);

    /**
     *
     * @param height
     * @param txId
     * @param startIndex
     * @param endIndex
     * @return
     */
    List<ContractPO> query(@Param("height") Long height, @Param("txId") String txId, @Param("startIndex") Integer startIndex, @Param("endIndex") Integer endIndex);

    /**
     * get query count
     * @param height
     * @param txId
     * @return
     */
    Long getQueryCount(@Param("height") Long height, @Param("txId") String txId);

    /**
     * query by txId and action index
     *
     * @param txId
     * @param actionIndex
     * @return
     */
    ContractPO queryByTxId(@Param("txId") String txId,@Param("actionIndex") int actionIndex);
}
