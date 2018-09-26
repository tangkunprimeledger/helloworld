package com.higgs.trust.rs.core.repository;

import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.common.dao.RocksUtils;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.rs.core.api.enums.CallbackTypeEnum;
import com.higgs.trust.rs.core.bo.VoteRule;
import com.higgs.trust.rs.core.dao.po.VoteRulePO;
import com.higgs.trust.rs.core.dao.rocks.VoteRuleRocksDao;
import com.higgs.trust.slave.api.enums.manage.VotePatternEnum;
import org.rocksdb.Transaction;
import org.rocksdb.WriteOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class VoteRuleRepositoryTest extends IntegrateBaseTest{
    @Autowired
    private VoteRuleRepository voteRuleRepository;

    @Autowired
    private VoteRuleRocksDao voteRuleRocksDao;

    @Test public void testAdd() throws Exception {
        VoteRule voteRule = new VoteRule();
        voteRule.setPolicyId("test-policy-id");
        voteRule.setCallbackType(CallbackTypeEnum.ALL);
        voteRule.setVotePattern(VotePatternEnum.SYNC);

        Transaction tx = RocksUtils.beginTransaction(new WriteOptions());
        ThreadLocalUtils.putRocksTx(tx);
        voteRuleRepository.add(voteRule);
        RocksUtils.txCommit(tx);
        ThreadLocalUtils.clearRocksTx();
    }

    @Test public void testQueryByPolicyId() throws Exception {
        VoteRule voteRule = voteRuleRepository.queryByPolicyId("test-policy-id");
        System.out.println(voteRule);
    }

    @Test public void testBatchInsert() throws Exception {
        List<VoteRule> voteRuleList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            VoteRule voteRule = new VoteRule();
            voteRule.setPolicyId("test-policy-id" + i);
            voteRule.setCallbackType(CallbackTypeEnum.ALL);
            voteRule.setVotePattern(VotePatternEnum.SYNC);

            voteRuleList.add(voteRule);
        }

        Transaction tx = RocksUtils.beginTransaction(new WriteOptions());
        ThreadLocalUtils.putRocksTx(tx);
        voteRuleRepository.batchInsert(voteRuleList);
        RocksUtils.txCommit(tx);
        ThreadLocalUtils.clearRocksTx();

        List<VoteRulePO> voteRules = voteRuleRocksDao.queryByPrefix("test-policy-id");
        System.out.println(voteRules);
    }
}