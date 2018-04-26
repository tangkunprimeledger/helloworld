/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.api.controller;

import com.higgs.trust.slave.api.BlockChainService;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author suimi
 * @date 2018/4/24
 */

@RestController @Slf4j @RequestMapping("/block") public class BlockChainController {

    @Autowired private BlockChainService blockChainService;

    /**
     * get the block headers
     *
     * @param startHeight
     * @param size
     * @return
     */
    @RequestMapping(value = "/header/get", method = RequestMethod.GET) List<BlockHeader> getBlockHeaders(
        @RequestParam(value = "startHeight") long startHeight, @RequestParam(value = "size") int size) {
        return blockChainService.listBlockHeaders(startHeight, size);
    }

    /**
     * get the blocks
     *
     * @param startHeight
     * @param size
     * @return
     */
    @RequestMapping(value = "/get", method = RequestMethod.GET) List<Block> getBlocks(
        @RequestParam(value = "startHeight") long startHeight, @RequestParam(value = "size") int size) {
        return blockChainService.listBlocks(startHeight, size);
    }
}
