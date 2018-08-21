package com.higgs.trust.consensus.p2pvalid.core.service;

import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.consensus.p2pvalid.api.P2pConsensusClient;
import com.higgs.trust.consensus.p2pvalid.core.ResponseCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.ValidResponseWrap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author liuyu
 * @description
 * @date 2018-08-20
 */
@Component @Slf4j public class P2PSendService {

    @Autowired private NodeState nodeState;
    @Autowired private ClusterInfo clusterInfo;
    @Autowired private P2pConsensusClient p2pConsensusClient;

    @Value("${p2p.send.retryNum:3}") int retryNum;

    private ExecutorService sendExecutorService;

    @PostConstruct public void initThreadPool() {
        sendExecutorService =
            new ThreadPoolExecutor(10, 50, 3600, TimeUnit.SECONDS, new LinkedBlockingQueue<>(5000), (r) -> {
                Thread thread = new Thread(r);
                thread.setName("command send thread executor");
                thread.setDaemon(true);
                return thread;
            });
    }

    /**
     * submit command to p2p layer
     *
     * @param validCommand
     */
    public void submit(ValidCommand<?> validCommand) {
        if (!nodeState.isState(NodeStateEnum.Running)) {
            throw new RuntimeException(String.format("the node state is not running, please try again latter"));
        }
        ValidCommandWrap validCommandWrap = new ValidCommandWrap();
        validCommandWrap.setCommandClass(validCommand.getClass());
        validCommandWrap.setFromNode(nodeState.getNodeName());
        validCommandWrap.setSign(SignUtils.sign(validCommand.getMessageDigestHash(), clusterInfo.privateKey()));
        validCommandWrap.setValidCommand(validCommand);

        clusterInfo.clusterNodeNames().forEach((toNodeName) -> {
            sendExecutorService.execute(() -> {
                execute(toNodeName, validCommandWrap);
            });
        });
    }

    /**
     * execute
     *
     * @param toNodeName
     * @param validCommandWrap
     */
    private void execute(String toNodeName, ValidCommandWrap validCommandWrap) {
        int i = 0;
        do {
            try {
                Profiler.start("start send p2p command");
                Profiler.enter("send p2p command");
                ValidResponseWrap<? extends ResponseCommand> sendValidResponse =
                    p2pConsensusClient.send(toNodeName, validCommandWrap);
                if (sendValidResponse.isSucess()) {
                    log.info("send command to node:{} success", toNodeName);
                    break;
                } else {
                    log.error("send command to node:{} failed {}, response:{} ", toNodeName, sendValidResponse);
                }
            } catch (Throwable t) {
                log.error("send to node:{},command:{},error {}", toNodeName, validCommandWrap.getValidCommand(), t);
            } finally {
                Profiler.release();
            }
        } while (++i < retryNum);
    }
}
