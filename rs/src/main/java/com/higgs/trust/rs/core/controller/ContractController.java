package com.higgs.trust.rs.core.controller;

import com.higgs.trust.rs.core.api.ContractService;
import com.higgs.trust.rs.core.bo.ContractCreateRequest;
import com.higgs.trust.rs.core.bo.ContractInvokeRequest;
import com.higgs.trust.rs.core.bo.ContractMigrationRequest;
import com.higgs.trust.rs.core.bo.ContractQueryRequest;
import com.higgs.trust.slave.api.vo.ContractVO;
import com.higgs.trust.slave.api.vo.PageVO;
import com.higgs.trust.slave.api.vo.RespData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author duhongming
 * @date 2018/5/14
 */
@RestController @Slf4j @CrossOrigin @RequestMapping(path = "/contract") public class ContractController {

    @Autowired
    private ContractService contractService;

    private <T> RespData<T> ok(T data) {
        RespData<T> respData = new RespData<T>();
        respData.setCode("000000");
        respData.setData(data);
        return respData;
    }

    private <T> RespData<T> fail(T data, String code, String message) {
        RespData<T> respData = new RespData<T>();
        respData.setCode(code);
        respData.setData(data);
        respData.setMsg(message);
        return respData;
    }

    @PutMapping
    public RespData<String> deploy(@RequestBody String code) {
        if (StringUtils.isEmpty(code)) {
            return fail(null, "", "code is empty");
        }
        String txId = "0x00000000" + code.hashCode() + System.currentTimeMillis();
        RespData result = contractService.deploy(txId, code);
        return result.isSuccess() ? ok(txId) : fail(txId, result.getRespCode(), result.getMsg());
    }

    @PutMapping(path = "deploy2")
    public RespData<String> deploy2(@RequestBody ContractCreateRequest request) {
        String code = request.getCode();
        if (StringUtils.isEmpty(code)) {
            return fail(null, "", "code is empty");
        }
        String txId = "0x00000000" + code.hashCode() + System.currentTimeMillis();
        RespData result = contractService.deploy(txId, code, request.getInitArgs());
        return result.isSuccess() ? ok(txId) : fail(txId, result.getRespCode(), result.getMsg());
    }

    @PostMapping(path = "/invoke")
    public RespData<String> invoke(@RequestBody ContractInvokeRequest invokeRequest) {
        if (invokeRequest == null) {
            return fail(null, "", "invalid invokeRequest");
        }
        if (StringUtils.isEmpty(invokeRequest.getAddress())) {
            return fail(null, "", "address is empty");
        }
        String txId = "0x10000000" + invokeRequest.getAddress().hashCode() + System.currentTimeMillis();
        RespData result = contractService.invoke(txId, invokeRequest.getAddress(), invokeRequest.getBizArgs());
        return result.isSuccess() ? ok(txId) : fail(txId, result.getRespCode(), result.getMsg());
    }

    @PostMapping(path = "/migration")
    public RespData<String> migration(@RequestBody ContractMigrationRequest migrationRequest) {
        if (migrationRequest == null) {
            return fail(null, "", "invalid migrationRequest");
        }
        if (StringUtils.isEmpty(migrationRequest.getFromAddress())) {
            return fail(null, "", "fromAddress is empty");
        }

        if (StringUtils.isEmpty(migrationRequest.getToAddress())) {
            return fail(null, "", "toAddress is empty");
        }

        RespData result = contractService.migration(migrationRequest);
        return result.isSuccess() ? ok(migrationRequest.getTxId()) : fail(migrationRequest.getTxId(), result.getRespCode(), result.getMsg());
    }

    @PostMapping(path = "/query")
    public RespData<Object> query(@RequestBody ContractQueryRequest request) {
        Object result = contractService.query(request);
        return ok(result);
    }

    @GetMapping(path = "/list")
    public RespData<PageVO<ContractVO>> queryList(@RequestParam Long height, @RequestParam(required = false) String txId,
                                                  @RequestParam Integer pageIndex, @RequestParam Integer pageSize) {
        try {
            PageVO<ContractVO> result = contractService.queryList(height, txId, pageIndex, pageSize);
            return RespData.success(result);
        } catch (Exception ex) {
            return RespData.error("", ex.getMessage(), null);
        }
    }
}
