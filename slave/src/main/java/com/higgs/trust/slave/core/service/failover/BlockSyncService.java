package com.higgs.trust.slave.core.service.failover;

import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.core.service.block.hash.TxRootHashBuilder;
import com.higgs.trust.slave.core.service.consensus.cluster.ClusterService;
import com.higgs.trust.slave.core.service.pack.PackageService;
import com.higgs.trust.slave.integration.block.BlockChainClient;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service @Slf4j public class BlockSyncService {
    @Autowired private BlockService blockService;
    @Autowired private PackageService packageService;
    @Autowired private BlockChainClient blockChainClient;
    @Autowired private TxRootHashBuilder txRootHashBuilder;
    @Autowired private NodeState nodeState;
    @Autowired private ClusterService clusterService;

    /**
     * 从startHeight开始获取size个blockheader
     *
     * @param startHeight 开始高度
     * @param size        数量
     * @return blockheader列表
     */
    public List<BlockHeader> getHeaders(long startHeight, int size) {
        return blockChainClient.getBlockHeaders(nodeState.notMeNodeNameReg(), startHeight, size);
    }

    /**
     * 从startHeight开始获取size个block
     *
     * @param startHeight 开始高度
     * @param size        数量
     * @return blockheader列表
     */
    public List<Block> getBlocks(long startHeight, int size) {
        return blockChainClient.getBlocks(nodeState.notMeNodeNameReg(), startHeight, size);
    }

    /**
     * bft验证blockheader
     *
     * @param blockHeader blockheader
     * @return 协议验证结果
     */
    public Boolean bftValidating(BlockHeader blockHeader) {
        Boolean aBoolean = clusterService.validatingHeader(blockHeader, nodeState.getConsensusWaitTime());
        if (log.isTraceEnabled()) {
            log.trace("the blockheader:{} validated result by bft :{}", blockHeader.getHeight(), aBoolean);
        }
        return aBoolean;
    }

    /**
     * get the cluster height
     * @return
     */
    public Long getClusterHeight() {
        Long clusterHeight = clusterService.getClusterHeight(3, nodeState.getConsensusWaitTime());
        if (log.isTraceEnabled()) {
            log.trace("get the cluster height:{}", clusterHeight);
        }
        return clusterHeight;
    }

    /**
     * 本地验证block transactions hash
     *
     * @param previousHash previous block hash
     * @param blocks       block列表
     * @return 验证结果
     */
    public boolean validatingBlocks(String previousHash, List<Block> blocks) {
        if (blocks.isEmpty()) {
            return false;
        }
        Assert.isTrue(StringUtils.isNotBlank(previousHash), "previous hash can't be blank");
        String preHash = previousHash;
        for (Block block : blocks) {
            BlockHeader blockHeader = block.getBlockHeader();
            if (validating(preHash, block)) {
                preHash = blockHeader.getBlockHash();
            } else {
                log.warn("validating block chain failed at {}:{}", blockHeader.getHeight(), blockHeader.getBlockHash());
                return false;
            }
        }
        return true;
    }

    /**
     * 本地验证block transactions hash
     *
     * @param previousHash previous block hash
     * @param block        block
     * @return 验证结果
     */
    public boolean validating(String previousHash, Block block) {
        Assert.isTrue(StringUtils.isNotBlank(previousHash), "previous hash can't be blank");
        BlockHeader blockHeader = block.getBlockHeader();
        if (!previousHash.equalsIgnoreCase(blockHeader.getPreviousHash())) {
            if (log.isTraceEnabled()) {
                log.trace("validating block : {} previous block hash failed.", blockHeader.getHeight());
            }
            return false;
        }
        return validating(block);
    }

    /**
     * 本地验证block transactions hash
     *
     * @param block block
     * @return 验证结果
     */
    public boolean validating(Block block) {
        BlockHeader blockHeader = block.getBlockHeader();
        if (!validating(blockHeader)) {
            return false;
        }
        String txRootHash = txRootHashBuilder.buildTxs(block.getSignedTxList());
        if (!blockHeader.getStateRootHash().getTxRootHash().equalsIgnoreCase(txRootHash)) {
            if (log.isTraceEnabled()) {
                log.trace("validating block: {} tx root hash failed.", blockHeader.getHeight());
            }
            return false;
        }
        return true;
    }

    /**
     * 本地验证区块链hash
     *
     * @param blockHeaders 要验证的blockheader列表
     * @return 验证结果
     */
    public boolean validating(List<BlockHeader> blockHeaders) {
        if (blockHeaders.isEmpty()) {
            return false;
        }
        String preHash = blockHeaders.get(0).getPreviousHash();
        return validating(preHash, blockHeaders);
    }

    /**
     * 本地验证区块链hash
     *
     * @param previousHash 前一区块hash
     * @param blockHeaders 要验证的blockheader列表
     * @return 验证结果
     */
    public boolean validating(String previousHash, List<BlockHeader> blockHeaders) {
        if (blockHeaders.isEmpty()) {
            return false;
        }
        Assert.isTrue(StringUtils.isNotBlank(previousHash), "previous hash can't be blank");
        String preHash = previousHash;
        for (BlockHeader blockHeader : blockHeaders) {
            if (validating(preHash, blockHeader)) {
                preHash = blockHeader.getBlockHash();
            } else {
                log.warn("validating block chain headers failed at {}:{}", blockHeader.getHeight(),
                    blockHeader.getBlockHash());
                return false;
            }
        }
        return true;
    }

    /**
     * validating block header with previous hash
     *
     * @param previousHash
     * @param blockHeader
     * @return
     */
    public boolean validating(String previousHash, BlockHeader blockHeader) {
        if (blockHeader == null) {
            return false;
        }
        Assert.isTrue(StringUtils.isNotBlank(previousHash), "previous hash can't be blank");
        boolean validated = previousHash.equalsIgnoreCase(blockHeader.getPreviousHash()) && validating(blockHeader);
        if (!validated && log.isDebugEnabled()) {
            log.debug("validating block chain header : {} hash failed.", blockHeader.getHeight());
        }
        return validated;
    }

    /**
     * validating block header
     *
     * @param blockHeader
     * @return
     */
    public boolean validating(BlockHeader blockHeader) {
        if (blockHeader == null) {
            return false;
        }
        String currentHash = blockService.buildBlockHash(blockHeader);
        boolean validated = currentHash.equalsIgnoreCase(blockHeader.getBlockHash());
        if (!validated && log.isDebugEnabled()) {
            log.debug("validating block header : {} hash failed.", blockHeader.getHeight());
        }
        return validated;
    }

}
