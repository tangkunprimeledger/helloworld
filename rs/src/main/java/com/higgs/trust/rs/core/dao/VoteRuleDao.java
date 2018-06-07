package com.higgs.trust.rs.core.dao;

import com.higgs.trust.common.mybatis.BaseDao;
import com.higgs.trust.rs.core.bo.VoteRule;
import com.higgs.trust.rs.core.dao.po.VoteRulePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper public interface VoteRuleDao extends BaseDao<VoteRulePO> {

    /**
     * query vote rule by policy id
     *
     * @param policyId
     * @return
     */
    VoteRulePO queryByPolicyId(@Param("policyId") String policyId);
}
