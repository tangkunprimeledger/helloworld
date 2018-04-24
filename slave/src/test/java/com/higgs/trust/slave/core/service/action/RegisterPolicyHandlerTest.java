package com.higgs.trust.slave.core.service.action;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.service.action.manage.RegisterPolicyHandler;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/*
 *
 * @desc
 * @author shenaingyan
 * @date 2018/4/2
 *
 */
public class RegisterPolicyHandlerTest extends BaseTest {

    @Autowired RegisterPolicyHandler registerPolicyHandler;

    @Autowired BlockService blockService;

    //policy已存在
    @Test public void testValidate1() throws Exception {

        Package pack = new Package();
        pack.setHeight(1L);
        Block block = blockService.buildDummyBlock(1L, new Date().getTime());
        PackContext packContext = new PackContext(pack, block);

        Set<String> rsIdSet = new HashSet<>();
        rsIdSet.add("test1");
        rsIdSet.add("test2");
        rsIdSet.add("test3");
        RegisterPolicy registerPolicyAction = new RegisterPolicy();
        registerPolicyAction.setIndex(1);
        registerPolicyAction.setType(ActionTypeEnum.REGISTER_POLICY);
        registerPolicyAction.setPolicyId("policy-1hsdh6310-23hhs");
        registerPolicyAction.setPolicyName("test-policy");
        registerPolicyAction.setRsIdSet(rsIdSet);

        packContext.setCurrentAction(registerPolicyAction);

        try {
            registerPolicyHandler.validate(packContext);
        } catch (SlaveException e) {
            Assert.assertEquals(e.getCode().toString(), "SLAVE_POLICY_EXISTS_ERROR");
        }
    }

    //policy参数为空
    @Test public void testValidate2() throws Exception {
        Package pack = new Package();
        pack.setHeight(1L);
        Block block = blockService.buildDummyBlock(1L, new Date().getTime());
        PackContext packContext = new PackContext(pack, block);

        RegisterPolicy registerPolicyAction = new RegisterPolicy();
        registerPolicyAction = null;

        packContext.setCurrentAction(registerPolicyAction);
        try {
            registerPolicyHandler.validate(packContext);
        } catch (SlaveException e) {
            Assert.assertEquals(e.getCode().toString(), "SLAVE_PARAM_VALIDATE_ERROR");
        }

    }

    //    policy参数校验不通过
    @Test public void testValidate3() throws Exception {
        Package pack = new Package();
        pack.setHeight(1L);
        Block block = blockService.buildDummyBlock(1L, new Date().getTime());
        PackContext packContext = new PackContext(pack, block);

        Set<String> rsIdSet = new HashSet<>();
        rsIdSet.add("test1");
        rsIdSet.add("test2");
        rsIdSet.add("test3");
        RegisterPolicy registerPolicyAction = new RegisterPolicy();
        registerPolicyAction.setIndex(1);
        registerPolicyAction.setType(ActionTypeEnum.REGISTER_POLICY);
        registerPolicyAction.setPolicyName("test-policy");
        registerPolicyAction.setRsIdSet(rsIdSet);

        packContext.setCurrentAction(registerPolicyAction);
        try {
            registerPolicyHandler.validate(packContext);
        } catch (SlaveException e) {
            Assert.assertEquals(e.getCode().toString(), "SLAVE_PARAM_VALIDATE_ERROR");
        }
    }

    //rsIdList重复
    @Test public void testValidate4() throws Exception {
        Package pack = new Package();
        pack.setHeight(1L);
        Block block = blockService.buildDummyBlock(1L, new Date().getTime());
        PackContext packContext = new PackContext(pack, block);

        Set<String> rsIdSet = new HashSet<>();
        rsIdSet.add("test1");
        rsIdSet.add("test2");
        rsIdSet.add("test2");
        RegisterPolicy registerPolicyAction = new RegisterPolicy();
        registerPolicyAction.setIndex(1);
        registerPolicyAction.setType(ActionTypeEnum.REGISTER_POLICY);
        registerPolicyAction.setPolicyId("policy-1hsdh6310-23hhs");
        registerPolicyAction.setPolicyName("test-policy");
        registerPolicyAction.setRsIdSet(rsIdSet);

        packContext.setCurrentAction(registerPolicyAction);

        try {
            registerPolicyHandler.validate(packContext);
        } catch (SlaveException e) {
            Assert.assertEquals(e.getCode().toString(), "SLAVE_PARAM_VALIDATE_ERROR");
        }
    }

    //校验通过
    @Test public void testValidate5() throws Exception {
        Package pack = new Package();
        pack.setHeight(1L);
        Block block = blockService.buildDummyBlock(1L, new Date().getTime());
        PackContext packContext = new PackContext(pack, block);

        Set<String> rsIdSet = new HashSet<>();
        rsIdSet.add("test1");
        rsIdSet.add("test2");
        rsIdSet.add("test3");
        RegisterPolicy registerPolicyAction = new RegisterPolicy();
        registerPolicyAction.setIndex(1);
        registerPolicyAction.setType(ActionTypeEnum.REGISTER_POLICY);
        Random random = new Random();
        String pid = String.valueOf(random.nextInt());
        registerPolicyAction.setPolicyId(pid);
        registerPolicyAction.setPolicyName("test-policy");
        registerPolicyAction.setRsIdSet(rsIdSet);

        packContext.setCurrentAction(registerPolicyAction);

        registerPolicyHandler.validate(packContext);
    }

    //成功增加policy
    @Test public void testPersist() throws Exception {
        Package pack = new Package();
        pack.setHeight(1L);
        Block block = blockService.buildDummyBlock(1L, new Date().getTime());
        PackContext packContext = new PackContext(pack, block);

        Set<String> rsIdSet = new HashSet<>();
        rsIdSet.add("test1");
        rsIdSet.add("test2");
        rsIdSet.add("test3");
        RegisterPolicy registerPolicyAction = new RegisterPolicy();
        registerPolicyAction.setIndex(1);
        registerPolicyAction.setType(ActionTypeEnum.REGISTER_POLICY);
        Random random = new Random();
        String pid = String.valueOf(random.nextInt());
        registerPolicyAction.setPolicyId(pid);
        registerPolicyAction.setPolicyName("test-policy");
        registerPolicyAction.setRsIdSet(rsIdSet);

        packContext.setCurrentAction(registerPolicyAction);

        registerPolicyHandler.persist(packContext);
    }

}