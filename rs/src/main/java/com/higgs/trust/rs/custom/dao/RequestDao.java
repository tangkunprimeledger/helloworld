package com.higgs.trust.rs.custom.dao;

import com.higgs.trust.common.mybatis.BaseDao;
import com.higgs.trust.rs.custom.dao.po.RequestPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

@Mapper
public interface RequestDao extends BaseDao<RequestPO> {
    /**
     * 根据requestId 查询请求数据
     *
     * @param requestId
     * @return
     */
    RequestPO queryByRequestId(@Param("requestId") String requestId);


    /**
     *
     * 更改请求状态
     * @param requestId
     * @param fromStatus
     * @param toStatus
     * @return
     */
    int updateStatusByRequestId(@Param("requestId") String requestId, @Param("fromStatus")String fromStatus, @Param("toStatus")String toStatus, @Param("respCode") String respCode, @Param("respMsg") String respMsg);
}
