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
public class AccountInfoServiceImpl implements AccountInfoService {
    @Autowired
    AccountRepository accountRepository;

    @Override
    public List<AccountInfoVO> queryByAccountNos(List<String> accountNos) {
        return accountRepository.queryByAccountNos(accountNos);
    }

    @Override
    public PageVO<AccountInfoVO> queryAccountInfo(QueryAccountVO req) {
        if (!dealBefore(req)) {
            return null;
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
            List<AccountInfoVO> list = accountRepository.queryAccountInfoWithOwner(req.getAccountNo(), req.getDataOwner(), req.getPageNo(), req.getPageSize());
            pageVO.setData(list);
        }
        log.info("[AccountInfoServiceImpl.queryAccountInfo] query result: {}", pageVO);
        return pageVO;
    }


    @Override
    public List<AccountInfoVO> queryAccountsByPage(QueryAccountVO req) {
        if (!dealBefore(req)) {
            return null;
        }
        List<AccountInfoVO> list = accountRepository.queryAccountInfoWithOwner(req.getAccountNo(), req.getDataOwner(), req.getPageNo(), req.getPageSize());
        log.info("[AccountInfoServiceImpl.queryAccountsByPage] query result: {}", list);
        return list;
    }

    /**
     * pre deal
     * @param req
     * @return
     */
    private boolean dealBefore(QueryAccountVO req) {
        if (null == req) {
            return false;
        }
        //less than minimum，use default value
        Integer minNo = 0;
        if (null == req.getPageNo() || req.getPageNo().compareTo(minNo) <= 0) {
            req.setPageNo(1);
        }
        //over the maximum，use default value
        Integer maxSize = 100;
        if (null == req.getPageSize() || req.getPageSize().compareTo(maxSize) == 1) {
            req.setPageSize(20);
        }
        return true;
    }
}
