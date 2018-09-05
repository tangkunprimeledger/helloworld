package com.higgs.trust.slave.api.controller;

import com.higgs.trust.slave.api.RocksDbService;
import com.higgs.trust.slave.api.enums.RespCodeEnum;
import com.higgs.trust.slave.api.vo.RespData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author tangfashuang
 */
@RestController
@Slf4j
@RequestMapping("/rocks")
public class RocksDbController {

    @Autowired private RocksDbService rocksDbService;
    @RequestMapping(value = "/data/get", method = RequestMethod.GET)
    public RespData getData(@RequestParam("columnFamily") String columnFamily, @RequestParam("key") String key) {
        if (StringUtils.isEmpty(columnFamily)) {
            RespData respData = new RespData();
            respData.setCode(RespCodeEnum.PARAM_NOT_VALID.getRespCode());
            respData.setMsg("column family is empty");
            return respData;
        }
        return rocksDbService.getData(columnFamily, key);
    }

}
