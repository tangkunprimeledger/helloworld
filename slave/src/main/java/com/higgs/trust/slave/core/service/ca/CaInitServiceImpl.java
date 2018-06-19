package com.higgs.trust.slave.core.service.ca;

import com.higgs.trust.config.node.NodeState;
import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import com.higgs.trust.slave.core.repository.config.ConfigRepository;
import com.higgs.trust.slave.core.service.action.ca.CaInitHandler;
import com.higgs.trust.slave.integration.ca.CaInitClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

        Map caMap = acquirePubKeys(100);

        // construct genius block and insert into db
        try {
            log.info("[CaInitServiceImpl.initKeyPair] start to generate genius block");
            caInitHandler.process(caMap);
            log.info("[CaInitServiceImpl.initKeyPair] end generate genius block");

            //            writeKyePairToFile(caMap);
            log.info("[CaInitServiceImpl.initKeyPair] end write all nodes' pubKey to file");

        } catch (Throwable e) {
            log.error("[CaInitServiceImpl.initKeyPair] cluster init CA error", e);
            throw new SlaveException(SlaveErrorEnum.SLAVE_CA_INIT_ERROR,
                "[CaInitServiceImpl.initKeyPair] cluster init CA error", e);
        }

    }

    private Map acquirePubKeys(int retryCount) {
        List<String> nodeList = clusterInfo.clusterNodeNames();
        Map<String, String> caMap = new HashMap();
        log.info("[CaInitServiceImpl.acquirePubKeys] start to acquire all nodes' pubKey, nodeList size = {}",
            nodeList.size());

        int i = 0;
        do {
            if (caMap.size() == nodeList.size()) {
                log.info("[CaInitServiceImpl.acquirePubKeys]  acquired all nodes' pubKey, caMap size = {}",
                    caMap.size());
                return caMap;
            }
            CountDownLatch countDownLatch = new CountDownLatch(nodeList.size());
            // acquire all nodes' pubKey
            nodeList.forEach((nodeName) -> {
                try {
                    RespData<String> resp = caClient.caInit(nodeName);

                    String pubKey = resp.getData();
                    caMap.put(nodeName, pubKey);
                } catch (Throwable e) {
                    log.error("[CaInitServiceImpl.acquirePubKeys] acquire pubKey error", e);
                } finally {
                    countDownLatch.countDown();
                }
            });

            try {
                countDownLatch.await(1 * 60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("send count down latch is interrupted", e);
            }

            try {
                Thread.sleep(3 * 1000);
            } catch (InterruptedException e) {
                log.warn("acquire pubKey error.", e);
            }

        } while (++i < retryCount);

        log.info("[CaInitServiceImpl.acquirePubKeys]  end acquire all nodes' pubKey, caMap size = {}", caMap.size());
        return caMap;
    }

    @Override public RespData<String> initCaTx() {
        // TODO 公私钥的生成会在集群自检时，发现没有创世块，那么就应该生成公私钥

        // acquire pubKey from DB
        String pubKey = configRepository.getConfig(nodeState.getNodeName()).getPubKey();
        log.info("[CaInitServiceImpl.initCaTx] nodeName={}, pubKey={}", nodeState.getNodeName(), pubKey);
        RespData resp = new RespData();
        resp.setData(pubKey);
        return resp;
    }

}
