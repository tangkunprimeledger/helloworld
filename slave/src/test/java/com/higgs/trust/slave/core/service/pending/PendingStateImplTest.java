package com.higgs.trust.slave.core.service.pending;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.vo.TransactionVO;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import com.higgs.trust.slave.model.enums.biz.PendingTxStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
 *
 * @desc
 * @author tangfashuang
 * @date 2018/4/14
 *
 */
public class PendingStateImplTest extends BaseTest {

    @Autowired
    private PendingState pendingState;



    private static final List<SignedTransaction> signedTxList = new ArrayList<>();

    private static final String priKey1 =
        "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBALhZZQEKaaZOsD4z+m1AoBlVnSZFD9mafaRAt9PVHtgkFJViV8a3s+2bFEkxVIxLSqdyWVCqZCOm1+6jbWmk4pN5yY4AaZBCPM6kHkzl/o5ctGXqkIf9s8WxFAmKpMEi7qP6SCitaoMy1sC+IS/vcOphzJZ3NbP3kG0FyCR5EvGdAgMBAAECgYAf7HMeRARZpWTF0NB8HOXcnUPSfcEp6KP7Tq3GxDBMM6tQ1y/mHKfO7L0Nk7pVdTBfYODwpCElP15DWA+5bLFDl2GzCY3gBoMrgrpIaaD672Qf12ikcVf6q/FSNJAvvDPSpLQKYJKG4Aa8/0mFZB9JU1KM2+wDl4Fgf/Lf+vM/zQJBAN1X5nvjy4nwTXlHWvtB+PG9ptAsGo9baEtGW2UrDhPYObghJpk4Slxo+r7l4fWNnwEP6kMGBlFMH41oxNezYLcCQQDVNqiBJBVUgIVGaLSb3ksyXerOf9w1g7JTpSoZmf6SOMAZQ3kqky9Ik4LGwnlFaolUEA/fcTev2rC8iBFt00RLAkB09I5H5jzVXRFCxQ5w9xIYghKTqso5952rMLj4QwDEQZt2DKY9jb3VCG990TBNNJDQ2dz5n0RVTrjZWoOwSgsPAkBvFcArMIKQeTl22pymzOV+w2HP3tv7YbcqT1Yk6o+w3TJwty/M18x90qUDK1WFriEIlCnA77rku1rzjy0NfFILAkBnbOFY4ECXdFysok3n5AgrdHBr8bRSRvzBpdjUL9eH0Cs/37aqwZynwCg6DX5/I2gIwX78ysnujB4mY6lFOG5f";

    private static final String priKey2 =
        "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKQwv8kaTmN15Z7c6gz3/7a8wmtuJqgn25uWAkBO5vTs6DpfB0Nf7N5jOH2pMhkgqkdiOlpNpTb+zoJZ+DNy28mHHbpb99GDEoa5zvcXxypU5yrNhmrch1bJbKZQiGoX/5NAia9t/Kltxdcs6EmWuOQB79fLhLDgwHeUDzYOdM13AgMBAAECgYAFWznGe78262+0QQy5o5WKBpppGszUC4jUiI5GPsy2DMx+qv73qbd2gdIj91MVEsW7Um8I5yOOqb1e70RzmTmmSgmIbc7L2ogkEVa/AWdnmFIqVV7EOokc7pExc0UMlIBXCiNynrQic0YtxV65JjaE/JAFomCCAUBbsP9TSs/ZMQJBANRq8rBvR1PCA9pwzqfwalKAAzpwsOs0tavP8XF80xm7XKNZrnOIIiLSj+ME630ECYJZ2XTKF1g/TblIHV8zAYMCQQDF4LQcqKuNbeUeu0Xf3VX0TXPImIdB3ZbbQyPuynhk5D0Fx72q29gRKUZifrm1Kog6fvrwN1IyuoZem3oijEX9AkApkKPckmnKofRPEjPd+NVVP2diUBrOa4oBDLeaFWrZZihCbpIMWV8UoU82hQfvdpLFxv8eM01OH1T+JHZa4ogxAkA2WEs/H7fV5NurQAWlwPUNXoQxEGr9VO1MlLj2qRa9ps13m+7kUPKba/mPrXw1XFQDtMIYXSkvE3k53HuDp4DFAkAxhxi9veGOKa24Fp+4MFSF3L9UdR6MROqIYVGgE0gHj7r+NIuCqk/l9acw9W4E5gAN03P3RAKpjmcqxOkZyj7h";

