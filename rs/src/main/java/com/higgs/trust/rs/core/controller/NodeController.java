package com.higgs.trust.rs.core.controller;

import com.higgs.trust.rs.core.service.NodeConsensusService;
import com.higgs.trust.rs.core.vo.NodeOptVO;
import com.higgs.trust.slave.api.vo.RespData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/5 17:37
 */
@RestController
@Slf4j
public class NodeController {

    @Autowired
    private NodeConsensusService nodeConsensusService;

    /**
     * auth ca transaction
     *
     * @param vo
     * @return
     */
    @RequestMapping(value = "/node/join", method = RequestMethod.POST)
    RespData<String> nodeJoin(
            @RequestBody NodeOptVO vo) {
        return nodeConsensusService.joinConsensusTx(vo);
    }

}
