package com.higgs.trust.slave.core.service.action;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.core.service.transaction.TxCheckHandler;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/*
 *
 * @desc
 * @author tangfashuang
 * @date 2018/3/28
 *
 */
public class CheckTxHandlerTest extends BaseTest {

    private static final String priKey1 =
        "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBALhZZQEKaaZOsD4z+m1AoBlVnSZFD9mafaRAt9PVHtgkFJViV8a3s+2bFEkxVIxLSqdyWVCqZCOm1+6jbWmk4pN5yY4AaZBCPM6kHkzl/o5ctGXqkIf9s8WxFAmKpMEi7qP6SCitaoMy1sC+IS/vcOphzJZ3NbP3kG0FyCR5EvGdAgMBAAECgYAf7HMeRARZpWTF0NB8HOXcnUPSfcEp6KP7Tq3GxDBMM6tQ1y/mHKfO7L0Nk7pVdTBfYODwpCElP15DWA+5bLFDl2GzCY3gBoMrgrpIaaD672Qf12ikcVf6q/FSNJAvvDPSpLQKYJKG4Aa8/0mFZB9JU1KM2+wDl4Fgf/Lf+vM/zQJBAN1X5nvjy4nwTXlHWvtB+PG9ptAsGo9baEtGW2UrDhPYObghJpk4Slxo+r7l4fWNnwEP6kMGBlFMH41oxNezYLcCQQDVNqiBJBVUgIVGaLSb3ksyXerOf9w1g7JTpSoZmf6SOMAZQ3kqky9Ik4LGwnlFaolUEA/fcTev2rC8iBFt00RLAkB09I5H5jzVXRFCxQ5w9xIYghKTqso5952rMLj4QwDEQZt2DKY9jb3VCG990TBNNJDQ2dz5n0RVTrjZWoOwSgsPAkBvFcArMIKQeTl22pymzOV+w2HP3tv7YbcqT1Yk6o+w3TJwty/M18x90qUDK1WFriEIlCnA77rku1rzjy0NfFILAkBnbOFY4ECXdFysok3n5AgrdHBr8bRSRvzBpdjUL9eH0Cs/37aqwZynwCg6DX5/I2gIwX78ysnujB4mY6lFOG5f";

    private static final String priKey2 =
        "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKQwv8kaTmN15Z7c6gz3/7a8wmtuJqgn25uWAkBO5vTs6DpfB0Nf7N5jOH2pMhkgqkdiOlpNpTb+zoJZ+DNy28mHHbpb99GDEoa5zvcXxypU5yrNhmrch1bJbKZQiGoX/5NAia9t/Kltxdcs6EmWuOQB79fLhLDgwHeUDzYOdM13AgMBAAECgYAFWznGe78262+0QQy5o5WKBpppGszUC4jUiI5GPsy2DMx+qv73qbd2gdIj91MVEsW7Um8I5yOOqb1e70RzmTmmSgmIbc7L2ogkEVa/AWdnmFIqVV7EOokc7pExc0UMlIBXCiNynrQic0YtxV65JjaE/JAFomCCAUBbsP9TSs/ZMQJBANRq8rBvR1PCA9pwzqfwalKAAzpwsOs0tavP8XF80xm7XKNZrnOIIiLSj+ME630ECYJZ2XTKF1g/TblIHV8zAYMCQQDF4LQcqKuNbeUeu0Xf3VX0TXPImIdB3ZbbQyPuynhk5D0Fx72q29gRKUZifrm1Kog6fvrwN1IyuoZem3oijEX9AkApkKPckmnKofRPEjPd+NVVP2diUBrOa4oBDLeaFWrZZihCbpIMWV8UoU82hQfvdpLFxv8eM01OH1T+JHZa4ogxAkA2WEs/H7fV5NurQAWlwPUNXoQxEGr9VO1MlLj2qRa9ps13m+7kUPKba/mPrXw1XFQDtMIYXSkvE3k53HuDp4DFAkAxhxi9veGOKa24Fp+4MFSF3L9UdR6MROqIYVGgE0gHj7r+NIuCqk/l9acw9W4E5gAN03P3RAKpjmcqxOkZyj7h";

