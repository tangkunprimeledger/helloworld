package com.higgs.trust.slave.dao.block;

import com.higgs.trust.common.mybatis.BaseDao;
import com.higgs.trust.slave.dao.po.block.BlockPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper public interface BlockDao extends BaseDao<BlockPO> {
    /**
     * query block by block height
     *
     * @param height
     * @return
     */
    BlockPO queryByHeight(@Param("height") Long height);

    /**
     * query blocks by block height
     *
     * @param startHeight start height
     * @param size size of blocks
     * @return
     */
    List<BlockPO> queryBlocks(@Param("startHeight") Long startHeight, @Param("limit") int size);

    /**
     * get max height of block
     *
     * @return
     */
    Long getMaxHeight();

    /**
     * get max height of block
     *
     * @param limit
     * @return
     */
    List<Long> getLimitHeight(@Param("limit") int limit);

    /**
     * query blocks with condition
     * @param height
     * @param blockHash
     * @param start
     * @param end
     * @return
     */
    List<BlockPO> queryBlocksWithCondition(@Param("height") Long height, @Param("blockHash") String blockHash,
        @Param("start") int start, @Param("end") int end);

    /**
     * count blocks with condition
     * @param height
     * @param blockHash
     * @return
     */
    long countBlockWithCondition(@Param("height") Long height,
        @Param("blockHash") String blockHash);
}
