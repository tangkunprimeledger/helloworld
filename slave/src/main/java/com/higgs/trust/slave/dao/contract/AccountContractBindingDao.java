package com.higgs.trust.slave.dao.contract;

import com.higgs.trust.slave.dao.BaseDao;
import com.higgs.trust.slave.dao.po.contract.AccountContractBindingPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper public interface AccountContractBindingDao extends BaseDao<AccountContractBindingPO> {
    List<AccountContractBindingPO> queryListByAccountNo(String accountNo);
}
