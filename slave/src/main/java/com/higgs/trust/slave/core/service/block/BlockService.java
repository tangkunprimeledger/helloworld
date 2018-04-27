package com.higgs.trust.slave.core.service.block;

import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.TransactionReceipt;
import com.higgs.trust.slave.model.bo.context.PackageData;
import com.higgs.trust.slave.model.enums.BlockHeaderTypeEnum;

import java.util.List;

/**
 * @Description:
 * @author: pengdi
 **/
public interface BlockService {
    /**
     * get the height from most recently persisted block in the final block chain
     *
     * @return
     */
    Long getMaxHeight();

    /**
     * build block p2p
     *
     * @param processTypeEnum
     * @param packageData
     * @param txReceipts
     * @return
     */
    BlockHeader buildHeader(TxProcessTypeEnum processTypeEnum, PackageData packageData,
        List<TransactionReceipt> txReceipts);

    /**
     * store block p2p for package validate
     *
     * @param header
     */
    void storeTempHeader(BlockHeader header, BlockHeaderTypeEnum headerTypeEnum);

    /**
     * get block p2p with block height from temporary storage in database
     * most used by package validate and persist
     *
     * @param height
     * @return
     */
    BlockHeader getTempHeader(Long height, BlockHeaderTypeEnum headerTypeEnum);

    /**
     * get final persisted block header
     *
     * @param height
     * @return
     */
    BlockHeader getHeader(Long height);

    /**
     * build block
     *
     * @param packageData
     * @param blockHeader
     * @return
     */
    Block buildBlock(PackageData packageData, BlockHeader blockHeader);

    /**
     * build dummy block
     *
     * @param height
     * @param blockTime
     * @return
     */
    Block buildDummyBlock(Long height, Long blockTime);

    /**
     * persist block for final result
     *
     * @param block
     * @param txReceipts
     */
    void persistBlock(Block block, List<TransactionReceipt> txReceipts);

    /**
     * compare the two header datas
     *
     * @param header1
     * @param header2
     * @return
     */
    boolean compareBlockHeader(BlockHeader header1, BlockHeader header2);

    /**
     * build hash for block header
     *
     * @param blockHeader
     * @return
     */
    String buildBlockHash(BlockHeader blockHeader);

    /**
     * query block from db
     * @param blockHeight
     * @return
     */
    Block queryBlock(Long blockHeight);
}
