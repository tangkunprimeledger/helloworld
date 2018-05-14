package com.higgs.trust.rs.custom.biz.api.impl.identity;

import com.higgs.trust.IntegrateBaseTest;
import com.higgs.trust.rs.custom.api.identity.IdentityService;
import com.higgs.trust.rs.custom.model.RespData;
import com.higgs.trust.rs.custom.model.bo.identity.IdentityRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**  
 * @desc TODO  
 * @author WangQuanzhou
 * @date 2018/5/14 15:34
 */  
public class IdentityServiceImplTest extends IntegrateBaseTest {

    @Autowired
    IdentityService identityService;

    @Test public void testAcceptRequest() throws Exception {
        IdentityRequest identityRequest = new IdentityRequest();
        identityRequest.setReqNo("1236");
        identityRequest.setFlag("000");
        identityRequest.setKey("wang");
        identityRequest.setValue("daye");
        RespData resp = identityService.acceptRequest(identityRequest);
    }

    @Test public void testQueryIdentityByKey() throws Exception {
        System.out.println("根据key进行查询。。。");
        String key = "wang";
        RespData resp = identityService.queryIdentityByKey(key);
        System.out.println(resp);
    }

    @Test public void testQueryIdentityByReqNo() throws Exception {
        System.out.println("根据reqNo进行查询。。。");
        String reqNo = "wang";
        RespData resp = identityService.queryIdentityByReqNo(reqNo);
        System.out.println(resp);
    }

    @Test public void testAsyncSendIdentity() throws Exception {
    }

}