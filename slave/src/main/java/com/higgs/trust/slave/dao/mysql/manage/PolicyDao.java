package com.higgs.trust.slave.dao.mysql.manage;

import com.higgs.trust.common.mybatis.BaseDao;
import com.higgs.trust.slave.dao.po.manage.PolicyPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

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

    /**
     * batch insert
     *
     * @param policyPOList
     * @return
     */
    int batchInsert(List<PolicyPO> policyPOList);
}
