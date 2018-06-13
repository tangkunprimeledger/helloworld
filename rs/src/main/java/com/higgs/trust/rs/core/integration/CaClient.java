package com.higgs.trust.rs.core.integration;

import com.higgs.trust.common.feign.FeignRibbonConstants;
import com.higgs.trust.slave.api.vo.CaVO;
import com.higgs.trust.slave.api.vo.RespData;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/5 16:59
 */
@FeignClient("${higgs.trust.prefix}") public interface CaClient {

    /**
     * send ca auth request
     *
     * @param nodeNameReg
     * @param caVO
     * @return
     */
    @RequestMapping(value = "/ca/auth", method = RequestMethod.POST) RespData<String> caAuth(
        @RequestHeader(FeignRibbonConstants.NODE_NAME_REG) String nodeNameReg, @RequestBody CaVO caVO);

    /**
     * @param
     * @return
     * @desc send acqurie ca  request
     */
    @RequestMapping(value = "/ca/get", method = RequestMethod.POST) RespData<String> acquireCA(
        @RequestHeader(FeignRibbonConstants.NODE_NAME_REG) String nodeNameReg, @RequestBody CaVO caVO);
}
