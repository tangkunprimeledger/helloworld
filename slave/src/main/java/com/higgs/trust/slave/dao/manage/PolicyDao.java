package com.higgs.trust.slave.dao.manage;

import com.higgs.trust.slave.dao.BaseDao;
import com.higgs.trust.slave.dao.po.manage.PolicyPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author tangfashuang
 * @date 2018/03/28
 * @desc policy dao
 */
@Mapper public interface PolicyDao extends BaseDao<PolicyPO> {
    /**
     * query policy by policyId
     *
     * @param policyId
     * @return
     */
    PolicyPO queryByPolicyId(String policyId);
}
