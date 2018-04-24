package com.higgs.trust.slave.dao.pack;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.IntegrateBaseTest;
import com.higgs.trust.slave.dao.po.pack.PackagePO;
import com.higgs.trust.slave.model.enums.biz.PackageStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
public class PackageDaoTest extends BaseTest {

    @Autowired
    private PackageDao packageDao;

    private PackagePO packagePO = new PackagePO();

    @BeforeMethod public void setUp() throws Exception {
        packagePO.setStatus(PackageStatusEnum.WAIT_VALIDATE_CONSENSUS.getCode());
        packagePO.setPackageTime(System.currentTimeMillis());
        packagePO.setHeight(4L);
    }

    @Test
    public  void save() {
        packageDao.add(packagePO);

        PackagePO po = packageDao.queryByHeight(packagePO.getHeight());

        Assert.assertEquals(po.getHeight(), packagePO.getHeight());
        Assert.assertEquals(po.getPackageTime(), packagePO.getPackageTime());
        Assert.assertEquals(po.getStatus(), packagePO.getStatus());
    }

    @Test public void queryByHeight() {
        PackagePO po = packageDao.queryByHeight(packagePO.getHeight());

        Assert.assertEquals(po.getHeight(), packagePO.getHeight());
        Assert.assertEquals(po.getPackageTime(), packagePO.getPackageTime());
        Assert.assertEquals(po.getStatus(), packagePO.getStatus());
    }

    @Test public void queryByHeightForUpdate() {
    }

    @Test public void updateStatus() {
        packageDao.updateStatus(2L, PackageStatusEnum.INIT.getCode(), PackageStatusEnum.RECEIVED.getCode());

        PackagePO po = packageDao.queryByHeightForUpdate(2L);

        Assert.assertEquals(po.getStatus(), PackageStatusEnum.VALIDATED.getCode());

    }

    @Test public void getMaxHeight() {
        Long max = packageDao.getMaxHeight();
        Assert.assertEquals(4L, max.longValue());
    }

    @Test public void queryByStatus() {
        List<PackagePO> packagePOList = packageDao.queryByStatus(PackageStatusEnum.INIT.getCode());
        packagePOList.forEach(packagePO1 ->  {
            System.out.println(packagePO1);
        });
        Assert.assertEquals(2, packagePOList.size());
    }

    @Test public void queryHeightListByStatus() {
        List<Long> heightList = packageDao.queryHeightListByStatus(PackageStatusEnum.INIT.getCode());

        Assert.assertEquals(2L, heightList.get(0).longValue());
        Assert.assertEquals(3L, heightList.get(1).longValue());
        Assert.assertEquals(2, heightList.size());
    }

    @Test public void getMinHeightWithStatus() {
        Set<String> statusSet = new HashSet<>();
        statusSet.add(PackageStatusEnum.INIT.getCode());
        statusSet.add(PackageStatusEnum.RECEIVED.getCode());
        Long height = packageDao.getMinHeightWithStatus(statusSet);

        Assert.assertEquals(2L, height.longValue());
    }

    @Test public void countWithStatus() {
        Set<String> statusSet = new HashSet<>();
        statusSet.add(PackageStatusEnum.INIT.getCode());
        statusSet.add(PackageStatusEnum.RECEIVED.getCode());

        long count = packageDao.countWithStatus(statusSet);
        Assert.assertEquals(3L, count);
    }
}