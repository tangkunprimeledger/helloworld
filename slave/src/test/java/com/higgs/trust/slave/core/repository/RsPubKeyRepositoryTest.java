package com.higgs.trust.slave.core.repository;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import com.higgs.trust.slave.model.bo.manage.RsPubKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

/*
 *
 * @desc
 * @author tangfashuang
 * @date 2018/4/14
 *
 */
public class RsPubKeyRepositoryTest extends BaseTest {
    @Autowired
    private RsPubKeyRepository rsPubKeyRepository;

    private RsPubKey rsPubKey;

    @BeforeMethod public void setUp() throws Exception {
        rsPubKey = new RsPubKey();
        rsPubKey.setRsId("rs-test3");
        rsPubKey.setPubKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDQ801oyESemN3Sk+GdPP0LlDI49zhUoLfKg/WRYE9014pOOKJT+ztIFSYi/rQbQrqrFup1NgVrdkv9f0phlatXcXKNVUglPw5O0XgUzEcmQTsX83TJ2k1jG4jvhE6HSZd3kGytZoiclW4O4S3u7P/MYh+wBrCpKaVuhguyqW+MyQIDAQAB");
        rsPubKey.setDesc("rs-test3-RsPubKey");
    }

    @Test public void queryAll() {
        List<RsPubKey> rsPubKeyList = rsPubKeyRepository.queryAll();
        rsPubKeyList.forEach(rsPubKey -> {
            System.out.println(rsPubKey);
        });
    }

    // cannot acuqire rsPubkey
    @Test public void queryByRsIdReturnNull() {
        RsPubKey rsPubKey = rsPubKeyRepository.queryByRsId("test");
        Assert.assertEquals(null, rsPubKey);
    }

    // success
    @Test public void queryByRsId() {
        RsPubKey rsPubKey = rsPubKeyRepository.queryByRsId("rs-test1");
        System.out.println(rsPubKey);
    }

    @Test public void save() {
        rsPubKeyRepository.save(rsPubKey);
        RsPubKey rsPubKey1 = rsPubKeyRepository.queryByRsId(rsPubKey.getRsId());
        Assert.assertEquals(rsPubKey.getPubKey(), rsPubKey1.getPubKey());
        Assert.assertEquals(rsPubKey.getDesc(), rsPubKey1.getDesc());
    }

    @Test public void convertActionToRsPubKey() {
        RegisterRS registerRS = new RegisterRS();
        registerRS.setRsId("rs-test4");
        registerRS.setDesc("rs-test4-RsPubKey");
        registerRS.setPubKey("test-public-key");

        RsPubKey rs = rsPubKeyRepository.convertActionToRsPubKey(registerRS);
        Assert.assertEquals(rs.getDesc(), registerRS.getDesc());
        Assert.assertEquals(rs.getPubKey(), registerRS.getPubKey());
        Assert.assertEquals(rs.getRsId(), registerRS.getRsId());

    }
}