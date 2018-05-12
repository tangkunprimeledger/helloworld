package com.higgs.trust.rs.core.dao;

import com.higgs.trust.common.mybatis.BaseDao;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CoreTransactionDao extends BaseDao<CoreTransactionPO> {
}
