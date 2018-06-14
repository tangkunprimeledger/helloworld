package com.higgs.trust.slave.dao.contract;

import com.higgs.trust.slave.dao.BaseDao;
import com.higgs.trust.slave.dao.po.contract.AccountContractBindingPO;
import com.higgs.trust.slave.dao.po.contract.ContractStatePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;

@Mapper
public interface ContractStateDao extends BaseDao<ContractStatePO> {

    int batchInsert(Collection<ContractStatePO> list);

    int batchUpdate(Collection<ContractStatePO> list);

    int save(ContractStatePO state);

    int deleteByAddress(@Param("address") String address);

    ContractStatePO queryByAddress(@Param("address") String address);
}
