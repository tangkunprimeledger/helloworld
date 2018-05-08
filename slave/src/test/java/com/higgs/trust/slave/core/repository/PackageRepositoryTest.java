package com.higgs.trust.slave.core.repository;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 *
 * @desc
 * @author tangfashuang
 * @date 2018/4/14
 *
 */
public class PackageRepositoryTest extends BaseTest {

    @Autowired
    private PackageRepository packageRepository;

    private static final String priKey1 =
        "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBALhZZQEKaaZOsD4z+m1AoBlVnSZFD9mafaRAt9PVHtgkFJViV8a3s+2bFEkxVIxLSqdyWVCqZCOm1+6jbWmk4pN5yY4AaZBCPM6kHkzl/o5ctGXqkIf9s8WxFAmKpMEi7qP6SCitaoMy1sC+IS/vcOphzJZ3NbP3kG0FyCR5EvGdAgMBAAECgYAf7HMeRARZpWTF0NB8HOXcnUPSfcEp6KP7Tq3GxDBMM6tQ1y/mHKfO7L0Nk7pVdTBfYODwpCElP15DWA+5bLFDl2GzCY3gBoMrgrpIaaD672Qf12ikcVf6q/FSNJAvvDPSpLQKYJKG4Aa8/0mFZB9JU1KM2+wDl4Fgf/Lf+vM/zQJBAN1X5nvjy4nwTXlHWvtB+PG9ptAsGo9baEtGW2UrDhPYObghJpk4Slxo+r7l4fWNnwEP6kMGBlFMH41oxNezYLcCQQDVNqiBJBVUgIVGaLSb3ksyXerOf9w1g7JTpSoZmf6SOMAZQ3kqky9Ik4LGwnlFaolUEA/fcTev2rC8iBFt00RLAkB09I5H5jzVXRFCxQ5w9xIYghKTqso5952rMLj4QwDEQZt2DKY9jb3VCG990TBNNJDQ2dz5n0RVTrjZWoOwSgsPAkBvFcArMIKQeTl22pymzOV+w2HP3tv7YbcqT1Yk6o+w3TJwty/M18x90qUDK1WFriEIlCnA77rku1rzjy0NfFILAkBnbOFY4ECXdFysok3n5AgrdHBr8bRSRvzBpdjUL9eH0Cs/37aqwZynwCg6DX5/I2gIwX78ysnujB4mY6lFOG5f";

    private static final String priKey2 =
        "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKQwv8kaTmN15Z7c6gz3/7a8wmtuJqgn25uWAkBO5vTs6DpfB0Nf7N5jOH2pMhkgqkdiOlpNpTb+zoJZ+DNy28mHHbpb99GDEoa5zvcXxypU5yrNhmrch1bJbKZQiGoX/5NAia9t/Kltxdcs6EmWuOQB79fLhLDgwHeUDzYOdM13AgMBAAECgYAFWznGe78262+0QQy5o5WKBpppGszUC4jUiI5GPsy2DMx+qv73qbd2gdIj91MVEsW7Um8I5yOOqb1e70RzmTmmSgmIbc7L2ogkEVa/AWdnmFIqVV7EOokc7pExc0UMlIBXCiNynrQic0YtxV65JjaE/JAFomCCAUBbsP9TSs/ZMQJBANRq8rBvR1PCA9pwzqfwalKAAzpwsOs0tavP8XF80xm7XKNZrnOIIiLSj+ME630ECYJZ2XTKF1g/TblIHV8zAYMCQQDF4LQcqKuNbeUeu0Xf3VX0TXPImIdB3ZbbQyPuynhk5D0Fx72q29gRKUZifrm1Kog6fvrwN1IyuoZem3oijEX9AkApkKPckmnKofRPEjPd+NVVP2diUBrOa4oBDLeaFWrZZihCbpIMWV8UoU82hQfvdpLFxv8eM01OH1T+JHZa4ogxAkA2WEs/H7fV5NurQAWlwPUNXoQxEGr9VO1MlLj2qRa9ps13m+7kUPKba/mPrXw1XFQDtMIYXSkvE3k53HuDp4DFAkAxhxi9veGOKa24Fp+4MFSF3L9UdR6MROqIYVGgE0gHj7r+NIuCqk/l9acw9W4E5gAN03P3RAKpjmcqxOkZyj7h";

