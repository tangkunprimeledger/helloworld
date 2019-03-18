package com.higgs.trust.rs.core.integration;

import com.higgs.trust.slave.api.vo.CaVO;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.ca.Ca;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @author WangQuanzhou
 * @date 2018/6/5 16:59
 */
//@FeignClient("${higgs.trust.prefix}")
public interface CaClient {

    /**
     * send ca auth request
     *
     * @param nodeNameReg
     * @param list
     * @return
     */
    @RequestMapping(value = "/ca/auth", method = RequestMethod.POST) RespData<String> caAuth(String nodeNameReg, @RequestBody List<CaVO> list);

    /**
     * @param
     * @return
     * @desc send acqurie ca  request
     */
    @RequestMapping(value = "/ca/get", method = RequestMethod.POST)
    RespData<Ca> acquireCA(String nodeNameReg, @RequestParam("user") String user);


    /**
     * @param
     * @return
     * @desc send acqurie ca  request
     */
    @RequestMapping(value = "/ca/sync", method = RequestMethod.POST)
    RespData<Map> syncCluster(String nodeNameReg);
}
