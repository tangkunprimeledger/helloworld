package com.higgs.trust.rs.core.repository;

import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.common.dao.RocksUtils;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.rs.core.api.enums.VoteResultEnum;
import com.higgs.trust.rs.core.bo.VoteRequestRecord;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class VoteReqRecordRepositoryTest extends IntegrateBaseTest{
    @Autowired
    private VoteReqRecordRepository voteReqRecordRepository;

    @Test public void testAdd() throws Exception {
        VoteRequestRecord voteRequestRecord = new VoteRequestRecord();
        voteRequestRecord.setTxId("test-tx-id-1");
        voteRequestRecord.setSender("TRUST-TEST0");
        voteRequestRecord.setTxData("test-data");
        voteRequestRecord.setId(1L);
        voteRequestRecord.setSign("test-signature");
        ThreadLocalUtils.putWriteBatch(new WriteBatch());
        voteReqRecordRepository.add(voteRequestRecord);
        RocksUtils.batchCommit(new WriteOptions(), ThreadLocalUtils.getWriteBatch());
        ThreadLocalUtils.clearWriteBatch();
    }

    @Test public void testQueryByTxId() throws Exception {
        VoteRequestRecord requestRecord = voteReqRecordRepository.queryByTxId("test-tx-id-1");
        System.out.println(requestRecord);
    }

    @Test public void testSetVoteResult() throws Exception {
        ThreadLocalUtils.putWriteBatch(new WriteBatch());
        voteReqRecordRepository.setVoteResult("test-tx-id-1", "new test signature", VoteResultEnum.AGREE);
        RocksUtils.batchCommit(new WriteOptions(), ThreadLocalUtils.getWriteBatch());
        ThreadLocalUtils.clearWriteBatch();

        VoteRequestRecord requestRecord = voteReqRecordRepository.queryByTxId("test-tx-id-1");
        System.out.println(requestRecord);
    }
}