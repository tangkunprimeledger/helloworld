package com.higgs.trust.rs.core.controller;

import com.higgs.trust.rs.core.api.CaService;
import com.higgs.trust.rs.core.api.SignService;
import com.higgs.trust.slave.api.vo.CaVO;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**  
 * @desc TODO  
 * @author WangQuanzhou
 * @date 2018/6/5 17:37    
 */
@RestController @Slf4j public class CaController {

    @Autowired private CaService caService;

    /**
     * auth ca transaction
     *
     * @param caVO
     * @return
     */
    @RequestMapping(value = "/ca/auth") RespData<String> caAuth(@RequestBody CaVO caVO) {
        return caService.authCaTx(caVO);
    }

    /**
     * update ca transaction
     *
     * @param caVO
     * @return
     */
    @RequestMapping(value = "/ca/update") RespData<String> caUpdate(@RequestBody CaVO caVO) {
        return caService.updateCaTx(caVO);
    }

    /**
     * cancel ca transaction
     *
     * @param caVO
     * @return
     */
    @RequestMapping(value = "/ca/cancel") RespData<String> caCancel(@RequestBody CaVO caVO) {
        return caService.cancelCaTx(caVO);
    }

    /**
     * init ca transaction
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/ca/init") RespData<String> caInit() {
        return caService.initCaTx();
    }
}
