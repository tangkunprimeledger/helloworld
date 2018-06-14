package com.higgs.trust.slave.core.service.ca;

import com.higgs.trust.config.node.NodeState;
import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.config.ConfigRepository;
import com.higgs.trust.slave.core.service.action.ca.CaInitHandler;
import com.higgs.trust.slave.integration.ca.CaInitClient;
import com.higgs.trust.slave.model.bo.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/5 15:48
 */
@Service @Slf4j public class CaInitServiceImpl implements CaInitService {

    public static final String PUB_KEY = "pubKey";
    public static final String PRI_KEY = "priKey";

    @Autowired private ConfigRepository configRepository;
    @Autowired private NodeState nodeState;
    @Autowired private CaInitClient caClient;
    @Autowired private ClusterInfo clusterInfo;
    @Autowired private CaInitHandler caInitHandler;
    @Value("${bftSmart.systemConfigs.myId:test}") private String myId;

    // TODO 单节点的加入是否也应该和集群初始启动一样，在自检过程中发现没有创世块，自动生成公私钥，然后插入DB？？

    /**
     * @return
     * @desc execute command initStart on one node, it will call each node in the cluster to execute command initKeyPair
     */
    @Override public RespData<String> initStart() {
        List<String> nodeList = clusterInfo.clusterNodeNames();
        nodeList.forEach((nodeName) -> {
            initKeyPair();
        });
        return new RespData<>();
    }

    /**
     * @param
     * @return
     * @desc
     */
    @Override public void initKeyPair() {
        List<String> nodeList = clusterInfo.clusterNodeNames();
        Map<String, String> caMap = new HashMap();
        // acquire all Node's pubKey
        nodeList.forEach((nodeName) -> {
            RespData<String> resp = caClient.caInit(nodeName);
            String pubKey = resp.getData();
            caMap.put(nodeName, pubKey);
        });

        // construct genius block and insert into db
        try {
            caInitHandler.process(caMap);

            writeKyePairToFile(caMap);

        } catch (Throwable e) {
            // TODO 抛出异常？？？
        }

    }

    @Override public RespData<String> initCaTx() {
        // TODO 公私钥的生成会在集群自检时，发现没有创世块，那么就应该生成公私钥

        // acquire pubKey from DB
        String pubKey = configRepository.getConfig(nodeState.getNodeName()).getPubKey();
        return new RespData<>(pubKey);
    }

    /**
     * @param pubKey
     * @return
     * @desc write file
     */
    private void fileWriter(String pubKey, String flag) {
        String path = "config" + System.getProperty("file.separator") + "keys" + System.getProperty("file.separator");
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(path + flag + myId, false));
            w.write(pubKey);
            w.flush();
            w.close();
        } catch (IOException e) {
            log.error("[fileWriter]write pubKey to file error", e);
            throw new SlaveException(SlaveErrorEnum.SLAVE_CA_WRITE_FILE_ERROR,
                "[fileWriter]write pubKey to file error");
        }
    }

    private Date calculatePeriod() {
        Calendar calendar = Calendar.getInstance();
        // default 1 year later
        calendar.add(Calendar.YEAR, 1);
        return calendar.getTime();
    }

    private void writeKyePairToFile(Map<String, String> map) {
        for (String key : map.keySet()) {
            fileWriter(map.get(key), "publickey");
        }
        Config config = configRepository.getConfig(nodeState.getNodeName());
        fileWriter(config.getPriKey(), "privatekey");
    }

}
