package com.higgs.trust.slave.dao.pack;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.enums.ActionTypeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.dao.po.pack.PendingTransactionPO;
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
import java.util.Date;
import java.util.List;

/*
 *
 * @desc
 * @author tangfashuang
 * @date 2018/4/14
 *
 */
public class PendingTransactionDaoTest extends BaseTest {

    @Autowired
    private PendingTransactionDao pendingTransactionDao;

    private PendingTransactionPO pendingTransactionPO;

    private SignedTransaction signedTransaction;

    private static final String priKey1 =
        "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBALhZZQEKaaZOsD4z+m1AoBlVnSZFD9mafaRAt9PVHtgkFJViV8a3s+2bFEkxVIxLSqdyWVCqZCOm1+6jbWmk4pN5yY4AaZBCPM6kHkzl/o5ctGXqkIf9s8WxFAmKpMEi7qP6SCitaoMy1sC+IS/vcOphzJZ3NbP3kG0FyCR5EvGdAgMBAAECgYAf7HMeRARZpWTF0NB8HOXcnUPSfcEp6KP7Tq3GxDBMM6tQ1y/mHKfO7L0Nk7pVdTBfYODwpCElP15DWA+5bLFDl2GzCY3gBoMrgrpIaaD672Qf12ikcVf6q/FSNJAvvDPSpLQKYJKG4Aa8/0mFZB9JU1KM2+wDl4Fgf/Lf+vM/zQJBAN1X5nvjy4nwTXlHWvtB+PG9ptAsGo9baEtGW2UrDhPYObghJpk4Slxo+r7l4fWNnwEP6kMGBlFMH41oxNezYLcCQQDVNqiBJBVUgIVGaLSb3ksyXerOf9w1g7JTpSoZmf6SOMAZQ3kqky9Ik4LGwnlFaolUEA/fcTev2rC8iBFt00RLAkB09I5H5jzVXRFCxQ5w9xIYghKTqso5952rMLj4QwDEQZt2DKY9jb3VCG990TBNNJDQ2dz5n0RVTrjZWoOwSgsPAkBvFcArMIKQeTl22pymzOV+w2HP3tv7YbcqT1Yk6o+w3TJwty/M18x90qUDK1WFriEIlCnA77rku1rzjy0NfFILAkBnbOFY4ECXdFysok3n5AgrdHBr8bRSRvzBpdjUL9eH0Cs/37aqwZynwCg6DX5/I2gIwX78ysnujB4mY6lFOG5f";

    private static final String priKey2 =
        "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKQwv8kaTmN15Z7c6gz3/7a8wmtuJqgn25uWAkBO5vTs6DpfB0Nf7N5jOH2pMhkgqkdiOlpNpTb+zoJZ+DNy28mHHbpb99GDEoa5zvcXxypU5yrNhmrch1bJbKZQiGoX/5NAia9t/Kltxdcs6EmWuOQB79fLhLDgwHeUDzYOdM13AgMBAAECgYAFWznGe78262+0QQy5o5WKBpppGszUC4jUiI5GPsy2DMx+qv73qbd2gdIj91MVEsW7Um8I5yOOqb1e70RzmTmmSgmIbc7L2ogkEVa/AWdnmFIqVV7EOokc7pExc0UMlIBXCiNynrQic0YtxV65JjaE/JAFomCCAUBbsP9TSs/ZMQJBANRq8rBvR1PCA9pwzqfwalKAAzpwsOs0tavP8XF80xm7XKNZrnOIIiLSj+ME630ECYJZ2XTKF1g/TblIHV8zAYMCQQDF4LQcqKuNbeUeu0Xf3VX0TXPImIdB3ZbbQyPuynhk5D0Fx72q29gRKUZifrm1Kog6fvrwN1IyuoZem3oijEX9AkApkKPckmnKofRPEjPd+NVVP2diUBrOa4oBDLeaFWrZZihCbpIMWV8UoU82hQfvdpLFxv8eM01OH1T+JHZa4ogxAkA2WEs/H7fV5NurQAWlwPUNXoQxEGr9VO1MlLj2qRa9ps13m+7kUPKba/mPrXw1XFQDtMIYXSkvE3k53HuDp4DFAkAxhxi9veGOKa24Fp+4MFSF3L9UdR6MROqIYVGgE0gHj7r+NIuCqk/l9acw9W4E5gAN03P3RAKpjmcqxOkZyj7h";

    @BeforeMethod public void setUp() throws Exception {
        pendingTransactionPO = new PendingTransactionPO();
        pendingTransactionPO.setTxId("pending-tx-test-1");
        pendingTransactionPO.setStatus(PendingTxStatusEnum.INIT.getCode());

        signedTransaction = initTx();
        pendingTransactionPO.setTxData(JSON.toJSONString(signedTransaction));
//        pendingTransactionPO.setTxData(null);
    }

    private SignedTransaction initTx() throws Exception{
        SignedTransaction signedTx1 = new SignedTransaction();

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
        coreTx1.setLockTime(new Date());
        coreTx1.setBizModel(new JSONObject());
        coreTx1.setVersion(VersionEnum.V1.getCode());
        coreTx1.setPolicyId("000000");

        String sign1 = SignUtils.sign(JSON.toJSONString(coreTx1), priKey1);
        String sign2 = SignUtils.sign(JSON.toJSONString(coreTx1), priKey2);
        List<String> signList = new ArrayList<>();
        signList.add(sign1);
        signList.add(sign2);
        signedTx1.setCoreTx(coreTx1);
//        signedTx1.setSignatureList(signList);

        return signedTx1;

//        signedTxList.add(signedTx1);
    }

    @Test
    public void save() {
        pendingTransactionDao.add(pendingTransactionPO);
    }

    @Test public void queryByStatus() {
        List<PendingTransactionPO> poList = pendingTransactionDao.queryByStatus(PendingTxStatusEnum.INIT.getCode(), 20);
        poList.forEach(pendingTx -> {
            SignedTransaction signedTx = JSON.parseObject(pendingTx.getTxData(), SignedTransaction.class);
            System.out.println(signedTx);

        });

        Assert.assertEquals("pending-tx-test-0", poList.get(0).getTxId());
        Assert.assertEquals("pending-tx-test-1", poList.get(1).getTxId());
    }

    @Test public void queryByTxId() {
        PendingTransactionPO po = pendingTransactionDao.queryByTxId("pending-tx-test-1");
        SignedTransaction signedTx = JSON.parseObject(po.getTxData(), SignedTransaction.class);

        System.out.println(signedTx);
    }

    @Test public void updateStatus() {
        pendingTransactionDao.updateStatus("pending-tx-test-2", PendingTxStatusEnum.INIT.getCode(), PendingTxStatusEnum.PACKAGED.getCode(), 2L);

        PendingTransactionPO po = pendingTransactionDao.queryByTxId("pending-tx-test-0");

        Assert.assertEquals(2L, po.getHeight().longValue());
        Assert.assertEquals(PendingTxStatusEnum.PACKAGED.getCode(), po.getStatus());
    }

    @Test public void queryByHeight() {
        List<PendingTransactionPO> pendingTxList = pendingTransactionDao.queryByHeight(2L);
        System.out.println(pendingTxList);
        Assert.assertEquals(PendingTxStatusEnum.INIT.getCode(), pendingTxList.get(0).getStatus());
    }
}