package com.higgs.trust.rs.core.controller;

import com.higgs.trust.rs.core.api.ContractService;
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