    private List<SignedTransaction> signedTransactions = new ArrayList<>();

    private Package pack;


    @BeforeMethod public void setUp() throws Exception {

        SignedTransaction signedTx1 = initTx();
        signedTx1.getCoreTx().setTxId("pending-tx-test-5");

        SignedTransaction signedTx2 = initTx();
        signedTx2.getCoreTx().setTxId("pending-tx-test-4");

        signedTransactions.add(signedTx1);
//        signedTransactions.add(signedTx2);
    }

    private SignedTransaction initTx() throws Exception{
        SignedTransaction signedTransaction = new SignedTransaction();

        RegisterPolicy registerPolicy = new RegisterPolicy();
        registerPolicy.setPolicyId("test-policy-3");
        registerPolicy.setPolicyName("测试注册policy-3");

        List<String> rsIds = new ArrayList<>();
        rsIds.add("rs-test2");
        rsIds.add("rs-test3");
        registerPolicy.setRsIds(rsIds);

        CoreTransaction coreTx1 = new CoreTransaction();
        List<Action> registerPolicyList = new ArrayList<>();
        registerPolicyList.add(registerPolicy);

        coreTx1.setTxId("pending-tx-test-5");
        coreTx1.setActionList(registerPolicyList);
        coreTx1.setBizModel(null);
        coreTx1.setVersion("2.0.0");
        coreTx1.setPolicyId("000000");

        String sign1 = SignUtils.sign(JSON.toJSONString(coreTx1), priKey1);
        String sign2 = SignUtils.sign(JSON.toJSONString(coreTx1), priKey2);
        List<String> signList = new ArrayList<>();
        signList.add(sign1);
        signList.add(sign2);
        signedTransaction.setCoreTx(coreTx1);
        signedTransaction.setSignatureList(signList);

        return signedTransaction;
    }

    @Test public void save() {
        pack = new Package();
        pack.setSignedTxList(signedTransactions);
        pack.setStatus(PackageStatusEnum.INIT);
        pack.setHeight(3L);
        pack.setPackageTime(System.currentTimeMillis());

        packageRepository.save(pack);
    }

    @Test public void updateStatus() {
        packageRepository.updateStatus(3L, PackageStatusEnum.INIT, PackageStatusEnum.RECEIVED);
    }

    @Test public void load() {
        Package packa = packageRepository.load(2L);
        Assert.assertEquals(2L, packa.getHeight().longValue());
        Assert.assertEquals(PackageStatusEnum.RECEIVED.getCode(), packa.getStatus());
        Assert.assertEquals(2, packa.getSignedTxList().size());
    }

    @Test public void load1() {
    }

    @Test public void loadAndLock() {
        Package packa = packageRepository.loadAndLock(3L);
        Assert.assertEquals(3L, packa.getHeight().longValue());
        Assert.assertEquals(PackageStatusEnum.RECEIVED.getCode(), packa.getStatus());
        Assert.assertEquals(2, packa.getSignedTxList().size());
    }

    @Test public void getMaxHeight() {
        Long height = packageRepository.getMaxHeight();

        Assert.assertEquals(3L, height.longValue());
    }

    @Test public void getHeightListByStatus() {
        List<Long> heightList = packageRepository.getHeightListByStatus(PackageStatusEnum.INIT.getCode());
        Assert.assertEquals(null, heightList);

        List<Long> heights = packageRepository.getHeightListByStatus(PackageStatusEnum.RECEIVED.getCode());
        Assert.assertEquals(1, heights.size());
    }

    @Test public void getMinHeight() {
        Set<String> statusSet = new HashSet<>();
        statusSet.add(PackageStatusEnum.INIT.getCode());
        statusSet.add(PackageStatusEnum.RECEIVED.getCode());
        Long height = packageRepository.getMinHeight(statusSet);

        Assert.assertEquals(3L, height.longValue());
    }

    @Test public void count() {
        Set<String> statusSet = new HashSet<>();
        statusSet.add(PackageStatusEnum.INIT.getCode());
        statusSet.add(PackageStatusEnum.RECEIVED.getCode());
        long count = packageRepository.count(statusSet);

        Assert.assertEquals(1, count);
    }

    @Test
    public void getHeightListForProcess() {
      // List<Long> heightList = packageRepository.getHeightListForProcess(4L);
       // System.out.println(heightList);
    }
}