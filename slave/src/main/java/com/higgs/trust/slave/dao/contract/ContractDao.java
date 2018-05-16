package com.higgs.trust.slave.dao.contract;

import com.higgs.trust.slave.dao.BaseDao;
import com.higgs.trust.slave.dao.po.contract.ContractPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper public interface ContractDao extends BaseDao<ContractPO> {

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
}
