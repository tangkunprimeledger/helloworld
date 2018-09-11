package com.higgs.trust.slave.core.repository;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.common.crypto.rsa.Rsa;
import com.higgs.trust.common.dao.RocksUtils;
import com.higgs.trust.common.utils.ThreadLocalUtils;
import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.common.config.InitConfig;
import com.higgs.trust.slave.core.repository.config.SystemPropertyRepository;
import com.higgs.trust.slave.dao.po.pack.PackagePO;
import com.higgs.trust.slave.dao.rocks.pack.PackRocksDao;
import com.higgs.trust.slave.dao.rocks.pack.PackStatusRocksDao;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.Package;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import com.higgs.trust.slave.model.bo.action.Action;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author tangfashuang
 */
public class PackageRepositoryTest extends BaseTest {

    @Autowired private PackageRepository packageRepository;
    @Autowired private PackStatusRocksDao packStatusRocksDao;
    @Autowired private PackRocksDao packRocksDao;
    @Autowired private SystemPropertyRepository systemPropertyRepository;
    @Autowired private InitConfig initConfig;

    private static final String priKey1 =
        "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBALhZZQEKaaZOsD4z+m1AoBlVnSZFD9mafaRAt9PVHtgkFJViV8a3s+2bFEkxVIxLSqdyWVCqZCOm1+6jbWmk4pN5yY4AaZBCPM6kHkzl/o5ctGXqkIf9s8WxFAmKpMEi7qP6SCitaoMy1sC+IS/vcOphzJZ3NbP3kG0FyCR5EvGdAgMBAAECgYAf7HMeRARZpWTF0NB8HOXcnUPSfcEp6KP7Tq3GxDBMM6tQ1y/mHKfO7L0Nk7pVdTBfYODwpCElP15DWA+5bLFDl2GzCY3gBoMrgrpIaaD672Qf12ikcVf6q/FSNJAvvDPSpLQKYJKG4Aa8/0mFZB9JU1KM2+wDl4Fgf/Lf+vM/zQJBAN1X5nvjy4nwTXlHWvtB+PG9ptAsGo9baEtGW2UrDhPYObghJpk4Slxo+r7l4fWNnwEP6kMGBlFMH41oxNezYLcCQQDVNqiBJBVUgIVGaLSb3ksyXerOf9w1g7JTpSoZmf6SOMAZQ3kqky9Ik4LGwnlFaolUEA/fcTev2rC8iBFt00RLAkB09I5H5jzVXRFCxQ5w9xIYghKTqso5952rMLj4QwDEQZt2DKY9jb3VCG990TBNNJDQ2dz5n0RVTrjZWoOwSgsPAkBvFcArMIKQeTl22pymzOV+w2HP3tv7YbcqT1Yk6o+w3TJwty/M18x90qUDK1WFriEIlCnA77rku1rzjy0NfFILAkBnbOFY4ECXdFysok3n5AgrdHBr8bRSRvzBpdjUL9eH0Cs/37aqwZynwCg6DX5/I2gIwX78ysnujB4mY6lFOG5f";

    private static final String priKey2 =
        "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKQwv8kaTmN15Z7c6gz3/7a8wmtuJqgn25uWAkBO5vTs6DpfB0Nf7N5jOH2pMhkgqkdiOlpNpTb+zoJZ+DNy28mHHbpb99GDEoa5zvcXxypU5yrNhmrch1bJbKZQiGoX/5NAia9t/Kltxdcs6EmWuOQB79fLhLDgwHeUDzYOdM13AgMBAAECgYAFWznGe78262+0QQy5o5WKBpppGszUC4jUiI5GPsy2DMx+qv73qbd2gdIj91MVEsW7Um8I5yOOqb1e70RzmTmmSgmIbc7L2ogkEVa/AWdnmFIqVV7EOokc7pExc0UMlIBXCiNynrQic0YtxV65JjaE/JAFomCCAUBbsP9TSs/ZMQJBANRq8rBvR1PCA9pwzqfwalKAAzpwsOs0tavP8XF80xm7XKNZrnOIIiLSj+ME630ECYJZ2XTKF1g/TblIHV8zAYMCQQDF4LQcqKuNbeUeu0Xf3VX0TXPImIdB3ZbbQyPuynhk5D0Fx72q29gRKUZifrm1Kog6fvrwN1IyuoZem3oijEX9AkApkKPckmnKofRPEjPd+NVVP2diUBrOa4oBDLeaFWrZZihCbpIMWV8UoU82hQfvdpLFxv8eM01OH1T+JHZa4ogxAkA2WEs/H7fV5NurQAWlwPUNXoQxEGr9VO1MlLj2qRa9ps13m+7kUPKba/mPrXw1XFQDtMIYXSkvE3k53HuDp4DFAkAxhxi9veGOKa24Fp+4MFSF3L9UdR6MROqIYVGgE0gHj7r+NIuCqk/l9acw9W4E5gAN03P3RAKpjmcqxOkZyj7h";

