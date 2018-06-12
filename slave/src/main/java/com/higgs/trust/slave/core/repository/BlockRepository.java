package com.higgs.trust.slave.core.repository;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.slave.api.vo.BlockVO;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.dao.block.BlockDao;
import com.higgs.trust.slave.dao.block.BlockHeaderDao;
import com.higgs.trust.slave.dao.po.block.BlockHeaderPO;
import com.higgs.trust.slave.dao.po.block.BlockPO;
import com.higgs.trust.slave.model.bo.*;
import com.higgs.trust.slave.model.convert.BlockConvert;
import com.higgs.trust.slave.model.enums.BlockHeaderTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author tangfashuang
 * @desc block repository
 * @date 2018/04/10 16:26
 */
@Repository @Slf4j public class BlockRepository {
    @Autowired BlockDao blockDao;
    @Autowired BlockHeaderDao blockHeaderDao;
    @Autowired TransactionRepository transactionRepository;

    /**
     * get max height of block
     *
     * @return
     */
    public Long getMaxHeight() {
        return blockDao.getMaxHeight();
    }

    /**
     * get max height of block
     *
     * @return
     */
    public List<Long> getMaxHeight(int size) {
        return blockDao.getLimitHeight(size);
    }

    /**
     * get block info by block height
     *
     * @param height
     * @return
     */
    public Block getBlock(Long height) {
        BlockPO blockPO = blockDao.queryByHeight(height);
        if (blockPO == null) {
            return null;
        }
        Block block = new Block();
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHeight(blockPO.getHeight());
        blockHeader.setBlockHash(blockPO.getBlockHash());
        blockHeader.setBlockTime(blockPO.getBlockTime() != null ? blockPO.getBlockTime().getTime() : null);
        blockHeader.setPreviousHash(blockPO.getPreviousHash());
        blockHeader.setVersion(blockPO.getVersion());
        StateRootHash rootHash = new StateRootHash();
        rootHash.setRsRootHash(blockPO.getRsRootHash());
        rootHash.setTxRootHash(blockPO.getTxRootHash());
        rootHash.setTxReceiptRootHash(blockPO.getTxReceiptRootHash());
        rootHash.setPolicyRootHash(blockPO.getPolicyRootHash());
        rootHash.setContractRootHash(blockPO.getContractRootHash());
        rootHash.setAccountRootHash(blockPO.getAccountRootHash());
        rootHash.setCaRootHash(blockPO.getCaRootHash());
        blockHeader.setStateRootHash(rootHash);
        block.setBlockHeader(blockHeader);
        if (height == 1) {
            block.setGenesis(true);
        }
        //txs
        block.setSignedTxList(transactionRepository.queryTransactions(height));
        return block;
    }

    /**
     * list the blocks
     *
     * @param startHeight start height
     * @param size        size
     * @return
     */
    public List<Block> listBlocks(long startHeight, int size) {
        List<BlockPO> blockPOs = blockDao.queryBlocks(startHeight, size);
        if (blockPOs == null || blockPOs.isEmpty()) {
            return Collections.emptyList();
        }
        List<Block> blocks = new ArrayList<>();
        blockPOs.forEach(blockPO -> {
            Block block = new Block();
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(blockPO.getHeight());
            blockHeader.setBlockHash(blockPO.getBlockHash());
            blockHeader.setBlockTime(blockPO.getBlockTime() != null ? blockPO.getBlockTime().getTime() : null);
            blockHeader.setPreviousHash(blockPO.getPreviousHash());
            blockHeader.setVersion(blockPO.getVersion());
            StateRootHash rootHash = new StateRootHash();
            rootHash.setRsRootHash(blockPO.getRsRootHash());
            rootHash.setTxRootHash(blockPO.getTxRootHash());
            rootHash.setTxReceiptRootHash(blockPO.getTxReceiptRootHash());
            rootHash.setPolicyRootHash(blockPO.getPolicyRootHash());
            rootHash.setContractRootHash(blockPO.getContractRootHash());
            rootHash.setAccountRootHash(blockPO.getAccountRootHash());
            rootHash.setCaRootHash(blockPO.getCaRootHash());
            blockHeader.setStateRootHash(rootHash);
            block.setBlockHeader(blockHeader);
            if (blockPO.getHeight() == 1) {
                block.setGenesis(true);
            }
            //txs
            block.setSignedTxList(transactionRepository.queryTransactions(blockPO.getHeight()));
            blocks.add(block);
        });

        return blocks;
    }

