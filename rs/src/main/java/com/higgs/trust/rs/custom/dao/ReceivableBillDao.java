package com.higgs.trust.rs.custom.dao;

import com.higgs.trust.common.mybatis.BaseDao;
import com.higgs.trust.rs.custom.dao.po.ReceivableBillPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReceivableBillDao extends BaseDao<ReceivableBillPO> {

    /**
     *
     * @param txId
     * @param actionIndex
     * @param index
     * @param fromStatus
     * @param toStatus
     * @return
     */
    int updateStatus(@Param("txId") String txId, @Param("actionIndex")Long actionIndex,@Param("index") Long index,  @Param("fromStatus")String fromStatus, @Param("toStatus")String toStatus);
}
