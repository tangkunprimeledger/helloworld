package com.higgs.trust.rs.core.repository;

import com.higgs.trust.rs.common.config.RsConfig;
import com.higgs.trust.rs.core.api.enums.CallbackTypeEnum;
import com.higgs.trust.rs.core.api.enums.VotePatternEnum;
import com.higgs.trust.rs.core.bo.VoteRule;
import com.higgs.trust.rs.core.dao.VoteRuleDao;
import com.higgs.trust.rs.core.dao.po.VoteRulePO;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * @author liuyu
 * @description
 * @date 2018-06-06
 */
@Slf4j @Repository public class VoteRuleRepository {
    @Autowired private RsConfig rsConfig;
    @Autowired private VoteRuleDao voteRuleDao;

    /**
     * create new vote-rule for policy
     *
     * @param voteRule
     */
    public void add(VoteRule voteRule) {
        if (!rsConfig.isUseMySQL()) {
            //TODO: liuyu for rocksdb handler
            return;
        }
        VoteRulePO voteRulePO = new VoteRulePO();
        voteRulePO.setPolicyId(voteRule.getPolicyId());
        voteRulePO.setVotePattern(voteRule.getVotePattern().getCode());
        voteRulePO.setCallbackType(voteRule.getCallbackType().getCode());
        voteRulePO.setCreateTime(new Date());
        try {
            voteRuleDao.add(voteRulePO);
        } catch (DuplicateKeyException e) {
            log.error("[add.vote-rule] is idempotent by policyId:{}", voteRule.getPolicyId());
            throw new SlaveException(SlaveErrorEnum.SLAVE_IDEMPOTENT);
        }
    }

    /**
     * query vote-rule by policy id
     *
     * @param policyId
     * @return
     */
    public VoteRule queryByPolicyId(String policyId) {
        if (!rsConfig.isUseMySQL()) {
            //TODO: liuyu for rocksdb handler
            return null;
        }
        VoteRulePO voteRulePO = voteRuleDao.queryByPolicyId(policyId);
        if (voteRulePO == null) {
            return null;
        }
        VoteRule voteRule = new VoteRule();
        voteRule.setPolicyId(policyId);
        voteRule.setVotePattern(VotePatternEnum.fromCode(voteRulePO.getVotePattern()));
        voteRule.setCallbackType(CallbackTypeEnum.fromCode(voteRulePO.getCallbackType()));
        return voteRule;
    }
}
