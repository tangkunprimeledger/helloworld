package com.higgs.trust.slave.integration.ca;

import com.higgs.trust.common.feign.FeignRibbonConstants;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.config.Config;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/5 16:59
 */
@FeignClient("${higgs.trust.prefix}") public interface CaInitClient {

    /**
     * @param
     * @return
     * @desc send ca init request
     */
    @RequestMapping(value = "/ca/init", method = RequestMethod.GET) RespData<List<Config>> caInit(
        @RequestHeader(FeignRibbonConstants.NODE_NAME) String nodeName);

}
