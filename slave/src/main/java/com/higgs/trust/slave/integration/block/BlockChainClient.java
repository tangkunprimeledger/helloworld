/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.integration.block;

import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.api.vo.TransactionVO;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//@FeignClient("${higgs.trust.prefix}")
public interface BlockChainClient {
    /**
     * get the block headers
     *
     * @param nodeNameReg node name regex
     * @param startHeight
     * @param size
     * @return
     */
    @RequestMapping(value = "/block/header/get", method = RequestMethod.GET)
    List<BlockHeader> getBlockHeaders(String nodeNameReg, @RequestParam(value = "startHeight") long startHeight, @RequestParam(value = "size") int size);

    /**
     * get the block headers
     *
     * @param nodeName    node name
     * @param startHeight
     * @param size
     * @return
     */
    @RequestMapping(value = "/block/header/get", method = RequestMethod.GET)
    List<BlockHeader> getBlockHeadersFromNode(String nodeName,
        @RequestParam(value = "startHeight") long startHeight, @RequestParam(value = "size") int size);

    /**
     * get the blocks
     *
     * @param nodeNameReg node name regex
     * @param startHeight
     * @param size
     * @return
     */
    @RequestMapping(value = "/block/get", method = RequestMethod.GET)
    List<Block> getBlocks(String nodeNameReg,
        @RequestParam(value = "startHeight") long startHeight, @RequestParam(value = "size") int size);

    /**
     * get the blocks
     *
     * @param nodeName    node name
     * @param startHeight
     * @param size
     * @return
     */
    @RequestMapping(value = "/block/get", method = RequestMethod.GET)
    List<Block> getBlocksFromNode(String nodeName,
        @RequestParam(value = "startHeight") long startHeight, @RequestParam(value = "size") int size);

    /**
     * submit transaction
     *
     * @param nodeName     node name
     * @param transactions
     * @return submit failed transaction list
     */
    @RequestMapping(value = "/transaction/master/submit", method = RequestMethod.POST)
    RespData<List<TransactionVO>> submitToMaster(String nodeName,
        @RequestBody List<SignedTransaction> transactions);

}
