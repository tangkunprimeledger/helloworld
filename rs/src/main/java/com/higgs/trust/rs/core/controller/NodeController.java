package com.higgs.trust.rs.core.controller;

import com.higgs.trust.rs.core.api.CaService;
import com.higgs.trust.rs.core.service.NodeConsensusService;
import com.higgs.trust.slave.api.vo.CaVO;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.ca.Ca;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/5 17:37
 */
@RestController @Slf4j public class NodeController {

    @Autowired private NodeConsensusService nodeConsensusService;

    /**
     * auth ca transaction
     *
     * @param user
     * @return
     */
    @RequestMapping(value = "/node/join", method = RequestMethod.GET) RespData<String> nodeJoin(@RequestParam("user") String user){
        return nodeConsensusService.joinConsensusTx(user);
    }



}
