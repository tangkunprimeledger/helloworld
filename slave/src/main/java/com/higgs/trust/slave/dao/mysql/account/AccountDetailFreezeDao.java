package com.higgs.trust.slave.dao.mysql.account;

import com.higgs.trust.common.mybatis.BaseDao;
import com.higgs.trust.slave.dao.po.account.AccountDetailFreezePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author liuyu
 * @description freeze detail DAO
 * @date 2018-03-27
 */
@Mapper public interface AccountDetailFreezeDao extends BaseDao<AccountDetailFreezePO> {
    /**
     * batch insert
     *
     * @param list
     * @return
     */
    int batchInsert(@Param("list") List<AccountDetailFreezePO> list);
}
