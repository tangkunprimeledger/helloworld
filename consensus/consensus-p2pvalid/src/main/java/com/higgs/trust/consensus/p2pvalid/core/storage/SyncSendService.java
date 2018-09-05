package com.higgs.trust.consensus.p2pvalid.core.storage;

import com.higgs.trust.config.crypto.CryptoUtil;
import com.higgs.trust.consensus.p2pvalid.core.ResponseCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.ValidResponseWrap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component @Slf4j public class SyncSendService extends BaseSendService {

    private ExecutorService sendExecutorService =
        new ThreadPoolExecutor(4, 10, 3600, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100), (r) -> {
            Thread thread = new Thread(r);
            thread.setName("sync command send thread");
            thread.setDaemon(true);
            return thread;
        });

    @Override public <T extends ResponseCommand> T send(ValidCommand<?> validCommand) {
        ConcurrentHashMap<String, CommandCounter<T>> resultMap = sendAndHandle(validCommand);
        for (Map.Entry<String, CommandCounter<T>> entry : resultMap.entrySet()) {
            CommandCounter<T> value = entry.getValue();
            if (value.getCounter().get() >= (2 * clusterInfo.faultNodeNum() + 1)) {
                return value.getCommand();
            }
        }
        return null;
    }

    private <T extends ResponseCommand> ConcurrentHashMap<String, CommandCounter<T>> sendAndHandle(
        ValidCommand<?> validCommand) {
        if (log.isDebugEnabled()) {
            log.debug("sync send command {}", validCommand);
        }
        ValidCommandWrap validCommandWrap = new ValidCommandWrap();
        validCommandWrap.setCommandClass(validCommand.getClass());
        validCommandWrap.setFromNode(clusterInfo.nodeName());
        validCommandWrap.setSign(
            CryptoUtil.getProtocolCrypto().sign(validCommand.getMessageDigestHash(), clusterInfo.priKeyForConsensus()));
        validCommandWrap.setValidCommand(validCommand);
        ConcurrentHashMap<String, CommandCounter<T>> resultMap = new ConcurrentHashMap<>();
        List<String> nodeNames = clusterInfo.clusterNodeNames();
        CountDownLatch countDownLatch = new CountDownLatch(nodeNames.size());
        nodeNames.forEach((nodeName) -> {
            sendExecutorService.submit(() -> {
                try {
                    log.info("sync send command to node {} ", nodeName);
                    ValidResponseWrap<? extends ResponseCommand> validResponseWrap =
                        p2pConsensusClient.syncSend(nodeName, validCommandWrap);
                    Object result = validResponseWrap.result();
                    if (result != null) {
                        if (result instanceof ResponseCommand) {
                            fetchCommand(resultMap, (ResponseCommand)result);
                        } else if (result instanceof List) {
                            List<ResponseCommand> commands = (List)result;
                            for (ResponseCommand command : commands) {
                                fetchCommand(resultMap, command);
                            }
                        }
                    }
                } catch (Throwable throwable) {
                    log.error("submit p2p sync command failed!", throwable);
                } finally {
                    countDownLatch.countDown();
                }
            });
        });

        try {
            countDownLatch.await(1 * 60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("send count down latch is interrupted", e);
        }
        return resultMap;
    }

    private <T extends ResponseCommand> void fetchCommand(ConcurrentHashMap<String, CommandCounter<T>> resultMap,
        ResponseCommand command) {
        String key = command.getMessageDigestHash();
        CommandCounter counter = resultMap.get(key);
        if (counter == null) {
            CommandCounter commandCounter = resultMap.putIfAbsent(key, new CommandCounter(command));
            if (commandCounter == null) {
                counter = resultMap.get(key);
            } else {
                counter = commandCounter;
            }
        }
        counter.incrementAndGet();
    }

    @Getter class CommandCounter<T extends ResponseCommand> {
        T command;
        AtomicInteger counter = new AtomicInteger(0);

        CommandCounter(T command) {
            this.command = command;
        }

        int incrementAndGet() {
            return counter.incrementAndGet();
        }
    }
}
