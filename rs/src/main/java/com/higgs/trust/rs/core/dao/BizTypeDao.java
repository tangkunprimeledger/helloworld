package com.higgs.trust.rs.core.dao;

import com.higgs.trust.common.mybatis.BaseDao;
import com.higgs.trust.rs.core.dao.po.BizTypePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper public interface BizTypeDao extends BaseDao<BizTypePO> {
    /**
     * query by policy id
     *
     * @param policyId
     * @return
     */
    BizTypePO queryByPolicyId(@Param("policyId") String policyId);

    /**
     * query all bizType
     *
     * @return
     */
    List<BizTypePO> queryAll();

    /**
     * update bizType
     * @param policyId
     * @param bizType
     */
    void update(String policyId, String bizType);
}
