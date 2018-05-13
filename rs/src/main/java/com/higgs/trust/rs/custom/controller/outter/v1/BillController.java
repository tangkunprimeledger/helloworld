/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.rs.custom.controller.outter.v1;

import com.higgs.trust.rs.custom.vo.BillCreateVO;
import com.higgs.trust.rs.custom.vo.BillTransferVO;
import com.higgs.trust.slave.api.enums.RespCodeEnum;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.asynctosync.BlockingMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liuyu
 * @date 2018/5/12
 */

@RestController @Slf4j public class BillController {

    @Autowired BlockingMap rsResultMap;

    /**
     * create bill
     *
     * @param billCreateVO
     * @return
     */
    @RequestMapping(value = "/create") RespData<String> create(@RequestBody BillCreateVO billCreateVO) {

        RespData respData = null;

        try {
            respData = (RespData)rsResultMap.poll(billCreateVO.getRequestId(), 1000);
        } catch (InterruptedException e) {
            log.error("tx handle exception. ", e);
            respData = new RespData();
            respData.setCode(RespCodeEnum.SYS_FAIL.getRespCode());
            respData.setMsg("handle transaction exception.");
        }

        if (null == respData) {
            respData = new RespData();
            respData.setCode(RespCodeEnum.SYS_HANDLE_TIMEOUT.getRespCode());
            respData.setMsg("tx handle timeout");
        }
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
