package com.higgs.trust.slave.core.repository;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.crypto.rsa.Rsa;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import com.higgs.trust.slave.model.enums.biz.PendingTxStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/*
 *
 * @desc
 * @author tangfashuang
 * @date 2018/4/14
 *
 */
public class PendingTxRepositoryTest extends BaseTest {

    private static final String priKey1 =
        "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBALhZZQEKaaZOsD4z+m1AoBlVnSZFD9mafaRAt9PVHtgkFJViV8a3s+2bFEkxVIxLSqdyWVCqZCOm1+6jbWmk4pN5yY4AaZBCPM6kHkzl/o5ctGXqkIf9s8WxFAmKpMEi7qP6SCitaoMy1sC+IS/vcOphzJZ3NbP3kG0FyCR5EvGdAgMBAAECgYAf7HMeRARZpWTF0NB8HOXcnUPSfcEp6KP7Tq3GxDBMM6tQ1y/mHKfO7L0Nk7pVdTBfYODwpCElP15DWA+5bLFDl2GzCY3gBoMrgrpIaaD672Qf12ikcVf6q/FSNJAvvDPSpLQKYJKG4Aa8/0mFZB9JU1KM2+wDl4Fgf/Lf+vM/zQJBAN1X5nvjy4nwTXlHWvtB+PG9ptAsGo9baEtGW2UrDhPYObghJpk4Slxo+r7l4fWNnwEP6kMGBlFMH41oxNezYLcCQQDVNqiBJBVUgIVGaLSb3ksyXerOf9w1g7JTpSoZmf6SOMAZQ3kqky9Ik4LGwnlFaolUEA/fcTev2rC8iBFt00RLAkB09I5H5jzVXRFCxQ5w9xIYghKTqso5952rMLj4QwDEQZt2DKY9jb3VCG990TBNNJDQ2dz5n0RVTrjZWoOwSgsPAkBvFcArMIKQeTl22pymzOV+w2HP3tv7YbcqT1Yk6o+w3TJwty/M18x90qUDK1WFriEIlCnA77rku1rzjy0NfFILAkBnbOFY4ECXdFysok3n5AgrdHBr8bRSRvzBpdjUL9eH0Cs/37aqwZynwCg6DX5/I2gIwX78ysnujB4mY6lFOG5f";

    private static final String priKey2 =
        "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKQwv8kaTmN15Z7c6gz3/7a8wmtuJqgn25uWAkBO5vTs6DpfB0Nf7N5jOH2pMhkgqkdiOlpNpTb+zoJZ+DNy28mHHbpb99GDEoa5zvcXxypU5yrNhmrch1bJbKZQiGoX/5NAia9t/Kltxdcs6EmWuOQB79fLhLDgwHeUDzYOdM13AgMBAAECgYAFWznGe78262+0QQy5o5WKBpppGszUC4jUiI5GPsy2DMx+qv73qbd2gdIj91MVEsW7Um8I5yOOqb1e70RzmTmmSgmIbc7L2ogkEVa/AWdnmFIqVV7EOokc7pExc0UMlIBXCiNynrQic0YtxV65JjaE/JAFomCCAUBbsP9TSs/ZMQJBANRq8rBvR1PCA9pwzqfwalKAAzpwsOs0tavP8XF80xm7XKNZrnOIIiLSj+ME630ECYJZ2XTKF1g/TblIHV8zAYMCQQDF4LQcqKuNbeUeu0Xf3VX0TXPImIdB3ZbbQyPuynhk5D0Fx72q29gRKUZifrm1Kog6fvrwN1IyuoZem3oijEX9AkApkKPckmnKofRPEjPd+NVVP2diUBrOa4oBDLeaFWrZZihCbpIMWV8UoU82hQfvdpLFxv8eM01OH1T+JHZa4ogxAkA2WEs/H7fV5NurQAWlwPUNXoQxEGr9VO1MlLj2qRa9ps13m+7kUPKba/mPrXw1XFQDtMIYXSkvE3k53HuDp4DFAkAxhxi9veGOKa24Fp+4MFSF3L9UdR6MROqIYVGgE0gHj7r+NIuCqk/l9acw9W4E5gAN03P3RAKpjmcqxOkZyj7h";

