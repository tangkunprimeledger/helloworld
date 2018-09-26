package com.higgs.trust.network;

import com.google.common.collect.Sets;
import com.higgs.trust.network.message.DiscoveryPeersRequest;
import com.higgs.trust.network.message.DiscoveryPeersResponse;
import com.higgs.trust.network.utils.Hessian;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.*;

/**
 * @author duhongming
 * @date 2018/9/12
 */
public class DiscoveryPeersService {

    private static final String DISCOVERY_NODES_ACTION = "discoveryNodes";

    private final Logger log = LoggerFactory.getLogger(getClass());
    private Set<Peer> connectedPeers = Sets.newCopyOnWriteArraySet();
    private Set<Peer> unconnectedPeers = Sets.newCopyOnWriteArraySet();

    private final NetworkManage networkManage;
    private final Peers peers;
    private final ExecutorService executorService;
    private final Address localAddress;
    private final ScheduledExecutorService scheduledExecutor;

    private ScheduledFuture<?> timeoutFuture;

    public DiscoveryPeersService(final NetworkManage networkManage, final Peers peers, final ExecutorService executorService) {
        this.networkManage = networkManage;
        this.peers = peers;
        this.executorService = executorService;
        this.localAddress = networkManage.localPeer().getAddress();
        this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

        networkManage.messagingService.registerHandler(DISCOVERY_NODES_ACTION, this::discoveryPeersHandler);
    }

    public void start() {
        timeoutFuture = scheduledExecutor.scheduleAtFixedRate(() -> {
            Set<Peer> allPeers = networkManage.getPeers();
            DiscoveryPeersRequest request = new DiscoveryPeersRequest(allPeers);
            allPeers.forEach(peer -> {
                if (peer.getAddress().equals(localAddress)) {
                    return;
                }
                discoveryPeers(peer, request);
            });
        }, 0, 3, TimeUnit.SECONDS);
    }

    public void shutdown() {
        timeoutFuture.cancel(false);
        scheduledExecutor.shutdown();
    }

    protected void addPeer(Peer peer) {
        if (peer.getAddress().equals(localAddress)) {
            return;
        }

        Peer oldPeer = peers.putIfAbsent(peer);

        if (oldPeer == null || oldPeer.getNonce() < peer.getNonce()) {
            log.debug("Discovered a new peer {}", peer);
            if (oldPeer != null) {
                oldPeer.update(peer);
            }
            scheduledExecutor.execute(() -> discoveryPeers(peer, new DiscoveryPeersRequest(peers.getPeers())));
        }
    }

    private CompletableFuture<byte[]> discoveryPeersHandler(Address address, byte[] requestData) {
        DiscoveryPeersRequest request = Hessian.parse(requestData);
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        executorService.execute(() -> {
            log.trace("Received discovery peers message from {}", address);
            DiscoveryPeersResponse response = new DiscoveryPeersResponse(networkManage.getPeers());
            future.complete(Hessian.serialize(response));
            request.getPeers().forEach(networkManage::addPeer);
        });
        return future;
    }

    private void discoveryPeers(Peer to, DiscoveryPeersRequest request) {
        this.networkManage.<DiscoveryPeersResponse>send(to.getAddress(), DISCOVERY_NODES_ACTION, request).whenComplete((response, error) -> {
            if (error != null) {
                connectedPeers.remove(to);
                unconnectedPeers.add(to);
                log.warn("Peer {} not available, {}", to, error.getMessage());
                return;
            }
            log.debug("Send discovery peers message to {}", to.getAddress());
            response.getPeers().forEach(this.networkManage::addPeer);
            unconnectedPeers.remove(to);
            connectedPeers.add(to);
        });
    }
}
