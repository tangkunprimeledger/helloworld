package com.higgs.trust.slave.dao.contract;

import com.higgs.trust.slave.dao.BaseDao;
import com.higgs.trust.slave.dao.po.contract.AccountContractBindingPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * @author duhongming
 * @date 2018/4/26
 */
@Mapper public interface AccountContractBindingDao extends BaseDao<AccountContractBindingPO> {

    /**
     * batch insert
     * @param list
     * @return
     */
    int batchInsert(Collection<AccountContractBindingPO> list);

    /**
     * query AccountContractBinding list by accountNo
     * @param accountNo
     * @return
     */
    List<AccountContractBindingPO> queryListByAccountNo(@Param("accountNo") String accountNo);

    /**
     * query one AccountContractBinding by hash
     * @param hash
     * @return
     */
    AccountContractBindingPO queryByHash(@Param("hash") String hash);
}
