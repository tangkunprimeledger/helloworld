package com.higgs.trust.slave.core.service.action.manage;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.core.service.block.BlockService;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.context.PackContext;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.Date;

public class RegisterRsHandlerTest extends BaseTest {
    @Autowired BlockService blockService;

    @Autowired RegisterRsHandler registerRsHandler;

    @Test public void testProcess() throws Exception {
        Package pack = new Package();
        pack.setHeight(2L);
        Block block = blockService.buildDummyBlock(2L, new Date().getTime());
        PackContext packContext = new PackContext(pack, block);

        RegisterRS registerRS = new RegisterRS();
        registerRS.setRsId("test-register-rs1");
        registerRS.setDesc("测试register");
        registerRS.setIndex(0);
        registerRS.setType(ActionTypeEnum.REGISTER_RS);

        packContext.setCurrentAction(registerRS);



        registerRsHandler.process(packContext);
    }
}