package com.higgs.trust.slave.api.controller;

import com.higgs.trust.slave.api.ContractQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Chen Jiawei
 * @date 2018-11-30
 */
@RestController
@RequestMapping("/contract")
public class ContractController {
    @Autowired
    private ContractQueryService contractQueryService;

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    List<?> query(@RequestParam(value = "contractAddress") byte[] contractAddress,
                  @RequestParam(value = "methodSignature") String methodSignature,
                  @RequestParam(value = "args") Object... args) {
        return contractQueryService.query(contractAddress, methodSignature, args);
    }
}