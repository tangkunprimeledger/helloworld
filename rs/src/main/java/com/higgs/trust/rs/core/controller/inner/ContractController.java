package com.higgs.trust.rs.core.controller.inner;

import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.ContractService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author lingchao
 * @date 2019/1/5
 */
@RestController
@Slf4j
@RequestMapping(path = "/contract")
public class ContractController {
    @Autowired
    private ContractService contractService;

    /**
     * @param blockHeight
     * @param address
     * @param methodSignature
     * @param parameters
     * @return
     */
    @GetMapping(value = "/queryState2")
    List<?> queryState2(@RequestParam(value = "blockHeight") Long blockHeight, @RequestParam(value = "address") String address, @RequestParam(value = "methodSignature") String methodSignature, @RequestParam(value = "parameters") Object... parameters) {
        if (StringUtils.isBlank(address) || StringUtils.isBlank(methodSignature)) {
            log.error("address :{} or methodSignature :{} can not be null!", address, methodSignature);
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_PARAM_ERROR);
        }
        return contractService.query2(blockHeight, address, methodSignature, parameters);
    }
}