    private List<SignedTransaction> signedTransactions = new ArrayList<>();

    private Package pack;

    @BeforeMethod public void setUp() throws Exception {

        SignedTransaction signedTx1 = initTx();
        signedTx1.getCoreTx().setTxId("pending-tx-test-2");

        SignedTransaction signedTx2 = initTx();
        signedTx2.getCoreTx().setTxId("pending-tx-test-1");

        signedTransactions.add(signedTx1);
        signedTransactions.add(signedTx2);
    }

    private SignedTransaction initTx() throws Exception {
        SignedTransaction signedTransaction = new SignedTransaction();

        RegisterPolicy registerPolicy = new RegisterPolicy();
        registerPolicy.setPolicyId("test-policy-1");
        registerPolicy.setPolicyName("测试注册policy-1");

        List<String> rsIds = new ArrayList<>();
        rsIds.add("rs-test2");
        rsIds.add("rs-test3");
        registerPolicy.setRsIds(rsIds);

        CoreTransaction coreTx1 = new CoreTransaction();
        List<Action> registerPolicyList = new ArrayList<>();
        registerPolicyList.add(registerPolicy);

        coreTx1.setTxId("pending-tx-test-1");
        coreTx1.setActionList(registerPolicyList);
        coreTx1.setBizModel(null);
        coreTx1.setVersion("2.0.0");
        coreTx1.setPolicyId("000000");

        String sign1 = Rsa.sign(JSON.toJSONString(coreTx1), priKey1);
        String sign2 = Rsa.sign(JSON.toJSONString(coreTx1), priKey2);
        List<String> signList = new ArrayList<>();
        signList.add(sign1);
        signList.add(sign2);
        signedTransaction.setCoreTx(coreTx1);
        //        signedTransaction.setSignatureList(signList);

        return signedTransaction;
    }

    @Test public void testSave() throws Exception {
        pack = new Package();
        pack.setSignedTxList(signedTransactions);
        pack.setStatus(PackageStatusEnum.RECEIVED);
        pack.setHeight(2L);
        pack.setPackageTime(System.currentTimeMillis());

        try {
            ThreadLocalUtils.putWriteBatch(new WriteBatch());
            for (int i = 0; i < 100; i++) {
                pack.setHeight(2L + i);
                packageRepository.save(pack);
            }

            RocksUtils.batchCommit(new WriteOptions(), ThreadLocalUtils.getWriteBatch());
            System.out.println("after commit. load package height=20L, " + packageRepository.load(20L));
        } finally {
            ThreadLocalUtils.clearWriteBatch();
        }
    }