    /**
     * list the block headers
     *
     * @param startHeight start height
     * @param size        size
     * @return
     */
    public List<BlockHeader> listBlockHeaders(long startHeight, int size) {
        List<BlockPO> blockPOs = blockDao.queryBlocks(startHeight, size);
        if (blockPOs == null || blockPOs.isEmpty()) {
            return Collections.emptyList();
        }
        List<BlockHeader> headers = new ArrayList<>();
        blockPOs.forEach(blockPO -> {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.setHeight(blockPO.getHeight());
            blockHeader.setBlockHash(blockPO.getBlockHash());
            blockHeader.setBlockTime(blockPO.getBlockTime() != null ? blockPO.getBlockTime().getTime() : null);
            blockHeader.setPreviousHash(blockPO.getPreviousHash());
            blockHeader.setVersion(blockPO.getVersion());
            StateRootHash rootHash = new StateRootHash();
            rootHash.setRsRootHash(blockPO.getRsRootHash());
            rootHash.setTxRootHash(blockPO.getTxRootHash());
            rootHash.setTxReceiptRootHash(blockPO.getTxReceiptRootHash());
            rootHash.setPolicyRootHash(blockPO.getPolicyRootHash());
            rootHash.setContractRootHash(blockPO.getContractRootHash());
            rootHash.setAccountRootHash(blockPO.getAccountRootHash());
            rootHash.setCaRootHash(blockPO.getCaRootHash());
            blockHeader.setStateRootHash(rootHash);
            headers.add(blockHeader);
        });

        return headers;
    }

    /**
     * get block header data from db
     *
     * @param height
     * @return
     */
    public BlockHeader getTempHeader(Long height, BlockHeaderTypeEnum blockHeaderTypeEnum) {
        BlockHeaderPO blockHeaderPO = blockHeaderDao.queryByHeight(height, blockHeaderTypeEnum.getCode());
        if (blockHeaderPO == null) {
            return null;
        }
        return JSON.toJavaObject(JSON.parseObject(blockHeaderPO.getHeaderData()), BlockHeader.class);
    }

    /**
     * delete the temp header by height
     *
     * @param height
     * @param blockHeaderTypeEnum
     */
    public void deleteTempHeader(Long height, BlockHeaderTypeEnum blockHeaderTypeEnum) {
        blockHeaderDao.deleteBlockHeader(height, blockHeaderTypeEnum.getCode());
    }

    /**
     * save to db
     *
     * @param header
     */
    public void saveTempHeader(BlockHeader header, BlockHeaderTypeEnum blockHeaderTypeEnum) {
        BlockHeaderPO blockHeaderPO = new BlockHeaderPO();
        blockHeaderPO.setHeight(header.getHeight());
        blockHeaderPO.setType(blockHeaderTypeEnum.getCode());
        String headerData = JSON.toJSONString(header);
        blockHeaderPO.setHeaderData(headerData);
        blockHeaderPO.setCreateTime(new Date());
        try {
            blockHeaderDao.add(blockHeaderPO);
        } catch (DuplicateKeyException e) {
            log.error("[saveTempHeader] is idempotent blockHeight:{}", header.getHeight());
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT, e);
        }
    }

    /**
     * get block header data from db
     *
     * @param height
     * @return
     */
    public BlockHeader getBlockHeader(Long height) {
        BlockPO blockPO = blockDao.queryByHeight(height);
        if (blockPO == null) {
            return null;
        }
        return BlockConvert.convertBlockPOToBlockHeader(blockPO);
    }

    /**
     * save to db
     *
     * @param block
     * @param txReceipts
     */
    public void saveBlock(Block block, List<TransactionReceipt> txReceipts) {
        log.info("[BlockRepository.saveBlock] is start");
        BlockHeader blockHeader = block.getBlockHeader();
        BlockPO blockPO = new BlockPO();
        blockPO.setHeight(blockHeader.getHeight());
        blockPO.setPreviousHash(blockHeader.getPreviousHash());
        blockPO.setVersion(blockHeader.getVersion());
        Date blockTime = new Date(blockHeader.getBlockTime());
        blockPO.setBlockTime(blockTime);
        blockPO.setBlockHash(blockHeader.getBlockHash());
        StateRootHash rootHash = blockHeader.getStateRootHash();
        blockPO.setTxRootHash(rootHash.getTxRootHash());
        blockPO.setTxReceiptRootHash(rootHash.getTxReceiptRootHash());
        blockPO.setAccountRootHash(rootHash.getAccountRootHash());
        blockPO.setContractRootHash(rootHash.getContractRootHash());
        blockPO.setPolicyRootHash(rootHash.getPolicyRootHash());
        blockPO.setRsRootHash(rootHash.getRsRootHash());
        blockPO.setCaRootHash(rootHash.getCaRootHash());
        List<SignedTransaction> txs = block.getSignedTxList();

        //add transaction number to block table
        blockPO.setTxNum(txs.size());

        //save block
        try {
            blockDao.add(blockPO);
        } catch (DuplicateKeyException e) {
            log.error("[saveBlock] is idempotent blockHeight:{}", blockHeader.getHeight());
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        }
        //save transactions
        transactionRepository.batchSaveTransaction(blockHeader.getHeight(), blockTime, txs, txReceipts);
        log.info("[BlockRepository.saveBlock] is end");
    }

    public List<BlockVO> queryBlocksWithCondition(Long height, String blockHash, Integer pageNum, Integer pageSize) {
        if (null != blockHash) {
            blockHash = blockHash.trim();
        }

        List<BlockPO> list = blockDao.queryBlocksWithCondition(height, blockHash, (pageNum - 1) * pageSize, pageSize);

        return BeanConvertor.convertList(list, BlockVO.class);
    }

    public long countBlocksWithCondition(Long height, String blockHash) {

        return blockDao.countBlockWithCondition(height, blockHash);
    }
}
