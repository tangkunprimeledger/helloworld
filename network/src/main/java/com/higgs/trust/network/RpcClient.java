package com.higgs.trust.network;

import com.higgs.trust.network.utils.Hessian;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author duhongming
 * @date 2018/9/19
 */
public class RpcClient {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Random random;

    private NetworkManage networkManage;

    public RpcClient(NetworkManage networkManage) {
        this.random = new Random();
        this.networkManage = networkManage;
    }

    public <T> CompletableFuture<T> sendAndReceiveAsync(Address to, String action, Object message) {
        return sendAndReceiveAsync(to, action, message, null);
    }

    public <T> CompletableFuture<T> sendAndReceiveAsync(Address to, String action, Object message, Duration timeout) {
        log.debug("Send to {}, {}", action, to);
        CompletableFuture<T> future = new CompletableFuture<>();
        networkManage.messagingService.sendAndReceive(to, action, Hessian.serialize(message), timeout).whenComplete((data, error) -> {
            if (error != null) {
                future.completeExceptionally(error);
                return;
            }
            T response = Hessian.parse(data);
            future.complete(response);
        });
        return future;
    }

    public <T> CompletableFuture<T> sendAndReceiveAsync(String toNodeName, String action, Object message) {
        return sendAndReceiveAsync(getAddressByName(toNodeName), action, message, null);
    }

    public <T> CompletableFuture<T> sendAndReceiveAsync(String toNodeName, String action, Object message, Duration timeout) {
        return sendAndReceiveAsync(getAddressByName(toNodeName), action, message, timeout);
    }

    public <T> T sendAndReceive(String toNodeName, String action, Object message) {
        return sendAndReceive(toNodeName, action, message, null);
    }

    public <T> T sendAndReceive(String toNodeName, String action, Object message, Duration timeout) {
        CompletableFuture<T> future = sendAndReceiveAsync(toNodeName, action, message, timeout);
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T randomSendAndReceive(List<String> nodes, String action, Object message) {
        String nodeName = getRandomNodeName(nodes);
        return sendAndReceive(nodeName, action, message, null);
    }

    public <T> T randomSendAndReceive(List<String> nodes, String action, Object message, Duration timeout) {
        String nodeName = getRandomNodeName(nodes);
        return sendAndReceive(nodeName, action, message, timeout);
    }


    private Address getAddressByName(String nodeName) {
        Address address = networkManage.getAddress(nodeName);
        return address;
    }

    private String getRandomNodeName(List<String> nodeNames) {
        String localName = networkManage.localPeer().getNodeName();
        nodeNames.remove(localName);
        String nodeName = nodeNames.get(random.nextInt(nodeNames.size()));
        return nodeName;
    }
}
