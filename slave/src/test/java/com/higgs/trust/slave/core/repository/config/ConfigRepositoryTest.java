package com.higgs.trust.slave.core.repository.config;

import com.higgs.trust.slave.BaseTest;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.dao.po.config.ConfigPO;
import com.higgs.trust.slave.model.bo.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;

public class ConfigRepositoryTest extends BaseTest {

    @Autowired private ConfigRepository configRepository;

    @Test public void testInsertConfig() throws Exception {
        Config config = new Config();
        config.setNodeName("node-1");
        config.setVersion(VersionEnum.V1.getCode());
        config.setPriKey("prikey");
        config.setPubKey("pubkey");
        config.setValid(true);
        config.setTmpPriKey("tempPriKey");
        config.setTmpPubKey("tempPubKey");
        configRepository.insertConfig(config);
    }

    @Test public void testUpdateConfig() throws Exception {
        Config config = new Config();
        config.setNodeName("node-1");
//        config.setTmpPubKey("tempPubKey");
//        config.setTmpPriKey("tempPriKey");
        config.setValid(false);
        configRepository.updateConfig(config);
    }

    @Test public void testGetConfig() throws Exception {
        System.out.println(configRepository.getBizConfig("node-1"));
    }

    @Test public void testBatchInsert() throws Exception {
        List list = new LinkedList();
        for (int i = 0; i < 5; i++) {
            ConfigPO config = new ConfigPO();
            config.setNodeName("node-" + i);
            config.setVersion(VersionEnum.V1.getCode());
            config.setPriKey("prikey");
            config.setPubKey("pubkey");
            config.setValid(true);
            config.setTmpPriKey("tempPriKey");
            config.setTmpPubKey("tempPubKey");
            list.add(config);
        }
        configRepository.batchInsert(list);
    }

    @Test public void testBatchUpdate() throws Exception {
        List list = new LinkedList();
        for (int i = 0; i < 1; i++) {
            ConfigPO config = new ConfigPO();
            config.setNodeName("node-" + i);
            config.setPriKey("prikey"+i);
            config.setPubKey("pubkey"+i);
            config.setValid(true);
            config.setTmpPriKey("haha"+i);
            config.setTmpPubKey("hehe"+i);
            list.add(config);
        }
        ConfigPO config = new ConfigPO();
        config.setNodeName("node-" + 2);
        config.setPriKey("prikey"+100);
        config.setPubKey("pubkey"+100);
        config.setValid(true);
        config.setTmpPriKey("di"+100);
        config.setTmpPubKey("pol"+100);
        list.add(config);
        configRepository.batchUpdate(list);
    }
}