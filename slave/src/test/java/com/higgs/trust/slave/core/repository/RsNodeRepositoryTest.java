package com.higgs.trust.slave.core.repository;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import com.higgs.trust.slave.model.bo.manage.RsNode;
import com.higgs.trust.slave.model.bo.manage.RsPubKey;
import com.higgs.trust.slave.model.enums.biz.RsNodeStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

/*
 *
 * @desc
 * @author tangfashuang
 * @date 2018/4/14
 *
 */
public class RsNodeRepositoryTest extends BaseTest {
    @Autowired
    private RsNodeRepository rsNodeRepository;

    private RsNode rsNode;

    @BeforeMethod public void setUp() throws Exception {
        rsNode = new RsNode();
        rsNode.setRsId("rs-test3");
        rsNode.setStatus(RsNodeStatusEnum.COMMON);
        rsNode.setDesc("rs-test3");
    }

    @Test public void queryAll() {
        List<RsNode> rsNodeList = rsNodeRepository.queryAll();
        rsNodeList.forEach(rsNode -> {
            System.out.println(rsNode);
        });
    }

    // cannot acuqire rsNode
    @Test public void queryByRsIdReturnNull() {
        RsNode rsNode = rsNodeRepository.queryByRsId("test");
        assertEquals(null, rsNode);
    }

    // success
    @Test public void queryByRsId() {
        RsNode rsNode = rsNodeRepository.queryByRsId("rs-test3");
        System.out.println(rsNode);
    }

    @Test public void convertActionToRsNode() {
        RegisterRS registerRS = new RegisterRS();
        registerRS.setRsId("rs-test4");
        registerRS.setDesc("rs-test4-RsNode");

        RsNode rs = rsNodeRepository.convertActionToRsNode(registerRS);
        assertEquals(rs.getRsId(), registerRS.getRsId());

    }

    @Test public void testQueryRsAndPubKey() throws Exception {
        List<RsPubKey> rsPubKeyList = rsNodeRepository.queryRsAndPubKey();
        System.out.println(rsPubKeyList);
    }
}