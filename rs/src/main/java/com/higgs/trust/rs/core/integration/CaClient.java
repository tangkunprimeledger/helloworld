package com.higgs.trust.rs.core.integration;

import com.higgs.trust.common.feign.FeignRibbonConstants;
import com.higgs.trust.slave.api.vo.CaVO;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.config.Config;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @desc TODO  
 * @author WangQuanzhou
 * @date 2018/6/5 16:59
 */
@FeignClient("${higgs.trust.prefix}") public interface CaClient {

    /**
     * send ca auth request
     *
     * @return
     * @param nodeNameReg
     * @param caVO
     */
    @RequestMapping(value = "/ca/auth", method = RequestMethod.POST) RespData<String> caAuth(
        @RequestHeader(FeignRibbonConstants.NODE_NAME_REG) String nodeNameReg, @RequestBody CaVO caVO);

    /** 
     * @desc send ca update request
     * @param   caVO
     * @return   
     */  
    @RequestMapping(value = "/ca/update", method = RequestMethod.POST) RespData<String> caUpdate(
        @RequestHeader(FeignRibbonConstants.NODE_NAME_REG) String nodeNameReg, @RequestBody CaVO caVO);


    /**
     * @desc send ca cancel request
     * @param   caVO
     * @return
     */
    @RequestMapping(value = "/ca/cancel", method = RequestMethod.POST) RespData<String> caCancel(
        @RequestHeader(FeignRibbonConstants.NODE_NAME_REG) String nodeNameReg, @RequestBody CaVO caVO);
}
