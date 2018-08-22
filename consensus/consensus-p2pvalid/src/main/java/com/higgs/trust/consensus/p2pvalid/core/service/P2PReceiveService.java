package com.higgs.trust.consensus.p2pvalid.core.service;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.higgs.trust.common.enums.MonitorTargetEnum;
import com.higgs.trust.common.utils.BeanConvertor;
import com.higgs.trust.common.utils.MonitorLogUtils;
import com.higgs.trust.common.utils.SignUtils;
import com.higgs.trust.config.p2p.ClusterInfo;
import com.higgs.trust.consensus.config.NodeState;
import com.higgs.trust.consensus.config.NodeStateEnum;
import com.higgs.trust.consensus.p2pvalid.core.P2PValidCommit;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.ValidConsensus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;

/**
 * @author liuyu
 * @description
 * @date 2018-08-20
 */
@Component @Slf4j public class P2PReceiveService {
    @Autowired private NodeState nodeState;
    @Autowired private ClusterInfo clusterInfo;
    @Autowired private ValidConsensus validConsensus;
    /**
     * store max received command number
     */
    @Value("${p2p.receive.maxCommandNum:5000}") int maxCommandNum;
    @Value("${p2p.receive.retryNum:10}") int retryNum;

    /**
     * store received command
     */
    private ConcurrentLinkedHashMap<String, ConcurrentHashMap<String, ValidCommandWrap>> receivedCommand =
        new ConcurrentLinkedHashMap.Builder<String, ConcurrentHashMap<String, ValidCommandWrap>>()
            .maximumWeightedCapacity(maxCommandNum)
            .listener((key, value) -> log.info("Evicted key:{},value:{}", key, value)).build();

    /**
     * store executed commands
     */
    private ConcurrentLinkedHashMap<String, Integer> executedCommand =
        new ConcurrentLinkedHashMap.Builder<String, Integer>().maximumWeightedCapacity(maxCommandNum)
            .listener((key, value) -> log.info("Evicted key:{},value:{}", key, value)).build();

    private ExecutorService receivedExecutorService;

    @PostConstruct public void initThreadPool() {
        receivedExecutorService =
            new ThreadPoolExecutor(5, 10, 3600, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1024), (r) -> {
                Thread thread = new Thread(r);
                thread.setName("command receive thread executor");
                thread.setDaemon(true);
                return thread;
            });
    }

    /**
     * process received command
     *
     * @param validCommandWrap
     */
    public void receive(ValidCommandWrap validCommandWrap) {
        log.debug("command receive : {}", validCommandWrap);
        if (!nodeState.isState(NodeStateEnum.Running)) {
            throw new RuntimeException(String.format("the node state is not running, please try again latter"));
        }
        String messageDigest = validCommandWrap.getValidCommand().getMessageDigestHash();
        String pubKey = clusterInfo.pubKey(validCommandWrap.getFromNode());
        if (!SignUtils.verify(messageDigest, validCommandWrap.getSign(), pubKey)) {
            throw new RuntimeException(String
                .format("check sign failed for node %s, validCommandWrap %s, pubKey %s", validCommandWrap.getFromNode(),
                    validCommandWrap, pubKey));
        }
        log.debug("command message digest:{}", validCommandWrap.getValidCommand().getMessageDigestHash());
        String fromNode = validCommandWrap.getFromNode();
        ConcurrentHashMap<String, ValidCommandWrap> _new = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, ValidCommandWrap> _old = receivedCommand.putIfAbsent(messageDigest, _new);
        //add command to memory for first
        if (_old == null) {
            _new.put(fromNode, validCommandWrap);
            return;
        }
        //add command to memory
        _old.put(fromNode, validCommandWrap);
        //check threshold
        int applyThreshold = Math.min(clusterInfo.faultNodeNum() * 2 + 1, clusterInfo.clusterNodeNames().size());
        if (_old.size() < applyThreshold) {
            log.debug("command.size is less than applyThreshold:{}", applyThreshold);
            return;
        }
        Integer v = executedCommand.putIfAbsent(messageDigest,0);
        if (v != null) {
            log.debug("command is already executed");
            return;
        }
        ValidCommandWrap o = BeanConvertor.convertBean(validCommandWrap, ValidCommandWrap.class);
        receivedExecutorService.execute(() -> {
            P2PValidCommit validCommit = new P2PValidCommit(o.getValidCommand());
            int num = 0;
            do {
                try {
                    validConsensus.getValidExecutor().execute(validCommit);
                } catch (Throwable t) {
                    log.error("execute validCommit:{} has error:{}", validCommit, t);
                }
                if (validCommit.isClosed()) {
                    log.info("execute validCommit:{} is success", validCommit);
                    break;
                }
                try {
                    Thread.sleep(100L + 100 * num);
                } catch (InterruptedException e) {
                    log.error("has InterruptedException", e);
                }
            } while (++num < retryNum);
            //make offline
            if (!validCommit.isClosed()) {
                log.warn("execute validCommit:{} is fail,so change state to offline", validCommit);
                nodeState.changeState(nodeState.getState(), NodeStateEnum.Offline);
                MonitorLogUtils.logIntMonitorInfo(MonitorTargetEnum.SLAVE_PACKAGE_PROCESS_ERROR.getMonitorTarget(), 1);
            }
        });
    }
}