    @Test public void testUpdateStatus() throws Exception {
        if (initConfig.isUseMySQL()) {
            packageRepository.updateStatus(3L, PackageStatusEnum.RECEIVED, PackageStatusEnum.WAIT_PERSIST_CONSENSUS);
        } else {
            try {
                //Long height = packStatusRocksDao.get(PackageStatusEnum.WAIT_PERSIST_CONSENSUS.getCode() + Constant.SPLIT_SLASH + 2);
                ThreadLocalUtils.putWriteBatch(new WriteBatch());
//                packageRepository
//                    .updateStatus(5L, PackageStatusEnum.RECEIVED, PackageStatusEnum.WAIT_PERSIST_CONSENSUS);
//                packageRepository
//                    .updateStatus(10L, PackageStatusEnum.RECEIVED, PackageStatusEnum.WAIT_PERSIST_CONSENSUS);
//                packageRepository
//                    .updateStatus(20L, PackageStatusEnum.RECEIVED, PackageStatusEnum.WAIT_PERSIST_CONSENSUS);
//                packageRepository
//                    .updateStatus(30L, PackageStatusEnum.RECEIVED, PackageStatusEnum.WAIT_PERSIST_CONSENSUS);
//                packageRepository
//                    .updateStatus(40L, PackageStatusEnum.RECEIVED, PackageStatusEnum.WAIT_PERSIST_CONSENSUS);
//                packageRepository
//                    .updateStatus(50L, PackageStatusEnum.RECEIVED, PackageStatusEnum.WAIT_PERSIST_CONSENSUS);
//                packageRepository
//                    .updateStatus(60L, PackageStatusEnum.RECEIVED, PackageStatusEnum.WAIT_PERSIST_CONSENSUS);
//                packageRepository
//                    .updateStatus(70L, PackageStatusEnum.RECEIVED, PackageStatusEnum.WAIT_PERSIST_CONSENSUS);
//                packageRepository
//                    .updateStatus(80L, PackageStatusEnum.RECEIVED, PackageStatusEnum.WAIT_PERSIST_CONSENSUS);
//                packageRepository
//                    .updateStatus(90L, PackageStatusEnum.RECEIVED, PackageStatusEnum.WAIT_PERSIST_CONSENSUS);
                packageRepository
                    .updateStatus(80L, PackageStatusEnum.WAIT_PERSIST_CONSENSUS, PackageStatusEnum.PERSISTED);
                packageRepository
                    .updateStatus(50L, PackageStatusEnum.WAIT_PERSIST_CONSENSUS, PackageStatusEnum.PERSISTED);
                packageRepository
                    .updateStatus(30L, PackageStatusEnum.WAIT_PERSIST_CONSENSUS, PackageStatusEnum.PERSISTED);
                packageRepository
                    .updateStatus(40L, PackageStatusEnum.WAIT_PERSIST_CONSENSUS, PackageStatusEnum.PERSISTED);
                packageRepository
                    .updateStatus(90L, PackageStatusEnum.WAIT_PERSIST_CONSENSUS, PackageStatusEnum.PERSISTED);
                packageRepository
                    .updateStatus(70L, PackageStatusEnum.WAIT_PERSIST_CONSENSUS, PackageStatusEnum.PERSISTED);
                RocksUtils.batchCommit(new WriteOptions(), ThreadLocalUtils.getWriteBatch());
            } finally {
                ThreadLocalUtils.clearWriteBatch();
            }
        }
    }

    @Test public void testLoad() throws Exception {
        Package packa = packageRepository.load(2L);
        Assert.assertEquals(2L, packa.getHeight().longValue());
        Assert.assertEquals(packa.getStatus(), PackageStatusEnum.PERSISTED);
        Assert.assertEquals(1, packa.getSignedTxList().size());
    }

    @Test public void testLoadHeightList() throws Exception {
        List<Long> heights = packageRepository.loadHeightList(25L);
    }

    @Test public void testLoadAndLock() throws Exception {
        Package packa = packageRepository.loadAndLock(3L);
        Assert.assertEquals(3L, packa.getHeight().longValue());
        Assert.assertEquals(PackageStatusEnum.RECEIVED.getCode(), packa.getStatus());
        Assert.assertEquals(2, packa.getSignedTxList().size());
    }

    @Test public void testGetMaxHeight() throws Exception {
        if (!initConfig.isUseMySQL()) {
            systemPropertyRepository.add(Constant.MAX_PACK_HEIGHT, String.valueOf(101L), "max package height");
        }
        Long height = packageRepository.getMaxHeight();

        Assert.assertEquals(101L, height.longValue());
    }

    @Test public void testGetMinHeight() throws Exception {
    }

    @Test public void testCount() throws Exception {
        Set<String> statusSet = new HashSet<>();
        statusSet.add(PackageStatusEnum.WAIT_PERSIST_CONSENSUS.getCode());
        statusSet.add(PackageStatusEnum.RECEIVED.getCode());
        long count = packageRepository.count(statusSet, 7L);

        Assert.assertEquals(1, count);
    }

    @Test public void testGetMaxHeightByStatus() throws Exception {
        List<String> keys = packStatusRocksDao.keys();
        Long height = packageRepository.getMaxHeightByStatus(PackageStatusEnum.WAIT_PERSIST_CONSENSUS);
        Package pack = packageRepository.load(height);
    }

