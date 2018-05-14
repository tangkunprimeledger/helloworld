package com.higgs.trust.slave.core.api.impl;

import com.higgs.trust.slave.api.AccountInfoService;
import com.higgs.trust.slave.api.vo.AccountInfoVO;
import com.higgs.trust.slave.api.vo.PageVO;
import com.higgs.trust.slave.api.vo.QueryAccountVO;
import com.higgs.trust.slave.core.repository.account.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-05-09
 */
@Service
@Slf4j
public class AccountInfoServiceImpl implements AccountInfoService{
    @Autowired AccountRepository accountRepository;

    @Override public List<AccountInfoVO> queryByAccountNos(List<String> accountNos) {
        return accountRepository.queryByAccountNos(accountNos);
    }

    @Override public PageVO<AccountInfoVO> queryAccountInfo(QueryAccountVO req) {
        if (null == req) {
            return null;
        }

        if (null == req.getPageNo()) {
            req.setPageNo(1);
        }
        if (null == req.getPageSize()) {
            req.setPageSize(20);
        }

        PageVO<AccountInfoVO> pageVO = new PageVO<>();
        pageVO.setPageNo(req.getPageNo());
        pageVO.setPageSize(req.getPageSize());

        long count = accountRepository.countAccountInfoWithOwner(req.getAccountNo(), req.getDataOwner());
        pageVO.setTotal(count);
        if (0 == count) {
            pageVO.setData(null);
            return pageVO;
        } else {
            List<AccountInfoVO> list = accountRepository.queryAccountInfoWithOwner(req.getAccountNo(), req.getDataOwner(),
                req.getPageNo(), req.getPageSize());
            pageVO.setData(list);
        }
        log.info("[AccountInfoServiceImpl.queryAccountInfo] query result: {}", pageVO);
        return pageVO;
    }
}
