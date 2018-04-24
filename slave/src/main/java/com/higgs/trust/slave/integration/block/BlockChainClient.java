/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.slave.integration.block;

import com.higgs.trust.slave.api.vo.TransactionVO;
import com.higgs.trust.common.feign.FeignRibbonConstants;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//TODO lichengcai 记得修改真正的client名
@FeignClient("${higgs.trust.prefix}") public interface BlockChainClient {
    /**
     * get the block headers
     * @param nodeNameReg node name regex
     * @param startHeight
     * @param size
     * @return
     *
     */
    @RequestMapping(value = "/getBlockHeaders", method = RequestMethod.GET) List<BlockHeader> getBlockHeaders(
        @RequestHeader(FeignRibbonConstants.NODE_NAME_REG) String nodeNameReg,
        @RequestParam(value = "startHeight") long startHeight, @RequestParam(value = "size") int size);

    /**
     * get the blocks
     * @param nodeNameReg node name regex
     * @param startHeight
     * @param size
     * @return
     */
    @RequestMapping(value = "/getBlocks", method = RequestMethod.GET) List<Block> getBlocks(
        @RequestHeader(FeignRibbonConstants.NODE_NAME_REG) String nodeNameReg,
        @RequestParam(value = "startHeight") long startHeight, @RequestParam(value = "size") int size);

    /**
     * submit transaction
     * @param nodeNameReg node name regex
     * @param transactions
     * @return submit failed transaction list
     */
    @RequestMapping(value = "/block/transaction/submit", method = RequestMethod.POST) List<TransactionVO> submitTransaction(
        @RequestHeader(FeignRibbonConstants.NODE_NAME_REG) String nodeNameReg,
        @RequestParam(value = "transactions") List<SignedTransaction> transactions);


}
