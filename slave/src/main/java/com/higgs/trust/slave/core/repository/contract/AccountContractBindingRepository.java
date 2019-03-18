package com.higgs.trust.slave.core.repository.contract;

import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.slave.common.config.InitConfig;
import com.higgs.trust.slave.dao.mysql.contract.AccountContractBindingDao;
import com.higgs.trust.slave.dao.po.contract.AccountContractBindingPO;
import com.higgs.trust.slave.dao.rocks.contract.AccountContractBindingRocksDao;
import com.higgs.trust.slave.model.bo.contract.AccountContractBinding;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * the repository of AccountContractBinding
 * @author duhongming
 * @date 2018-04-19
 * //TODO 此表可删除，暂时不考虑rocksdb的实现
 */
@Repository @Slf4j public class AccountContractBindingRepository {

    @Autowired private AccountContractBindingDao dao;

    @Autowired private AccountContractBindingRocksDao rocksDao;

    @Autowired private InitConfig initConfig;

    public boolean batchInsert(List<AccountContractBindingPO> list) {
        int result;
        if (initConfig.isUseMySQL()) {
            result = dao.batchInsert(list);
        } else {
            result = rocksDao.batchInsert(list);
            //TODO account_no_bind_hash不再使用
        }
        return result == list.size();
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