    @BeforeMethod
    public void setUp() throws Exception {
        SignedTransaction signedTx1 = new SignedTransaction();

        RegisterRS registerRS1 = new RegisterRS();
        registerRS1.setRsId("rs-test4");
        registerRS1.setDesc("rs-test4-desc");
        registerRS1.setType(ActionTypeEnum.REGISTER_RS);
        registerRS1.setIndex(1);
     //   registerRS1.setPubKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCxk7MQV0kyb4IIWez5l4PrxAPHujW+gQR3J3vbfnAb/Dy9BSoM0y27zn9oTFtn8p1KBppc2xQtH4ViQ5XpoFFA8sCmuZM6LXFn9Gd1LHUbQUj/AbLWyru3gD+PHwQ+Cd0y/PO4gBD4qD6WKKIHf7Nxl+dBnTNE1jNGflMVvdY1FQIDAQAB");

        CoreTransaction coreTx1 = new CoreTransaction();
        List<Action> registerRsList1 = new ArrayList<>();
        registerRsList1.add(registerRS1);

        coreTx1.setTxId("pending-tx-test-7");
        coreTx1.setActionList(registerRsList1);
        coreTx1.setLockTime(new Date());
        coreTx1.setBizModel(new JSONObject());
        coreTx1.setSender("rs-id-2");
        coreTx1.setVersion("1.1.0");
        coreTx1.setPolicyId("000000");

        String sign1 = SignUtils.sign(JSON.toJSONString(coreTx1), priKey1);
        String sign2 = SignUtils.sign(JSON.toJSONString(coreTx1), priKey2);
        List<String> signList1 = new ArrayList<>();
        signList1.add(sign1);
        signList1.add(sign2);
        signedTx1.setCoreTx(coreTx1);
//        signedTx1.setSignatureList(signList1);



        SignedTransaction signedTx2 = new SignedTransaction();

        RegisterRS registerRS2 = new RegisterRS();
        registerRS2.setRsId("rs-test5");
        registerRS2.setDesc("rs-test5-desc");
        registerRS2.setIndex(1);
        registerRS2.setType(ActionTypeEnum.REGISTER_RS);
  //      registerRS2.setPubKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCxk7MQV0kyb4IIWez5l4PrxAPHujW+gQR3J3vbfnAb/Dy9BSoM0y27zn9oTFtn8p1KBppc2xQtH4ViQ5XpoFFA8sCmuZM6LXFn9Gd1LHUbQUj/AbLWyru3gD+PHwQ+Cd0y/PO4gBD4qD6WKKIHf7Nxl+dBnTNE1jNGflMVvdY1FQIDAQAB");

        CoreTransaction coreTx2 = new CoreTransaction();
        List<Action> registerRsList2 = new ArrayList<>();
        registerRsList2.add(registerRS2);

        coreTx2.setTxId("pending-tx-test-6");
        coreTx2.setActionList(registerRsList2);
        coreTx2.setLockTime(new Date());
        coreTx2.setBizModel(new JSONObject());
        coreTx2.setSender("rs-id-3");
        coreTx2.setVersion("1.2.0");
        coreTx2.setPolicyId("000000");

        String sign3 = SignUtils.sign(JSON.toJSONString(coreTx2), priKey1);
        String sign4 = SignUtils.sign(JSON.toJSONString(coreTx2), priKey2);
        List<String> signList2 = new ArrayList<>();
        signList2.add(sign3);
        signList2.add(sign4);
        signedTx2.setCoreTx(coreTx2);
//        signedTx2.setSignatureList(signList2);

        signedTxList.add(signedTx1);
        signedTxList.add(signedTx2);
    }

    @Test public void addPendingTransactions() {
        List<TransactionVO> voList = pendingState.addPendingTransactions(signedTxList);
        Assert.assertEquals(1, voList.size());
    }

    @Test public void getPendingTransactions() {
        List<SignedTransaction> signedTransactions = pendingState.getPendingTransactions(2);
        Assert.assertEquals(2, signedTransactions.size());
        Assert.assertEquals("pending-tx-test-5", signedTransactions.get(0).getCoreTx().getTxId());
        Assert.assertEquals("pending-tx-test-6", signedTransactions.get(1).getCoreTx().getTxId());
    }

    @Test public void packagePendingTransactions() {
        int update = pendingState.packagePendingTransactions(signedTxList, 5L);

        Assert.assertEquals(update, 2);
    }

    @Test public void getPackagedTransactions() {
        List<SignedTransaction> signedTransactions = pendingState.getPackagedTransactions(5L);
        Assert.assertEquals(signedTransactions.size(), 3);
        System.out.println(signedTransactions);
    }
}