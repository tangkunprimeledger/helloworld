package com.higgs.trust.slave.dao.contract;

import com.higgs.trust.slave.dao.BaseDao;
import com.higgs.trust.slave.dao.po.contract.ContractPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper public interface ContractDao extends BaseDao<ContractPO> {

    /**
     * query contract by address
     * @param address
     * @return
     */
    ContractPO queryByAddress(@Param("address") String address);
}
