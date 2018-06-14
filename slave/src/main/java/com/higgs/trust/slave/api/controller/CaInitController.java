package com.higgs.trust.slave.api.controller;

import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.core.service.ca.CaInitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/5 17:37
 */
@RestController @Slf4j public class CaInitController {

    @Autowired private CaInitService caInitService;

    /**
     * init ca transaction
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/ca/init") RespData<String> caInit() {
        return caInitService.initCaTx();
    }

}
