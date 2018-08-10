package com.higgs.trust.slave.core.service.block;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.common.util.Profiler;
import com.higgs.trust.slave.core.repository.BlockRepository;
import com.higgs.trust.slave.core.service.block.hash.SnapshotRootHashBuilder;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.StateRootHash;
import com.higgs.trust.slave.model.bo.TransactionReceipt;
import com.higgs.trust.slave.model.bo.context.PackageData;
import com.higgs.trust.slave.model.enums.BlockVersionEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-04-12
 */
@Slf4j @Component public class BlockServiceImpl implements BlockService {
    @Autowired BlockRepository blockRepository;
    @Autowired SnapshotRootHashBuilder snapshotRootHashBuilder;

    @Override public Long getMaxHeight() {
        return blockRepository.getMaxHeight();
    }

     @Override
    public BlockHeader buildHeader(PackageData packageData, List<TransactionReceipt> txReceipts) {
        Profiler.enter("[getMaxHeight and getBlockHeader]");
        //query max height from db
        Long maxHeight = getMaxHeight();
        if (!packageData.getCurrentPackage().getHeight().equals(maxHeight + 1L)) {
            log.error("[buildHeader]the block height:{} of the package is not greater than 1 of db.height:{}",
                packageData.getCurrentPackage().getHeight(), maxHeight);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_BLOCK_HEIGHT_UNEQUAL_ERROR);
        }
        //query block from db
        BlockHeader mBlockHeader = blockRepository.getBlockHeader(maxHeight);
        if (mBlockHeader == null) {
            log.error("[buildHeader]getBlockHeader from db is null by max block height:{}", maxHeight);
            throw new SlaveException(SlaveErrorEnum.SLAVE_PACKAGE_GET_BLOCK_ERROR);
        }
        Profiler.release();

        //build block header bo
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHeight(maxHeight + 1L);
        blockHeader.setPreviousHash(mBlockHeader.getBlockHash());
        blockHeader.setBlockTime(packageData.getCurrentPackage().getPackageTime());
        blockHeader.setVersion(BlockVersionEnum.V1.getCode());
        blockHeader.setTotalTxNum(mBlockHeader.getTotalTxNum());

        //build all root hash for block header
        Profiler.enter("[buildRootHash]");
        StateRootHash stateRootHash = snapshotRootHashBuilder.build(packageData, txReceipts);
        Profiler.release();

        blockHeader.setStateRootHash(stateRootHash);

        //to calculate the hash of block header
        Profiler.enter("[buildBlockHash]");
        String blockHash = buildBlockHash(blockHeader);
        Profiler.release();

        blockHeader.setBlockHash(blockHash);
        return blockHeader;
    }

    /**
     * get final persisted block header
     *
     * @param height
     * @return
     */
     @Override public BlockHeader getHeader(Long height) {
        return blockRepository.getBlockHeader(height);
    }

    @Override public Block buildBlock(PackageData packageData, BlockHeader blockHeader) {
        Block block = new Block();
        block.setBlockHeader(blockHeader);
        block.setSignedTxList(packageData.getCurrentBlock().getSignedTxList());
        return block;
    }

    /**
     * build dummy block
     *
     * @param height
     * @return
     */
    @Override public Block buildDummyBlock(Long height, Long blockTime) {
        BlockHeader header = new BlockHeader();
        header.setHeight(height);
        header.setBlockTime(blockTime);

        Block block = new Block();
        block.setBlockHeader(header);
        return block;
    }

     @Override
    public void persistBlock(Block block, List<TransactionReceipt> txReceipts) {
        //TODO rocks db
        blockRepository.saveBlock(block, txReceipts);
    }

    @Override public boolean compareBlockHeader(BlockHeader header1, BlockHeader header2) {
        if (!StringUtils.equals(header1.getBlockHash(), header2.getBlockHash())) {
            return false;
        }
        if (!StringUtils.equals(header1.getPreviousHash(), header2.getPreviousHash())) {
            return false;
        }
        if (!StringUtils.equals(header1.getVersion(), header2.getVersion())) {
            return false;
        }
        if (!header1.getHeight().equals(header2.getHeight())) {
            return false;
        }
        if (!header1.getBlockTime().equals(header2.getBlockTime())) {
            return false;
        }

        StateRootHash rootHash1 = header1.getStateRootHash();
        StateRootHash rootHash2 = header2.getStateRootHash();

        if (!StringUtils.equals(rootHash1.getAccountRootHash(), rootHash2.getAccountRootHash())) {
            return false;
        }
        if (!StringUtils.equals(rootHash1.getContractRootHash(), rootHash2.getContractRootHash())) {
            return false;
        }
        if (!StringUtils.equals(rootHash1.getTxReceiptRootHash(), rootHash2.getTxReceiptRootHash())) {
            return false;
        }
        if (!StringUtils.equals(rootHash1.getTxRootHash(), rootHash2.getTxRootHash())) {
            return false;
        }
        if (!StringUtils.equals(rootHash1.getPolicyRootHash(), rootHash2.getPolicyRootHash())) {
            return false;
        }
        if (!StringUtils.equals(rootHash1.getRsRootHash(), rootHash2.getRsRootHash())) {
            return false;
        }
        if (!StringUtils.equals(rootHash1.getCaRootHash(), rootHash2.getCaRootHash())) {
            return false;
        }
        return true;
    }

    @Override public Block queryBlock(Long blockHeight) {
        return blockRepository.getBlock(blockHeight);
    }

    /**
     * build hash for block header
     *
     * @param blockHeader
     * @return
     */
    @Override public String buildBlockHash(BlockHeader blockHeader) {
        HashFunction function = Hashing.sha256();
        StringBuilder builder = new StringBuilder();
        builder.append(function.hashLong(blockHeader.getHeight()));
        builder.append(function.hashLong(blockHeader.getBlockTime()));
        builder.append(function.hashString(getSafety(blockHeader.getVersion()), Charsets.UTF_8));
        builder.append(function.hashString(getSafety(blockHeader.getPreviousHash()), Charsets.UTF_8));
        StateRootHash stateRootHash = blockHeader.getStateRootHash();
        builder.append(function.hashString(getSafety(stateRootHash.getTxRootHash()), Charsets.UTF_8));
        builder.append(function.hashString(getSafety(stateRootHash.getTxReceiptRootHash()), Charsets.UTF_8));
        builder.append(function.hashString(getSafety(stateRootHash.getAccountRootHash()), Charsets.UTF_8));
        builder.append(function.hashString(getSafety(stateRootHash.getContractRootHash()), Charsets.UTF_8));
        builder.append(function.hashString(getSafety(stateRootHash.getPolicyRootHash()), Charsets.UTF_8));
        builder.append(function.hashString(getSafety(stateRootHash.getRsRootHash()), Charsets.UTF_8));
        builder.append(function.hashString(getSafety(stateRootHash.getCaRootHash()), Charsets.UTF_8));
        String hash = function.hashString(builder.toString(), Charsets.UTF_8).toString();
        return hash;
    }

    private String getSafety(String data) {
        if (data == null) {
            return "";
        }
        return data;
    }
}
