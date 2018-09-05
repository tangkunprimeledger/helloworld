package com.higgs.trust.slave.core.repository;

import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.IntegrateBaseTest;
import com.higgs.trust.slave.common.config.InitConfig;
import com.higgs.trust.slave.core.repository.config.SystemPropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author liuyu
 * @description
 * @date 2018-04-18
 */
public class BlockRepositoryTest  extends BaseTest {
    @Autowired
    private BlockRepository blockRepository;
    @Autowired
    private SystemPropertyRepository systemPropertyRepository;
    @Autowired
    private InitConfig initConfig;

    @Test public void testGetMaxHeight() throws Exception {
        if (!initConfig.isUseMySQL()) {
            systemPropertyRepository.add(Constant.MAX_BLOCK_HEIGHT, "100", "max block height");
        }
        Long height = blockRepository.getMaxHeight();
        Assert.assertEquals(height.longValue(), 100L);
    }

    @Test public void testGetLimitHeight() throws Exception {
        blockRepository.getLimitHeight(10);
    }

    @Test public void testGetBlock() throws Exception {
    }

    @Test public void testListBlocks() throws Exception {
    }

    @Test public void testListBlockHeaders() throws Exception {
    }

    @Test public void testGetBlockHeader() throws Exception {
    }

    @Test public void testSaveBlock() throws Exception {
        for (int i = 0; i < 100; i++) {

        }
    }

    @Test public void testQueryBlocksWithCondition() throws Exception {
    }

    @Test public void testCountBlocksWithCondition() throws Exception {
    }

    @Test public void testQueryBlockByHeight() throws Exception {
    }
}
