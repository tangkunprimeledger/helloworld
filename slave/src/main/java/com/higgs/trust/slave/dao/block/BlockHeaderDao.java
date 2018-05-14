package com.higgs.trust.slave.dao.block;

import com.higgs.trust.slave.dao.BaseDao;
import com.higgs.trust.slave.dao.po.block.BlockHeaderPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper public interface BlockHeaderDao extends BaseDao<BlockHeaderPO> {
    /**
     * query block p2p by block height
     *
     * @param height
     * @param type
     * @return
     */
    BlockHeaderPO queryByHeight(@Param("height") Long height, @Param("type") String type);

    /**
     * delete the block header by height
     *
     * @param height
     * @param type
     */
    void deleteBlockHeader(@Param("height") Long height, @Param("type") String type);

}