    private static final String priKey3 =
        "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANDzTWjIRJ6Y3dKT4Z08/QuUMjj3OFSgt8qD9ZFgT3TXik44olP7O0gVJiL+tBtCuqsW6nU2BWt2S/1/SmGVq1dxco1VSCU/Dk7ReBTMRyZBOxfzdMnaTWMbiO+ETodJl3eQbK1miJyVbg7hLe7s/8xiH7AGsKkppW6GC7Kpb4zJAgMBAAECgYBORbYLuGmsF4uQ5ICxjDUmbz9ZA5MAcKwomsIU0UUyecN/hcuZNhWA7Rs6JLuHMroGeTEe8zuYg9n3fgV5BL4H96z3SBSrY+BsCf1CxYGXEVCHzlt6g8575MqtxIlqPXnpKr9S1663EtsCCJ93t5rZmMA7z8bUbFRTcrUsajYzAQJBAPynP0a6Pk5JlF0TW5vbzusZb3CsEdPTp39NxlHEx9v/2xuREti1CSVMhdm8ZDdC5hDoETZn4DTiBAF0Z5it6pkCQQDTt9uSFv16v+62yJIz0KE9EUZrLua1BlfTIyvgBZQ6Lp5ORS2S9iVzfOS77mufysbfGSpmD6Oc5ElY2coUy8GxAkAlFB5zMM4IC0Bc0IR3QTECy77RGE+deMhyJGXghjKWlNwBFa9gYmEvOiXCqKVEfurovEYaZ/A9kpXn6L9zZsKxAkAuym+IdfRHcKu9Uc6eDPnVmT/K6G6si15Vl2xW8mS0ByGNgtRzqlrUj0GuFx9KDXKuU81/CO3L+tgK/vceaXnBAkEAk+OjzXA0KXZGKm+O8/Vl8yiJQpuvpuO4cxy4E7nEAjevFip88p4tO03DVxjyq2Az7457q/T+C/Ohr1X9uS/v/Q==";
    @Autowired private PendingTxRepository pendingTxRepository;

    private SignedTransaction signedTx;

    private List<SignedTransaction> signedTransactions = new ArrayList<>();

    @BeforeMethod public void setUp() throws Exception {
        signedTx = initTx();

        SignedTransaction signedTx1 = initTx();
        signedTx1.getCoreTx().setTxId("pending-tx-test-1");

        SignedTransaction signedTx2 = initTx();
        signedTx2.getCoreTx().setTxId("pending-tx-test-2");

        signedTransactions.add(signedTx1);
        signedTransactions.add(signedTx2);
    }

    private SignedTransaction initTx() throws Exception {
        SignedTransaction signedTransaction = new SignedTransaction();

        RegisterPolicy registerPolicy = new RegisterPolicy();
        registerPolicy.setPolicyId("test-policy-1");
        registerPolicy.setPolicyName("测试注册policy-1");
        registerPolicy.setType(ActionTypeEnum.REGISTER_POLICY);
        registerPolicy.setIndex(0);

        List<String> rsIds = new ArrayList<>();
        rsIds.add("rs-test1");
        registerPolicy.setRsIds(rsIds);

        CoreTransaction coreTx1 = new CoreTransaction();
        List<Action> registerPolicyList = new ArrayList<>();
        registerPolicyList.add(registerPolicy);

        coreTx1.setTxId("pending-tx-test-1");
        coreTx1.setActionList(registerPolicyList);
        coreTx1.setBizModel(null);
        coreTx1.setVersion("2.0.0");
        coreTx1.setPolicyId("000000");

        String sign1 = Rsa.sign(JSON.toJSONString(coreTx1), priKey3);
        //        String sign2 = Rsa.sign(JSON.toJSONString(coreTx1), priKey2);
        List<String> signList = new ArrayList<>();
        signList.add(sign1);
        //        signList.add(sign2);
        signedTransaction.setCoreTx(coreTx1);
        //        signedTransaction.setSignatureList(signList);

        return signedTransaction;
    }

    @Test public void isExist() {
        Assert.assertEquals(true, pendingTxRepository.isExist("pending-tx-test-4"));
    }

    @Test public void batchUpdateStatus() {
        pendingTxRepository
            .batchUpdateStatus(signedTransactions, PendingTxStatusEnum.INIT, PendingTxStatusEnum.PACKAGED, 3L);

    }

    @Test public void saveWithStatus() {
        pendingTxRepository.saveWithStatus(signedTx, PendingTxStatusEnum.INIT, null);
    }

    @Test public void getTransactionsByStatus() {
        List<SignedTransaction> signedTransactionList =
            pendingTxRepository.getTransactionsByStatus(PendingTxStatusEnum.PACKAGED.getCode(), 4);

        Assert.assertEquals(4, signedTransactionList.size());

        //        Assert.assertEquals("pending-tx-test-3", signedTransactionList.get(0).getCoreTx().getTxId());
        //        Assert.assertEquals("pending-tx-test-4", signedTransactionList.get(1).getCoreTx().getTxId());
    }

    @Test public void getTransactionsByHeight() {
        List<SignedTransaction> signedTransactionList = pendingTxRepository.getTransactionsByHeight(2L);
        Assert.assertEquals(3, signedTransactionList.size());

        Assert.assertEquals("pending-tx-test-0", signedTransactionList.get(0).getCoreTx().getTxId());
        Assert.assertEquals("pending-tx-test-1", signedTransactionList.get(1).getCoreTx().getTxId());
        Assert.assertEquals("pending-tx-test-2", signedTransactionList.get(2).getCoreTx().getTxId());
    }
}