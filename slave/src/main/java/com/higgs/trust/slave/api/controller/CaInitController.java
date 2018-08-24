package com.higgs.trust.slave.api.controller;

import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.core.service.ca.CaInitService;
import com.higgs.trust.slave.model.bo.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
     * @return
     */
    @RequestMapping(value = "/ca/init", method = RequestMethod.GET) RespData<List<Config>> caInit() {
        return caInitService.initCaTx();
    }

}
