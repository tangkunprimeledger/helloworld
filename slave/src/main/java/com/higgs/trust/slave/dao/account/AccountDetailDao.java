package com.higgs.trust.slave.dao.account;

import com.higgs.trust.slave.dao.BaseDao;
import com.higgs.trust.slave.dao.po.account.AccountDetailPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author liuyu
 * @description detail of account DAO
 * @date 2018-03-27
 */
@Mapper public interface AccountDetailDao extends BaseDao<AccountDetailPO> {
}
