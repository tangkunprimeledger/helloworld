package com.higgs.trust.slave.core.api.impl;

import com.higgs.trust.slave.api.AccountInfoService;
import com.higgs.trust.slave.api.vo.AccountInfoVO;
import com.higgs.trust.slave.api.vo.QueryAccountVO;
import com.higgs.trust.slave.core.repository.account.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-05-09
 */
@Service
public class AccountInfoServiceImpl implements AccountInfoService{
    @Autowired AccountRepository accountRepository;

    @Override public List<AccountInfoVO> queryByAccountNos(List<String> accountNos) {
        return accountRepository.queryByAccountNos(accountNos);
    }

    @Override public List<AccountInfoVO> queryAccountInfo(QueryAccountVO req) {
        return accountRepository.queryAccountInfoWithOwner(req.getAccountNo(), req.getDataOwner());
    }
}