    @Test public void testGetMinHeightByStatus() throws Exception {
        List<String> keys = packStatusRocksDao.keys();
        Long height = packageRepository.getMinHeightByStatus(PackageStatusEnum.PERSISTED);
    }

    @Test public void testDeleteLessThanHeightAndStatus() throws Exception {
        List<String> keys = packStatusRocksDao.keys();
        packageRepository.deleteLessThanHeightAndStatus(193L, PackageStatusEnum.RECEIVED);
        List<String> newKeys = packStatusRocksDao.keys();
        List<String> newPackKeys = packRocksDao.keys();
    }

    @Test public void testDeleteRange() throws Exception {
        List<String> keys = packStatusRocksDao.keys();
        System.out.println(keys);
        String beginKey = packStatusRocksDao.queryFirstKey(PackageStatusEnum.WAIT_PERSIST_CONSENSUS.getIndex());
        String endKey = null;
        List<String> indexList = PackageStatusEnum.getIndexs(PackageStatusEnum.WAIT_PERSIST_CONSENSUS.getIndex());
        for (String index : indexList) {
            String key = packStatusRocksDao.queryFirstKey(index);
            if (!StringUtils.isEmpty(key)) {
                endKey = key;
                break;
            }
        }
        if (StringUtils.isEmpty(endKey)) {
            endKey = packStatusRocksDao.queryLastKey();
        }
//        packStatusRocksDao.deleteRange(beginKey, endKey);
//        packStatusRocksDao.delete(endKey);

        //        List<String> heights1 = packStatusRocksDao.queryKeysByPrefix(PackageStatusEnum.WAIT_PERSIST_CONSENSUS.getCode());
        //        List<String> heights2 = packStatusRocksDao.queryKeysByPrefix(PackageStatusEnum.PERSISTED.getCode());
        //        String beginKey = packRocksDao.queryFirstKey(null);
        //        String endKey = packRocksDao.queryLastKey(null);
        //        packRocksDao.deleteRange(beginKey, endKey);
        //        packRocksDao.delete(endKey);
        //                List<String> keys = packRocksDao.keys();

    }

    @Test public void testTransaction() {
        String key = "0000000000000020";
        new Thread(new Runnable() {
            @Override public void run() {
                Transaction tx = RocksUtils.beginTransaction(new WriteOptions());
                ThreadLocalUtils.putWriteBatch(new WriteBatch());
                try {
                    Long height = packageRepository.getForUpdate(tx, new ReadOptions(), key, false);
                    System.out.println(Thread.currentThread().getName() + " acquired lock");
                    packageRepository
                        .updateStatus(20L, PackageStatusEnum.WAIT_PERSIST_CONSENSUS, PackageStatusEnum.PERSISTED);
                    Thread.sleep(200);
                    tx.commit();
                    RocksUtils.batchCommit(new WriteOptions(), ThreadLocalUtils.getWriteBatch());
                } catch (RocksDBException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    System.out.println(e);
                } finally {
                    ThreadLocalUtils.clearWriteBatch();
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override public void run() {
                Transaction tx = RocksUtils.beginTransaction(new WriteOptions());
                ThreadLocalUtils.putWriteBatch(new WriteBatch());
                try {
                    System.out.println("acquired value = " + packStatusRocksDao.get(key));
                    Long height = packageRepository.getForUpdate(tx, new ReadOptions(), key, true);
                    System.out.println(Thread.currentThread().getName() + "acquired lock");
                    packageRepository
                        .updateStatus(20L, PackageStatusEnum.WAIT_PERSIST_CONSENSUS, PackageStatusEnum.PERSISTED);
                    Thread.sleep(500);
                    tx.commit();
                    RocksUtils.batchCommit(new WriteOptions(), ThreadLocalUtils.getWriteBatch());
                } catch (RocksDBException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    System.out.println(e);
                } finally {
                    ThreadLocalUtils.clearWriteBatch();
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        String index = PackageStatusEnum.RECEIVED.getIndex();
        String format = "00";
        DecimalFormat decimalFormat = new DecimalFormat(format);
        System.out.println("index=" + decimalFormat.format(Integer.parseInt(index) + 2));
    }
}