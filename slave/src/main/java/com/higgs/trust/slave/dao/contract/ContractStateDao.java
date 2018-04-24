package com.higgs.trust.slave.dao.contract;

import com.higgs.trust.slave.dao.BaseDao;
import com.higgs.trust.slave.dao.po.contract.ContractStatePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ContractStateDao extends BaseDao<ContractStatePO> {
    int save(ContractStatePO state);
    int deleteByAddress(@Param("address") String address);
    ContractStatePO queryByAddress(@Param("address") String address);
}
