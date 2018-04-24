package com.higgs.trust.slave.dao.block;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.dao.po.block.BlockHeaderPO;
import com.higgs.trust.slave.model.bo.BlockHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/*
 *
 * @desc
 * @author tangfashuang
 * @date 2018/4/8
 *
 */
public class BlockHeaderDaoTest extends BaseTest {
    @Autowired BlockHeaderDao blockHeaderDao;

    @Test public void queryByHeight() {

    }

    @Test public void add() {
        BlockHeaderPO blockHeaderPO = new BlockHeaderPO();
        blockHeaderPO.setHeight(1L);
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setBlockHash("block-hash");
        blockHeader.setBlockTime(System.currentTimeMillis());
        blockHeader.setHeight(1L);
        blockHeader.setPreviousHash("previous-block-hash");
        blockHeader.setVersion("1.0.2.1");
        //        blockHeader.setStateRootHash();
        blockHeaderPO.setHeaderData(JSON.toJSONString(blockHeader));
        blockHeaderDao.add(blockHeaderPO);
    }
}