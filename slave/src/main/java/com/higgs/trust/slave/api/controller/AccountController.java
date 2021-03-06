/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.api.controller;

import com.higgs.trust.slave.api.AccountInfoService;
import com.higgs.trust.slave.api.vo.AccountInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author suimi
 * @date 2018/4/24
 */

@RestController @Slf4j @RequestMapping("/account") public class AccountController {

    @Autowired private AccountInfoService accountInfoService;

    /**
     * batch query the account info
     *
     * @param accountNos
     * @return
     */
    @RequestMapping(value = "/batchQuery") List<AccountInfoVO> queryByAccountNos(
        @RequestBody List<String> accountNos) {
        return accountInfoService.queryByAccountNos(accountNos);
    }
}
