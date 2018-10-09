package com.higgs.trust.consensus.zk.service;

import com.higgs.trust.consensus.core.ConsensusClient;
import com.higgs.trust.consensus.core.ConsensusCommit;
import com.higgs.trust.consensus.core.ConsensusStateMachine;
import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * @author: zhouyafeng
 * @create: 2018/09/06 16:32
 * @description:
 */
@Component
public class ZKClient implements ConsensusClient,ConsensusStateMachine,InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(ZKClient.class);

    private CuratorFramework curatorFramework;

    @Autowired
    private ZkCommitReplicateComposite zkCommitReplicateComposite;

    Map<Class<?>, Function<ConsensusCommit<?>, ?>> functionMap;

    @Value("${zk.connection.info}")
    private String connectionInfo;

    @Value("${zk.datapath}")
    private String dataPath;

   public void afterPropertiesSet() {
        functionMap = zkCommitReplicateComposite.registerCommit();
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(connectionInfo)
                .connectionTimeoutMs(3000)
                .sessionTimeoutMs(6000)
                .retryPolicy(retryPolicy)
                .build();
        curatorFramework.start();
        log.info("zkClient start");
        listener(curatorFramework);
    }

    public void submitCommand(AbstractConsensusCommand command) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(command);
            if (curatorFramework.checkExists().forPath(dataPath) == null) {
                curatorFramework.create().forPath(dataPath, byteArrayOutputStream.toByteArray());
            } else {
                curatorFramework.setData().forPath(dataPath, byteArrayOutputStream.toByteArray());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void listener(CuratorFramework client) {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        final NodeCache nodeCache = new NodeCache(client, dataPath, false);
        try {
            nodeCache.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                byte[] bytes = nodeCache.getCurrentData().getData();
                ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
                AbstractConsensusCommand command = (AbstractConsensusCommand) objectInputStream.readObject();
                Function function = functionMap.get(command.getClass());
                if (function != null) {
                    while (true) {
                        try {
                            function.apply(command);
                            break;
                        } catch (Throwable e) {
                            log.error("apply error {}", e.getMessage());
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e1) {
                                log.error(e1.getMessage());
                            }
                        }
                    }
                } else {
                    log.error("The corresponding method is not registered.-- {}",
                            command.getClass().getSimpleName());
                }
                System.out.println(command.toString());
            }
        }, executorService);
    }

    @Override
    public <T> CompletableFuture<?> submit(AbstractConsensusCommand<T> command) {
        CompletableFuture completableFuture = CompletableFuture.runAsync(() -> submitCommand(command));
        return completableFuture;
    }

    @Override
    public void start() {

    }

    @Override
    public void leaveConsensus() {

    }

    @Override
    public void joinConsensus() {

    }
}