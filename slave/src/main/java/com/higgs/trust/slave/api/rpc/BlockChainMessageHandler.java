package com.higgs.trust.slave.api.rpc;

import com.higgs.trust.network.NetworkManage;
import com.higgs.trust.slave.api.BlockChainService;
import com.higgs.trust.slave.api.rpc.request.BlockRequest;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author duhongming
 * @date 2018/9/18
 */
@Component
@Slf4j
public class BlockChainMessageHandler implements InitializingBean {

    @Autowired
    private BlockChainService blockChainService;

    @Autowired
    private NetworkManage networkManage;

    /**
     * get the block headers
     * @param request
     * @return
     */
    public List<BlockHeader> getBlockHeaders(BlockRequest request) {
        return blockChainService.listBlockHeaders(request.getStartHeight(), request.getSize());
    }

    /**
     * get the blocks
     * @param request
     * @return
     */
    public List<Block> getBlocks(BlockRequest request) {
        return blockChainService.listBlocks(request.getStartHeight(), request.getSize());
    }

    @Override
    public void afterPropertiesSet() {
        networkManage.registerHandler("block/header/get", this::getBlockHeaders);
        networkManage.registerHandler("block/get", this::getBlocks);
    }
}
