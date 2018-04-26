package com.higgs.trust.slave.core.repository.contract;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.slave.dao.contract.AccountContractBindingDao;
import com.higgs.trust.slave.dao.po.contract.AccountContractBindingPO;
import com.higgs.trust.slave.model.bo.contract.AccountContractBinding;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * the repository of AccountContractBinding
 * @author duhongming
 * @date 2018-04-19
 */
@Repository @Slf4j public class AccountContractBindingRepository {

    @Autowired private AccountContractBindingDao dao;

    public void add(AccountContractBinding accountContractBinding) {
        AccountContractBindingPO po = BeanConvertor.convertBean(accountContractBinding, AccountContractBindingPO.class);
        dao.add(po);
    }

    public List<AccountContractBinding> queryListByAccountNo(String accountNo) {
        List<AccountContractBindingPO> list = dao.queryListByAccountNo(accountNo);
        List<AccountContractBinding> bindings = BeanConvertor.convertList(list, AccountContractBinding.class);
        return bindings;
    }

    public AccountContractBinding queryByHash(String hash) {
        AccountContractBindingPO po = dao.queryByHash(hash);
        return BeanConvertor.convertBean(po, AccountContractBinding.class);
    }
}