    private static final String priKey3 =
        "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANDzTWjIRJ6Y3dKT4Z08/QuUMjj3OFSgt8qD9ZFgT3TXik44olP7O0gVJiL+tBtCuqsW6nU2BWt2S/1/SmGVq1dxco1VSCU/Dk7ReBTMRyZBOxfzdMnaTWMbiO+ETodJl3eQbK1miJyVbg7hLe7s/8xiH7AGsKkppW6GC7Kpb4zJAgMBAAECgYBORbYLuGmsF4uQ5ICxjDUmbz9ZA5MAcKwomsIU0UUyecN/hcuZNhWA7Rs6JLuHMroGeTEe8zuYg9n3fgV5BL4H96z3SBSrY+BsCf1CxYGXEVCHzlt6g8575MqtxIlqPXnpKr9S1663EtsCCJ93t5rZmMA7z8bUbFRTcrUsajYzAQJBAPynP0a6Pk5JlF0TW5vbzusZb3CsEdPTp39NxlHEx9v/2xuREti1CSVMhdm8ZDdC5hDoETZn4DTiBAF0Z5it6pkCQQDTt9uSFv16v+62yJIz0KE9EUZrLua1BlfTIyvgBZQ6Lp5ORS2S9iVzfOS77mufysbfGSpmD6Oc5ElY2coUy8GxAkAlFB5zMM4IC0Bc0IR3QTECy77RGE+deMhyJGXghjKWlNwBFa9gYmEvOiXCqKVEfurovEYaZ/A9kpXn6L9zZsKxAkAuym+IdfRHcKu9Uc6eDPnVmT/K6G6si15Vl2xW8mS0ByGNgtRzqlrUj0GuFx9KDXKuU81/CO3L+tgK/vceaXnBAkEAk+OjzXA0KXZGKm+O8/Vl8yiJQpuvpuO4cxy4E7nEAjevFip88p4tO03DVxjyq2Az7457q/T+C/Ohr1X9uS/v/Q==";

    @Autowired TxCheckHandler checkTxHandler;

    @Test public void verifySignatures() throws Exception {

        Set<String> rsIdSet = new HashSet<>();
        rsIdSet.add("rs-test1");
        RegisterPolicy registerPolicyAction = new RegisterPolicy();
        registerPolicyAction.setPolicyId("syc-23ndnsc-3jndcs");
        registerPolicyAction.setPolicyName("test-policy");
        registerPolicyAction.setRsIdSet(rsIdSet);

        List actionList = new ArrayList<>();
        actionList.add(registerPolicyAction);

        SignedTransaction stx = new SignedTransaction();
        CoreTransaction ctx = new CoreTransaction();

        ctx.setPolicyId("policy-1hsdh6310-23hhs");
        ctx.setBizModel(null);
        ctx.setTxId("test-" + UUID.randomUUID().toString());
        ctx.setVersion(VersionEnum.V1.getCode());
        ctx.setLockTime(new Date());
        ctx.setActionList(actionList);

        stx.setCoreTx(ctx);

        String sign1 = SignUtils.sign(JSON.toJSONString(ctx), priKey1);
        String sign2 = SignUtils.sign(JSON.toJSONString(ctx), priKey2);
        String sign3 = SignUtils.sign(JSON.toJSONString(ctx), priKey3);
        List<String> signList = new ArrayList<>();
        signList.add(sign1);
        signList.add(sign2);
        signList.add(sign3);
        stx.setSignatureList(signList);

        //        checkTxHandler.verifySignatures(stx);
        Assert.assertEquals(true, checkTxHandler.verifySignatures(stx));
    }

    @Test
    public void checkActions() {
        Set<String> rsIdSet = new HashSet<>();
        rsIdSet.add("rs-test1");
        RegisterPolicy registerPolicyAction = new RegisterPolicy();
        registerPolicyAction.setPolicyId("syc-23ndnsc-3jndcs");
        registerPolicyAction.setPolicyName("test-policy");
        registerPolicyAction.setRsIdSet(rsIdSet);
        registerPolicyAction.setIndex(5);
        registerPolicyAction.setType(ActionTypeEnum.REGISTER_POLICY);

        RegisterRS registerRS = new RegisterRS();
        registerRS.setType(ActionTypeEnum.REGISTER_RS);
        registerRS.setIndex(0);
        registerRS.setPubKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCkML/JGk5jdeWe3OoM9/+2vMJrbiaoJ9ublgJATub07Og6XwdDX+zeYzh9qTIZIKpHYjpaTaU2/s6CWfgzctvJhx26W/fRgxKGuc73F8cqVOcqzYZq3IdWyWymUIhqF/+TQImvbfypbcXXLOhJlrjkAe/Xy4Sw4MB3lA82DnTNdwIDAQAB");
        registerRS.setDesc("rs-test3-desc");
        registerRS.setRsId("rs-test3");

        List actionList = new ArrayList<>();
        actionList.add(registerPolicyAction);
        actionList.add(registerRS);

        SignedTransaction stx = new SignedTransaction();
        CoreTransaction ctx = new CoreTransaction();

        ctx.setPolicyId("000000");
        ctx.setBizModel(null);
        ctx.setTxId("test-" + UUID.randomUUID().toString());
        ctx.setVersion(VersionEnum.V1.getCode());
        ctx.setLockTime(new Date());
        ctx.setActionList(actionList);

        boolean flag = checkTxHandler.checkActions(ctx);

        Assert.assertEquals(flag, false);


    }
}