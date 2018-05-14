/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.rs.core.controller;

import com.higgs.trust.rs.core.api.SignService;
import com.higgs.trust.slave.api.AccountInfoService;
import com.higgs.trust.slave.api.vo.AccountInfoVO;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author liuyu
 * @date 2018/5/12
 */

@RestController @Slf4j public class SignController {

    @Autowired private SignService signService;

    /**
     * sign transaction
     *
     * @param coreTransaction
     * @return
     */
    @RequestMapping(value = "/signTx") RespData<String> signTx(@RequestBody CoreTransaction coreTransaction) {
        //TODO:call custom business
        return signService.signTx(coreTransaction);
    }
}
