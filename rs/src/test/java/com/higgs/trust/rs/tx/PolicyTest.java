package com.higgs.trust.rs.tx;

import com.higgs.trust.rs.core.api.enums.CallbackTypeEnum;
import com.higgs.trust.rs.core.vo.manage.RegisterPolicyVO;
import com.higgs.trust.rs.core.vo.manage.RegisterRsVO;
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
    private static String REGISTER_POLICY_URL = "http://10.200.173.137:7070/v1/manage/policy/register";
    private static String REGISTER_RS_URL = "http://10.200.173.137:7070/v1/manage/rs/register";

    @Test public void test() {
        testRegistPolicy();
//        testRegistRS();
    }

    /**
     * 注册policy
     */
    private void testRegistPolicy() {
        RegisterPolicyVO registerPolicyVO = new RegisterPolicyVO();
        registerPolicyVO.setRequestId("req_no_policy_" + System.currentTimeMillis());

//        registerPolicyVO.setPolicyId("OPEN_ACCOUNT");
//        registerPolicyVO.setPolicyName("OPEN_ACCOUNT_NAME");
        registerPolicyVO.setPolicyId("TRANSFER_UTXO");
        registerPolicyVO.setPolicyName("TRANSFER_UTXO_NAME");

        registerPolicyVO.setRsIds(Lists.newArrayList("TRUST-NODED"));
        registerPolicyVO.setDecisionType(DecisionTypeEnum.FULL_VOTE.getCode());
        registerPolicyVO.setVotePattern(VotePatternEnum.SYNC.getCode());

//        registerPolicyVO.setCallbackType(CallbackTypeEnum.ALL.getCode());
        registerPolicyVO.setCallbackType(CallbackTypeEnum.SELF.getCode());

        CoreTxHelper.post(REGISTER_POLICY_URL, registerPolicyVO);
    }

    /**
     * 注册RS
     */
    private void testRegistRS() {
        RegisterRsVO vo = new RegisterRsVO();
        vo.setRequestId("req_no_rs_" + System.currentTimeMillis());
        vo.setRsId("TRUST-NODED");
        vo.setDesc("trust-nodea desc");
        CoreTxHelper.post(REGISTER_RS_URL, vo);
    }
}
