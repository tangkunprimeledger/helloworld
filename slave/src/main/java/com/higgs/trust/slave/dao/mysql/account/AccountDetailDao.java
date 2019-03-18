package com.higgs.trust.slave.dao.mysql.account;

import com.higgs.trust.common.mybatis.BaseDao;
import com.higgs.trust.slave.dao.po.account.AccountDetailPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author liuyu
 * @description detail of account DAO
 * @date 2018-03-27
 */
@Mapper public interface AccountDetailDao extends BaseDao<AccountDetailPO> {
    /**
     * batch insert
     *
     * @param list
     * @return
     */
    int batchInsert(@Param("list") List<AccountDetailPO> list);
}
