package com.higgs.trust.slave.core.service.failover;

import com.higgs.trust.slave.common.enums.NodeStateEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.FailoverExecption;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.core.service.pack.PackageService;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Service @Slf4j public class SyncService {

    @Autowired private FailoverProperties properties;
    @Autowired private SyncPackageCache cache;
    @Autowired private BlockRepository blockRepository;
    @Autowired private BlockService blockService;
    @Autowired private BlockSyncService blockSyncService;
    @Autowired private PackageService packageService;
    @Autowired private NodeState nodeState;

    /**
     * 自动同步区块
     */
    public void autoSync() {
        if (!nodeState.isState(NodeStateEnum.AutoSync)) {
            return;
        }
        log.info("auto sync starting ...");
        try {
            Long currentHeight = blockRepository.getMaxHeight();
            Long clusterHeight = null;
            int tryTimes = 0;
            do {
                clusterHeight = blockSyncService.getClusterHeight(3);
                if (clusterHeight != null) {
                    break;
                }
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    log.warn("self check error.", e);
                }
            } while (++tryTimes < properties.getTryTimes());
            if (clusterHeight == null) {
                throw new SlaveException(SlaveErrorEnum.SLAVE_CONSENSUS_GET_RESULT_FAILED);
            }
            if (clusterHeight <= currentHeight + properties.getThreshold()) {
                nodeState.changeState(NodeStateEnum.AutoSync, NodeStateEnum.Running);
                return;
            }
            cache.reset(clusterHeight);
            do {
                sync(currentHeight + 1, properties.getHeaderStep());
                currentHeight = blockRepository.getMaxHeight();
                //如果没有package接收，根据latestHeight判断是否在阈值内，否则，根据cache判断
            } while (cache.getMinHeight() <= clusterHeight ? clusterHeight > currentHeight + properties.getThreshold() :
                currentHeight + 1 < cache.getMinHeight());
        } catch (Exception e) {
            nodeState.changeState(NodeStateEnum.AutoSync, NodeStateEnum.Offline);
            throw e;
        }
        nodeState.changeState(NodeStateEnum.AutoSync, NodeStateEnum.Running);
    }

    /**
     * 同步指定数量的区块
     *
     * @param startHeight 开始高度
     * @param size        同步数量
     */
    public synchronized void sync(long startHeight, int size) {
        if (!nodeState.isState(NodeStateEnum.AutoSync, NodeStateEnum.ArtificialSync)) {
            throw new FailoverExecption(SlaveErrorEnum.SLAVE_FAILOVER_STATE_NOT_ALLOWED);
        }
        log.info("starting to sync the block, start height:{}, size:{}", startHeight, size);
        Assert.isTrue(size > 0, "the size of sync block must > 0");
        long currentHeight = blockRepository.getMaxHeight();
        log.info("local current block height:{}", currentHeight);
        if (currentHeight != startHeight - 1) {
            throw new FailoverExecption(SlaveErrorEnum.SLAVE_FAILOVER_START_HEIGHT_ERROR);
        }
        int tryTimes = 0;
        List<BlockHeader> headers = null;
        Boolean headerValidated = false;
        BlockHeader currentHeader = blockRepository.getBlockHeader(currentHeight);
        //批量拉取header并验证
        do {
            headers = blockSyncService.getHeaders(startHeight, size);
            if (headers == null || headers.isEmpty()) {
                continue;
            }
            if (log.isDebugEnabled()) {
                log.debug("get the block headers from other node:{}", ToStringBuilder.reflectionToString(headers));
            }
            headerValidated = blockSyncService.validating(currentHeader.getBlockHash(), headers);
            if (log.isDebugEnabled()) {
                log.debug("the block headers local valid result:{}", headerValidated);
            }
            if (!headerValidated) {
                continue;
            }
            headerValidated = blockSyncService.bftValidating(headers.get(headers.size() - 1));
            if (headerValidated == null || !headerValidated) {
                continue;
            }
        } while ((headerValidated == null || !headerValidated) && ++tryTimes < properties.getTryTimes());
        if (headerValidated == null || !headerValidated) {
            throw new FailoverExecption(SlaveErrorEnum.SLAVE_FAILOVER_GET_VALIDATING_HEADERS_FAILED);
        }
        int headerSize = headers.size();
        int startIndex = 0;
        long blockStartHeight = headers.get(startIndex).getHeight();
        long blockEndHeight = headers.get(headers.size() - 1).getHeight();
        int blockSize = properties.getBlockStep();
        BlockHeader preHeader = currentHeader;

        do {
            if (blockStartHeight + properties.getBlockStep() > blockEndHeight) {
                blockSize = new Long(blockEndHeight - blockStartHeight + 1).intValue();
            }
            List<Block> blocks = getAndValidatingBlock(preHeader, blockStartHeight, blockSize);
            blockSize = blocks.size();
            Block lastBlock = blocks.get(blockSize - 1);
            //验证最后块的header是否与header列表中的一致
            boolean blocksValidated =
                blockService.compareBlockHeader(lastBlock.getBlockHeader(), headers.get(startIndex + blockSize - 1));
            if (!blocksValidated) {
                log.error("validating the last block of blocks failed");
                throw new FailoverExecption(SlaveErrorEnum.SLAVE_FAILOVER_SYNC_BLOCK_VALIDATING_FAILED);
            }
            blocks.forEach(block -> syncBlock(block));
            startIndex = startIndex + blockSize;
            blockStartHeight = blockStartHeight + blockSize;
            preHeader = headers.get(startIndex - 1);
        } while (startIndex < headerSize - 1);
    }

    /**
     * 从指定节点同步指定数量的区块
     *
     * @param startHeight 开始高度
     * @param size        同步数量
     */
    public synchronized void sync(long startHeight, int size, String fromNodeName) {
        if (!nodeState.isState(NodeStateEnum.ArtificialSync)) {
            throw new FailoverExecption(SlaveErrorEnum.SLAVE_FAILOVER_STATE_NOT_ALLOWED);
        }
        log.info("starting to sync the block from node {}, start height:{}, size:{}", fromNodeName, startHeight, size);
        Assert.isTrue(size > 0, "the size of sync block must > 0");
        long currentHeight = blockRepository.getMaxHeight();
        log.info("local current block height:{}", currentHeight);
        if (currentHeight != startHeight - 1) {
            throw new FailoverExecption(SlaveErrorEnum.SLAVE_FAILOVER_START_HEIGHT_ERROR);
        }
        int tryTimes = 0;
        List<BlockHeader> headers = null;
        Boolean headerValidated = false;
        BlockHeader currentHeader = blockRepository.getBlockHeader(currentHeight);
        //批量拉取header并验证
        do {
            headers = blockSyncService.getHeadersFromNode(startHeight, size, fromNodeName);
            if (headers == null || headers.isEmpty()) {
                continue;
            }
            if (log.isDebugEnabled()) {
                log.debug("get the block headers from other node:{}", ToStringBuilder.reflectionToString(headers));
            }
            headerValidated = blockSyncService.validating(currentHeader.getBlockHash(), headers);
            if (log.isDebugEnabled()) {
                log.debug("the block headers local valid result:{}", headerValidated);
            }
            if (!headerValidated) {
                continue;
            }
        } while (!headerValidated && ++tryTimes < properties.getTryTimes());
        if (!headerValidated) {
            throw new FailoverExecption(SlaveErrorEnum.SLAVE_FAILOVER_GET_VALIDATING_HEADERS_FAILED);
        }
        int headerSize = headers.size();
        int startIndex = 0;
        long blockStartHeight = headers.get(startIndex).getHeight();
        long blockEndHeight = headers.get(headers.size() - 1).getHeight();
        int blockSize = properties.getBlockStep();
        BlockHeader preHeader = currentHeader;

        do {
            if (blockStartHeight + properties.getBlockStep() > blockEndHeight) {
                blockSize = new Long(blockEndHeight - blockStartHeight + 1).intValue();
            }
            List<Block> blocks = getAndValidatingBlock(preHeader, blockStartHeight, blockSize, fromNodeName);
            blockSize = blocks.size();
            Block lastBlock = blocks.get(blockSize - 1);
            //验证最后块的header是否与header列表中的一致
            boolean blocksValidated =
                blockService.compareBlockHeader(lastBlock.getBlockHeader(), headers.get(startIndex + blockSize - 1));
            if (!blocksValidated) {
                log.error("validating the last block of blocks failed");
                throw new FailoverExecption(SlaveErrorEnum.SLAVE_FAILOVER_SYNC_BLOCK_VALIDATING_FAILED);
            }
            blocks.forEach(block -> syncBlock(block));
            startIndex = startIndex + blockSize;
            blockStartHeight = blockStartHeight + blockSize;
            preHeader = headers.get(startIndex - 1);
        } while (startIndex < headerSize - 1);
    }

    /**
     * receive package height
     *
     * @param height
     */
    public void receivePackHeight(long height) {
        cache.receivePackHeight(height);
    }

    /**
     * get the blocks and validate it
     *
     * @param preHeader
     * @param startHeight
     * @param size
     * @return
     */
    private List<Block> getAndValidatingBlock(BlockHeader preHeader, long startHeight, int size) {
        int tryTimes = 0;
        List<Block> blocks = null;
        boolean blockValidated = false;
        do {
            blocks = blockSyncService.getBlocks(startHeight, size);
            if (blocks == null || blocks.isEmpty()) {
                continue;
            }
            blockValidated = blockSyncService.validatingBlocks(preHeader.getBlockHash(), blocks);
            if (!blockValidated) {
                continue;
            }
        } while (!blockValidated && ++tryTimes < properties.getTryTimes());
        if (!blockValidated) {
            throw new FailoverExecption(SlaveErrorEnum.SLAVE_FAILOVER_GET_VALIDATING_BLOCKS_FAILED);
        }
        return blocks;
    }

    /**
     * get the blocks and validate it
     *
     * @param preHeader
     * @param startHeight
     * @param size
     * @return
     */
    private List<Block> getAndValidatingBlock(BlockHeader preHeader, long startHeight, int size, String fromNode) {
        int tryTimes = 0;
        List<Block> blocks = null;
        boolean blockValidated = false;
        do {
            blocks = blockSyncService.getBlocksFromNode(startHeight, size, fromNode);
            if (blocks == null || blocks.isEmpty()) {
                continue;
            }
            blockValidated = blockSyncService.validatingBlocks(preHeader.getBlockHash(), blocks);
            if (!blockValidated) {
                continue;
            }
        } while (!blockValidated && ++tryTimes < properties.getTryTimes());
        if (!blockValidated) {
            throw new FailoverExecption(SlaveErrorEnum.SLAVE_FAILOVER_GET_VALIDATING_BLOCKS_FAILED);
        }
        return blocks;
    }

    /**
     * 同步单一block
     *
     * @param block 区块
     * @return 同步结果
     */
    private void syncBlock(Block block) {
        BlockHeader blockHeader = block.getBlockHeader();
        log.info("Sync block:{}", blockHeader.getHeight());
        Package pack = new Package();
        pack.setPackageTime(blockHeader.getBlockTime());
        pack.setHeight(blockHeader.getHeight());
        pack.setStatus(PackageStatusEnum.FAILOVER);
        pack.setSignedTxList(block.getSignedTxList());
        PackContext packContext = packageService.createPackContext(pack);
        packageService.process(packContext);
        boolean persistValid =
            blockService.compareBlockHeader(packContext.getCurrentBlock().getBlockHeader(), block.getBlockHeader());
        if (!persistValid) {
            throw new FailoverExecption(SlaveErrorEnum.SLAVE_FAILOVER_SYNC_BLOCK_PERSIST_RESULT_INVALID);
        }
    }
}
