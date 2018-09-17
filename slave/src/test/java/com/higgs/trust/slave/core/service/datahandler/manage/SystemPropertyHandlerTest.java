package com.higgs.trust.slave.core.service.datahandler.manage;

import com.higgs.trust.slave.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class SystemPropertyHandlerTest extends BaseTest {
    @Autowired
    private SystemPropertyHandler systemPropertyHandler;

    /*@Test
    public void testAdd() throws Exception {
        System.out.println("add desc null          "+systemPropertyHandler.add("ling","chao", null));
      //  System.out.println("add normal           "+systemPropertyHandler.add("lingling","chaochao", "desc"));
      //  System.out.println("add the same       "+systemPropertyHandler.add("lingling","chaochao", "desc"));
    }

    @Test
    public void testUpdate() throws Exception {
        System.out.println("no update        "+systemPropertyHandler.update("ling","chao"));
        System.out.println("normal update    "+systemPropertyHandler.update("ling","chao0"));
        System.out.println("no data update  "+systemPropertyHandler.update("linglingling","chao0"));
    }
*/
}