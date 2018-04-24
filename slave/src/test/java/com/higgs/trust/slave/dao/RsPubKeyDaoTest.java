package com.higgs.trust.slave.dao;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.dao.manage.RsPubKeyDao;
import com.higgs.trust.slave.dao.po.manage.RsPubKeyPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

/**
 * @author tangfashuang
 * @date 2018/04/13 22:19
 * @desc policy dao test
 */
public class RsPubKeyDaoTest extends BaseTest {
    @Autowired RsPubKeyDao rsPubKeyDao;

    @Test public void testAdd1() {
        RsPubKeyPO rsPubKey = new RsPubKeyPO();
        rsPubKey.setPubKey(
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC4WWUBCmmmTrA+M/ptQKAZVZ0mRQ/Zmn2kQLfT1R7YJBSVYlfGt7PtmxRJMVSMS0qncllQqmQjptfuo21ppOKTecmOAGmQQjzOpB5M5f6OXLRl6pCH/bPFsRQJiqTBIu6j+kgorWqDMtbAviEv73DqYcyWdzWz95BtBcgkeRLxnQIDAQAB");
        rsPubKey.setRsId("rs-test1");
        rsPubKey.setDesc("rs-test1-desc");

        rsPubKeyDao.add(rsPubKey);
    }

    @Test public void testAdd2() {
        RsPubKeyPO rsPubKey = new RsPubKeyPO();
        rsPubKey.setPubKey(
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCkML/JGk5jdeWe3OoM9/+2vMJrbiaoJ9ublgJATub07Og6XwdDX+zeYzh9qTIZIKpHYjpaTaU2/s6CWfgzctvJhx26W/fRgxKGuc73F8cqVOcqzYZq3IdWyWymUIhqF/+TQImvbfypbcXXLOhJlrjkAe/Xy4Sw4MB3lA82DnTNdwIDAQAB");
        rsPubKey.setRsId("rs-test2");
        rsPubKey.setDesc("rs-test2-desc");

        rsPubKeyDao.add(rsPubKey);
    }

    @Test public void testAdd3() {
        RsPubKeyPO rsPubKey = new RsPubKeyPO();
        rsPubKey.setPubKey(
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDQ801oyESemN3Sk+GdPP0LlDI49zhUoLfKg/WRYE9014pOOKJT+ztIFSYi/rQbQrqrFup1NgVrdkv9f0phlatXcXKNVUglPw5O0XgUzEcmQTsX83TJ2k1jG4jvhE6HSZd3kGytZoiclW4O4S3u7P/MYh+wBrCpKaVuhguyqW+MyQIDAQAB");
        rsPubKey.setRsId("rs-test3");
        rsPubKey.setDesc("rs-test3-desc");

        rsPubKeyDao.add(rsPubKey);
    }
}