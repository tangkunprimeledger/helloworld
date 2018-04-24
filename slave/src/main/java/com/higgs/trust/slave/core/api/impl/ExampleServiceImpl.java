package com.higgs.trust.slave.core.api.impl;

import com.higgs.trust.slave.api.ExampleService;
import com.higgs.trust.slave.dao.account.AccountInfoDao;
import com.higgs.trust.slave.dao.po.account.AccountInfoPO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Description:
 * @author: pengdi
 **/
public class ExampleServiceImpl implements ExampleService {

    @Autowired private AccountInfoDao accountInfoDao;

    @Override public boolean save(AccountInfoPO accountInfo) {
        return accountInfoDao.add(accountInfo) == 1;
    }

    @Override public boolean remove(Long userId) {
        return accountInfoDao.delete(userId) == 1;
    }

}
