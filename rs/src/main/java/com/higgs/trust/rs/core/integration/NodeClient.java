package com.higgs.trust.rs.core.integration;

import com.higgs.trust.common.feign.FeignRibbonConstants;
import com.higgs.trust.slave.api.vo.RespData;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/5 16:59
 */
@FeignClient("${higgs.trust.prefix}") public interface NodeClient {

    /**
     * @param
     * @return
     * @desc send node join request
     */
    @RequestMapping(value = "/node/join", method = RequestMethod.GET) RespData<String> nodeJoin(
        @RequestHeader(FeignRibbonConstants.NODE_NAME_REG) String nodeName, @RequestParam("user") String user);

    /**
     * @param
     * @return
     * @desc send node leave request
     */
    @RequestMapping(value = "/node/leave", method = RequestMethod.GET) RespData<String> nodeLeave(
        @RequestHeader(FeignRibbonConstants.NODE_NAME) String nodeName, @RequestParam("user") String user);

}
