package com.higgs.trust.slave.core.scheduler;

import com.higgs.trust.slave.common.enums.NodeStateEnum;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.FailoverExecption;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.managment.NodeState;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.repository.PackageRepository;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.core.service.failover.BlockSyncService;
import com.higgs.trust.slave.core.service.failover.FailoverProperties;
import com.higgs.trust.slave.core.service.pack.PackageService;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.enums.BlockHeaderTypeEnum;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service @Slf4j public class FailoverSchedule {

    @Autowired private BlockSyncService blockSyncService;
    @Autowired private BlockService blockService;
    @Autowired private PackageService packageService;
    @Autowired private BlockRepository blockRepository;
    @Autowired private PackageRepository packageRepository;
    @Autowired private NodeState nodeState;
    @Autowired private FailoverProperties properties;

    /**
     * 自动failover，判断状态是否为NodeStateEnum.Running
     */
    @Scheduled(fixedDelay = 3000) public void failover() {
        if (!nodeState.isState(NodeStateEnum.Running)) {
            return;
        }
        int size = 0;
        try {
            do {
                Long height = blockService.getMaxHeight();
                long nextHeight = height + 1;
                if (!needFailover(nextHeight)) {
                    break;
                }
                if (!failover(nextHeight)) {
                    log.warn("failover block:{} failed", nextHeight);
                    break;
                }
            } while (++size < properties.getFailoverStep());
        } catch (SlaveException e) {
            log.error("failover block error：{}", e.getCode().getDescription(), e);
        } catch (Exception e) {
            log.error("failover error", e);
        }
    }

    /**
     * failover指定高度
     *
     * @param height 区块高度
     */
    public boolean failover(long height) {
        if (!nodeState.isState(NodeStateEnum.Running, NodeStateEnum.ArtificialSync)) {
            throw new FailoverExecption(SlaveErrorEnum.SLAVE_FAILOVER_STATE_NOT_ALLOWED);
        }
        if (!checkAndInsert(height)) {
            return true;
        }
        log.info("failover block:{}", height);
        BlockHeader preBlockHeader = blockRepository.getBlockHeader(height - 1);
        int tryTimes = 0;
        Block block = null;
        boolean validated = false;
        do {
            List<Block> blocks = blockSyncService.getBlocks(height, 1);
            if (!blocks.isEmpty()) {
                block = blocks.get(0);
                if (log.isDebugEnabled()) {
                    log.debug("got the block:{}", block);
                }
                validated = blockSyncService.validating(preBlockHeader.getBlockHash(), block);
            }
        } while (!validated && ++tryTimes < properties.getTryTimes());
        if (!validated) {
            throw new FailoverExecption(SlaveErrorEnum.SLAVE_FAILOVER_GET_BLOCKS_FAILED);
        }
        return failoverBlock(block);
    }

    /**
     * 检查是否需要failover
     *
     * @param height
     * @return
     */
    private boolean needFailover(long height) {
        Long minHeight =
            packageRepository.getMinHeight(height, Collections.singleton(PackageStatusEnum.INIT.getCode()));
        if (minHeight == null || minHeight <= height) {
            return false;
        }
        Package pack = packageRepository.load(height);
        if (pack == null || pack.getStatus() == PackageStatusEnum.FAILOVER) {
            return true;
        }
        return false;
    }

    /**
     * check whether failover is needed, and insert the failover package if necessary.
     *
     * @param height 区块高度
     * @return
     */
    private boolean checkAndInsert(long height) {
        Long minHeight =
            packageRepository.getMinHeight(height, Collections.singleton(PackageStatusEnum.INIT.getCode()));
        if (minHeight == null || minHeight <= height) {
            return false;
        }
        Package pack = packageRepository.load(height);
        if (pack == null) {
            return insertFailoverPackage(height);
        } else {
            return pack.getStatus() == PackageStatusEnum.FAILOVER;
        }
    }

    /**
     * insert the failover package if the height package not exist
     *
     * @param height the height of package
     * @return
     */
    private boolean insertFailoverPackage(long height) {
        Package pack = new Package();
        pack.setPackageTime(System.currentTimeMillis());
        pack.setHeight(height);
        pack.setStatus(PackageStatusEnum.FAILOVER);
        try {
            packageRepository.save(pack);
        } catch (Exception e) {
            log.warn("insert failover package failed.", e);
            return false;
        }
        return true;
    }

    /**
     * failover block,执行validating,结果db验证，执行persisting,结果db验证，验证结果db不存在时结束，下次继续验证
     *
     * @param block 区块
     * @return 同步结果
     */
    public boolean failoverBlock(Block block) {
        BlockHeader blockHeader = block.getBlockHeader();
        Package pack = new Package();
        pack.setPackageTime(blockHeader.getBlockTime());
        pack.setHeight(blockHeader.getHeight());
        pack.setStatus(PackageStatusEnum.FAILOVER);
        pack.setSignedTxList(block.getSignedTxList());
        PackContext packContext = packageService.createPackContext(pack);
        packageService.validating(packContext);
        BlockHeader tempHeader = blockService.getTempHeader(blockHeader.getHeight(), BlockHeaderTypeEnum.TEMP_TYPE);
        BlockHeader consensusHeader =
            blockService.getTempHeader(blockHeader.getHeight(), BlockHeaderTypeEnum.CONSENSUS_VALIDATE_TYPE);
        if (consensusHeader == null) {
            return false;
        }
        boolean validated = blockService.compareBlockHeader(tempHeader, consensusHeader);
        if (validated) {
            packContext = packageService.createPackContext(pack);
            packageService.persisting(packContext);
        }
        return false;
    }
}
