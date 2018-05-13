package com.higgs.trust.rs.custom.dao;

import com.higgs.trust.common.mybatis.BaseDao;
import com.higgs.trust.rs.custom.dao.po.ReceivableBillPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReceivableBillDao extends BaseDao<ReceivableBillPO> {
}
