/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.rs.custom.controller.outter.v1;

import com.higgs.trust.rs.custom.vo.BillCreateVO;
import com.higgs.trust.rs.custom.vo.BillTransferVO;
import com.higgs.trust.slave.api.enums.RespCodeEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidateResult;
import com.higgs.trust.slave.common.util.beanvalidator.BeanValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liuyu
 * @date 2018/5/12
 */

@RestController @Slf4j public class BillController {

    /**
     * create bill
     *
     * @param billCreateVO
     * @return
     */
    @RequestMapping(value = "/create") RespData<String> create(@RequestBody BillCreateVO billCreateVO) {

        RespData respData = null;

        BeanValidateResult result = BeanValidator.validate(billCreateVO);

        if (!result.isSuccess()) {
            log.error("[BillController.create]param validate failed, cause: " + result.getFirstMsg());
            respData = new RespData();
            respData.setCode(RespCodeEnum.PARAM_NOT_VALID.getRespCode());
            respData.setMsg(result.getFirstMsg());
        }

        //TODO 调用BillService

        return respData;
    }

    /**
     * transfer bill
     *
     * @param billTransferVO
     * @return
     */
    @RequestMapping(value = "/transfer") RespData<String> create(@RequestBody BillTransferVO billTransferVO) {
        return null;
    }
}
