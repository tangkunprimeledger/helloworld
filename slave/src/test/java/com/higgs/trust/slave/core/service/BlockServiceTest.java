package com.higgs.trust.slave.core.service;

import com.higgs.trust.slave.IntegrateBaseTest;
import com.higgs.trust.slave.api.enums.account.FundDirectionEnum;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import com.higgs.trust.slave.core.service.action.account.TestDataMaker;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.enums.BlockHeaderTypeEnum;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liuyu
 * @description
 * @date 2018-04-17
 */
public class BlockServiceTest extends IntegrateBaseTest {

    @Autowired BlockService blockService;

    @Test public void getMaxHeight() {
        Long height = blockService.getMaxHeight();
        System.out.println("max.height:" + height);
    }

    @Test public void storeTempHeader() {
        BlockHeader header = new BlockHeader();
        BlockHeaderTypeEnum headerTypeEnum = BlockHeaderTypeEnum.TEMP_TYPE;
        blockService.storeTempHeader(header, headerTypeEnum);
    }

    @Test public void getTempHeader() {

    }

    @Test public void persistBlock() throws Exception {
        List<SignedTransaction> txs = new ArrayList<>();
        for (int k = 0; k < 2; k++) {
            List<Action> actions = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                Action action =
                    TestDataMaker.makeOpenAccountAction("tx-" + k + "-account_no_" + i, FundDirectionEnum.CREDIT);
                action.setIndex(i);
                actions.add(action);
            }
            CoreTransaction coreTransaction = TestDataMaker.makeCoreTx(actions, k, InitPolicyEnum.REGISTER);
            SignedTransaction tx = TestDataMaker.makeSignedTx(coreTransaction);
            txs.add(tx);
        }
        Block block = new Block();
        BlockHeader blockHeader = TestDataMaker.makeBlockHeader();
        block.setBlockHeader(blockHeader);
        block.setSignedTxList(txs);
        blockService.persistBlock(block);
    }

    @Test public void testQueryBlock() {
        Block block = blockService.queryBlock(1L);
        System.out.println(block);
        List<SignedTransaction> txs = block.getSignedTxList();
        for (SignedTransaction tx : txs) {
            CoreTransaction coreTransaction = tx.getCoreTx();
            List<Action> actions = coreTransaction.getActionList();
            for (Action action : actions) {
                System.out.println(action.getType());
            }
        }
    }

}
