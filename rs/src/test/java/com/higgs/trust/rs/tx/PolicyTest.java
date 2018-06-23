package com.higgs.trust.rs.tx;

import com.higgs.trust.rs.core.api.enums.CallbackTypeEnum;
import com.higgs.trust.rs.custom.api.vo.manage.RegisterPolicyVO;
import com.higgs.trust.slave.api.enums.manage.DecisionTypeEnum;
import com.higgs.trust.slave.api.enums.manage.VotePatternEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.testng.collections.Lists;

/**
 * @author liuyu
 * @description
 * @date 2018-06-23
 */
@Slf4j public class PolicyTest {
    private static String REGISTER_POLICY_URL = "http://127.0.0.1:7070/v1/manage/policy/register";

    @Test public void test() {
        testRegistPolicy();
    }

    /**
     * 注册policy
     */
    private void testRegistPolicy() {
        RegisterPolicyVO registerPolicyVO = new RegisterPolicyVO();
        registerPolicyVO.setRequestId("req_no_policy_" + System.currentTimeMillis());
        registerPolicyVO.setPolicyId("test_policy_id_001");
        registerPolicyVO.setPolicyName("test_policy_name_01");
        registerPolicyVO.setRsIds(Lists.newArrayList("TRUST-TEST1"));
        registerPolicyVO.setDecisionType(DecisionTypeEnum.FULL_VOTE.getCode());
        registerPolicyVO.setVotePattern(VotePatternEnum.SYNC.getCode());
        registerPolicyVO.setCallbackType(CallbackTypeEnum.ALL.getCode());

        CoreTxHelper.post(REGISTER_POLICY_URL, registerPolicyVO);
    }

}
