package com.higgs.trust.slave.core.repository;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.slave.api.vo.BlockVO;
import com.higgs.trust.slave.common.config.InitConfig;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.config.SystemPropertyRepository;
import com.higgs.trust.slave.dao.mysql.block.BlockDao;
import com.higgs.trust.slave.dao.po.block.BlockPO;
import com.higgs.trust.slave.dao.rocks.block.BlockRocksDao;
import com.higgs.trust.slave.model.bo.*;
import com.higgs.trust.slave.model.bo.config.SystemProperty;
import com.higgs.trust.slave.model.convert.BlockConvert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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
    @Autowired BlockRocksDao blockRocksDao;
    @Autowired TransactionRepository transactionRepository;
    @Autowired SystemPropertyRepository systemPropertyRepository;
    @Autowired InitConfig initConfig;

    /**
     * get max height of block
     *
     * @return
     */
    public Long getMaxHeight() {
        if (initConfig.isUseMySQL()) {
            return blockDao.getMaxHeight();
        } else {
            SystemProperty bo = systemPropertyRepository.queryByKey(Constant.MAX_BLOCK_HEIGHT);
            return bo != null && !StringUtils.isEmpty(bo.getValue()) ? Long.parseLong(bo.getValue()) : null;
        }
    }

    /**
     * get max height of block
     *
     * @return
     */
    public List<Long> getLimitHeight(int size) {
        if (initConfig.isUseMySQL()) {
            return blockDao.getLimitHeight(size);
        } else {
            Long maxBlockHeight = getMaxHeight();
            if (null == maxBlockHeight) {
                return null;
            }

            List<String> blockHeights = new ArrayList<>();
            while (size-- > 0 && maxBlockHeight > 0) {
                blockHeights.add(String.valueOf(maxBlockHeight--));
            }
            return blockRocksDao.getLimitHeight(blockHeights);
        }
    }

    /**
     * get block info by block height
     *
     * @param height
     * @return
     */
    public Block getBlock(Long height) {
        BlockPO blockPO;
        if (initConfig.isUseMySQL()) {
            blockPO = blockDao.queryByHeight(height);
        } else {
            blockPO = blockRocksDao.get(String.valueOf(height));
        }
        return convertPOToBO(blockPO);
    }

    private Block convertPOToBO(BlockPO blockPO) {
        if (null == blockPO) {
            return null;
        }
        Block block = new Block();
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHeight(blockPO.getHeight());
        blockHeader.setBlockHash(blockPO.getBlockHash());
        blockHeader.setBlockTime(blockPO.getBlockTime() != null ? blockPO.getBlockTime().getTime() : null);
        blockHeader.setPreviousHash(blockPO.getPreviousHash());
        blockHeader.setVersion(blockPO.getVersion());
        blockHeader.setTotalTxNum(blockPO.getTotalTxNum());
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
        if (initConfig.isUseMySQL()) {
            block.setSignedTxList(transactionRepository.queryTransactions(blockPO.getHeight()));
        } else {
            block.setSignedTxList(transactionRepository.convertPOsToBOs(blockPO.getTxPOs()));
        }
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
        List<BlockPO> blockPOs;
        if (initConfig.isUseMySQL()) {
            blockPOs = blockDao.queryBlocks(startHeight, size);

        } else {
            blockPOs = blockRocksDao.queryBlocks(startHeight, size);
        }

        if (CollectionUtils.isEmpty(blockPOs)) {
            return Collections.emptyList();
        }

        List<Block> blocks = new ArrayList<>();
        for (BlockPO po : blockPOs) {
            blocks.add(convertPOToBO(po));
        }
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
        List<BlockPO> blockPOs;
        if (initConfig.isUseMySQL()) {
            blockPOs = blockDao.queryBlocks(startHeight, size);
        } else {
            blockPOs = blockRocksDao.queryBlocks(startHeight, size);
        }

        if (CollectionUtils.isEmpty(blockPOs)) {
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
            blockHeader.setTotalTxNum(blockPO.getTotalTxNum());
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
    public BlockHeader getBlockHeader(Long height) {
        BlockPO blockPO;
        if (initConfig.isUseMySQL()) {
            blockPO = blockDao.queryByHeight(height);
        } else {
            blockPO = blockRocksDao.get(String.valueOf(height));
        }

        return blockPO == null ? null : BlockConvert.convertBlockPOToBlockHeader(blockPO);
    }

    /**
     * save to db
     *
     * @param block
     * @param txReceipts
     */
    public void saveBlock(Block block, List<TransactionReceipt> txReceipts) {
        if (log.isDebugEnabled()) {
            log.debug("[BlockRepository.saveBlock] is start");
        }

        BlockHeader blockHeader = block.getBlockHeader();
        //block height
        Long blockHeight = blockHeader.getHeight();
        BlockPO blockPO = new BlockPO();
        blockPO.setHeight(blockHeight);
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
        int txNum = txs.size();
        blockPO.setTxNum(txNum);
        //set total transaction num
        Long totalTxNum = blockHeader.getTotalTxNum();
        if (totalTxNum == null) {
            totalTxNum = 0L;
        }
        //total=lastNum + currentNum
        blockPO.setTotalTxNum(totalTxNum + txNum);
        //total block size use txs.length,unit:KB
        String blockData = JSON.toJSONString(txs);
        BigDecimal size = new BigDecimal(blockData.length());
        size = size.divide(new BigDecimal(1024), 2, BigDecimal.ROUND_HALF_DOWN);
        blockPO.setTotalBlockSize(size);
        //save block
        if (initConfig.isUseMySQL()) {
            try {
                blockDao.add(blockPO);
            } catch (DuplicateKeyException e) {
                log.error("[saveBlock] is idempotent blockHeight:{}", blockHeight);
                throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
            }
        } else {
            Profiler.enter("build tx POS");
            blockPO.setTxPOs(transactionRepository.buildTransactionPOs(blockHeight, blockTime, txs, txReceipts));
            Profiler.release();
            Profiler.enter("save block");
            blockRocksDao.save(blockPO);
            Profiler.release();
            Profiler.enter("save max block height");
            systemPropertyRepository.saveWithTransaction(Constant.MAX_BLOCK_HEIGHT, String.valueOf(blockHeight), "max block height");
            Profiler.release();
        }

        Profiler.enter("batch insert transaction");
        //save transactions
        transactionRepository.batchSaveTransaction(blockHeight, blockTime, txs, txReceipts);
        Profiler.release();


        if (log.isDebugEnabled()) {
            log.debug("[BlockRepository.saveBlock] is end");
        }
    }

    /**
     * query by condition、page
     *
     * @param height
     * @param blockHash
     * @param pageNum
     * @param pageSize
     * @return
     */
    public List<BlockVO> queryBlocksWithCondition(Long height, String blockHash, Integer pageNum, Integer pageSize) {
        if (null != blockHash) {
            blockHash = blockHash.trim();
        }
        List<BlockPO> list = blockDao.queryBlocksWithCondition(height, blockHash, (pageNum - 1) * pageSize, pageSize);
        return BeanConvertor.convertList(list, BlockVO.class);
    }

    @Deprecated public long countBlocksWithCondition(Long height, String blockHash) {
        return blockDao.countBlockWithCondition(height, blockHash);
    }

    /**
     * query block by height
     *
     * @param height
     * @return
     */
    public BlockVO queryBlockByHeight(Long height) {
        BlockPO blockPO = blockDao.queryByHeight(height);
        if (blockPO == null) {
            return null;
        }
        return BeanConvertor.convertBean(blockPO, BlockVO.class);
    }
}
