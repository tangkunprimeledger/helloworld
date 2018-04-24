package com.higgs.trust.slave.dao.block;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.dao.po.block.BlockPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.Date;

/*
 *
 * @desc
 * @author tangfashuang
 * @date 2018/4/8
 *
 */
public class BlockDaoTest extends BaseTest {

    @Autowired BlockDao blockDao;

    @Test public void queryByHeight() {
        BlockPO blockPO = blockDao.queryByHeight(1L);
        System.out.println(blockPO);
    }

    @Test public void addBlock() {
        BlockPO blockPO = new BlockPO();

        blockPO.setHeight(1L);
        blockPO.setVersion("1.0.12.2");
        blockPO.setAccountRootHash("account-root-hash");
        blockPO.setBlockTime(new Date());
        blockPO.setBlockHash("block-root-hash");
        blockPO.setContractRootHash("contract-root-hash");
        blockPO.setPolicyRootHash("policy-root-hash");
        blockPO.setPreviousHash("previous hash");
        blockPO.setRsRootHash("rs-root-hash");
        blockPO.setTxRootHash("tx-root-hash");
        blockPO.setTxReceiptRootHash("tx-receipt-root-hash");

        blockDao.add(blockPO);

    }
}