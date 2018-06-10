package com.higgs.trust.slave.core.repository;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.model.bo.manage.RegisterRS;
import com.higgs.trust.slave.model.bo.manage.RsNode;
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
public class RsNodeRepositoryTest extends BaseTest {
    @Autowired
    private RsNodeRepository rsNodeRepository;

    private RsNode rsNode;

    @BeforeMethod public void setUp() throws Exception {
        rsNode = new RsNode();
        rsNode.setRsId("rs-test3");
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
        Assert.assertEquals(null, rsNode);
    }

    // success
    @Test public void queryByRsId() {
        RsNode rsNode = rsNodeRepository.queryByRsId("rs-test1");
        System.out.println(rsNode);
    }

    @Test public void save() {
        rsNodeRepository.save(rsNode);
        RsNode rsNode1 = rsNodeRepository.queryByRsId(rsNode.getRsId());
    }

    @Test public void convertActionToRsNode() {
        RegisterRS registerRS = new RegisterRS();
        registerRS.setRsId("rs-test4");
        registerRS.setDesc("rs-test4-RsNode");

        RsNode rs = rsNodeRepository.convertActionToRsNode(registerRS);
        Assert.assertEquals(rs.getRsId(), registerRS.getRsId());

    }
}