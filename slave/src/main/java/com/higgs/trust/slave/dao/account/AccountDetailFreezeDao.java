package com.higgs.trust.slave.dao.account;

import com.higgs.trust.slave.dao.BaseDao;
import com.higgs.trust.slave.dao.po.account.AccountDetailFreezePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author liuyu
 * @description freeze detail DAO
 * @date 2018-03-27
 */
@Mapper public interface AccountDetailFreezeDao extends BaseDao<AccountDetailFreezePO> {
}
