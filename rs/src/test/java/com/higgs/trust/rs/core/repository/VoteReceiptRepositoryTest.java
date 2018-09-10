package com.higgs.trust.rs.core.repository;

import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.common.dao.RocksUtils;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.rs.core.api.enums.VoteResultEnum;
import com.higgs.trust.rs.core.bo.VoteReceipt;
import com.higgs.trust.rs.core.dao.po.VoteReceiptPO;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

public class VoteReceiptRepositoryTest extends IntegrateBaseTest {

    @Autowired
    private VoteReceiptRepository voteReceiptRepository;
    @Test public void testAdd() throws Exception {
        VoteReceipt voteReceipt = new VoteReceipt();
        voteReceipt.setTxId("test-tx-id-1");
        voteReceipt.setVoter("TRUST-TEST2");
        voteReceipt.setVoteResult(VoteResultEnum.AGREE);
        voteReceipt.setSign("test-signature");
        voteReceiptRepository.add(voteReceipt);
    }

    @Test public void testBatchAdd() throws Exception {
        List<VoteReceipt> voteReceipts = new ArrayList<>();
        for (int i = 2; i < 30; i++) {
            VoteReceipt voteReceipt = new VoteReceipt();
            voteReceipt.setTxId("test-tx-id-" + i);
            voteReceipt.setVoter("TRUST-TEST" + i);
            if (i % 3 == 0) {
                voteReceipt.setVoteResult(VoteResultEnum.AGREE);
                voteReceipt.setSign("test-signature");
            } else {
                voteReceipt.setVoteResult(VoteResultEnum.DISAGREE);
            }

            VoteReceipt voteReceipt1 = new VoteReceipt();
            voteReceipt1.setTxId("test-tx-id-" + i);
            voteReceipt1.setVoter("TRUST-TEST" + (i + 50));
            if (i % 3 == 0) {
                voteReceipt1.setVoteResult(VoteResultEnum.AGREE);
                voteReceipt1.setSign("test-signature");
            } else {
                voteReceipt1.setVoteResult(VoteResultEnum.DISAGREE);
            }
            voteReceipts.add(voteReceipt);
            voteReceipts.add(voteReceipt1);
        }
        ThreadLocalUtils.putWriteBatch(new WriteBatch());
        voteReceiptRepository.batchAdd(voteReceipts);
        RocksUtils.batchCommit(new WriteOptions(), ThreadLocalUtils.getWriteBatch());
        ThreadLocalUtils.clearWriteBatch();
    }

    @Test public void testQueryByTxId() throws Exception {
        List<VoteReceipt> list1 = voteReceiptRepository.queryByTxId("test-tx-id-15");
        System.out.println(list1);

        List<VoteReceipt> list2 = voteReceiptRepository.queryByTxId("test-tx-id-17");
        System.out.println(list2);
    }

    @Test public void testQueryForVoter() throws Exception {
        VoteReceipt receipt1 = voteReceiptRepository.queryForVoter("test-tx-id-13", "TRUST-TEST13");
        System.out.println(receipt1);
        VoteReceipt receipt2 = voteReceiptRepository.queryForVoter("test-tx-id-13", "TRUST-TEST63");
        System.out.println(receipt2);

        VoteReceipt receipt3 = voteReceiptRepository.queryForVoter("test-tx-id-13", "TRUST-TEST53");
        System.out.println(receipt3);
    }
}