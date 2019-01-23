package com.higgs.trust.rs.core.controller;

import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.api.vo.diagnosis.ContractCodeVO;
import com.higgs.trust.slave.core.service.diagnosis.DiagnosisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Chen Jiawei
 * @date 2019-01-21
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping(path = "/diagnosis")
public class DiagnosisController {
    @Autowired
    private DiagnosisService diagnosisService;

    /**
     * Query contract code with the specified address and the block height.
     *
     * @param address contract address, a hex string with 40 characters
     * @param height  height of block in which contract exists, if
     *                not given, query is on the latest block
     * @return the contract code
     */
    @GetMapping(path = "/contract/code")
    public RespData<ContractCodeVO> queryContractCode(
            @RequestParam("address") String address, @RequestParam(name = "height", required = false) Long height) {
        try {
            ContractCodeVO contractCode = diagnosisService.queryContractCode(address, height);
            return RespData.success(contractCode);
        } catch (Exception e) {
            return RespData.error("100000", e.getMessage(), null);
        }
    }
}
