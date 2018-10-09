package com.higgs.trust.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.higgs.trust.network.utils.Hessian;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author duhongming
 * @date 2018/8/30
 */
public class NetworkManage {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final AtomicBoolean started = new AtomicBoolean(false);

    protected static NetworkManage instance;

    private final Peers peers = new Peers();
    private final NetworkConfig config;
    private final Address localAddress;
    protected final MessagingService messagingService;
    private final DiscoveryPeersService discoveryPeersService;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    final Set<Peer> connectedPeers = Sets.newCopyOnWriteArraySet();
    final Set<Peer> unconnectedPeers = Sets.newCopyOnWriteArraySet();
    private List<NetworkListener> listeners;
    private HttpClient httpClient;
    private RpcClient rpcClient;

    public NetworkManage(final NetworkConfig config) {
        if (config.isSingleton()) {
            instance = this;
        }

        this.config = config;
        localAddress = config.localPeer().getAddress();
        List<Peer> seeds = Lists.newArrayList();
        config.seeds().forEach(s -> seeds.add(new Peer(s)));
        this.peers.init(localAddress, seeds, config);

        listeners = new ArrayList<>();

        messagingService = new MessagingService(this);
        discoveryPeersService = new DiscoveryPeersService(this, this.peers, executor);
        this.httpClient = new HttpClient(this);
        this.rpcClient = new RpcClient(this);
        this.initDefaultListener();
    }

    /**
     * Get the instance of NetworkManage if config singleton is true
     * @return
     */
    public static NetworkManage getInstance() {
        return instance;
    }

    private void initDefaultListener() {
        listeners.add(((event, message) -> {
            if (event == NetworkListener.Event.LEAVE) {
                Address address = (Address) message;
                Peer peer = peers.getByAddress(address);

                if (peer != null) {
                    connectedPeers.remove(peer);
                    unconnectedPeers.add(peer);
                }
                log.info("Peer {} disconnected", message);;
            }
        }));
    }

    public Set<Peer> getPeers() {
        return peers.getPeers();
    }

    public void updatePeerConnected(Address address, boolean connected) {
        this.peers.updatePeerConnected(address, connected);
    }

    public Address getAddress(String nodeName) {
        return peers.getAddress(nodeName);
    }

    public HttpClient httpClient() {
        return httpClient;
    }

    public RpcClient rpcClient() {
        return rpcClient;
    }

    public Peer getPeerByName(String nodeName) {
        return peers.getPeer(nodeName);
    }

    public void addListener(NetworkListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    protected void notifyListeners(NetworkListener.Event event, Object message) {
        synchronized (listeners) {
            if (listeners.size() == 0) {
                return;
            }
            for (NetworkListener listener : listeners) {
                messagingService.clientGroup.execute(() -> listener.handle(event, message));
            }
        }
    }

    public void start() {

        if (started.get()) {
            log.warn("Network already running ...");
            return;
        }
        started.set(true);

        messagingService.start().whenComplete((messagingService, error) -> {
            if (error != null) {
                log.error("Start net server failed, {}", error.getMessage());
                return;
            }

            this.discoveryPeersService.start();
            log.info("Network started on {}", localAddress);
        });
    }

    public void shutdown() {
        if (!started.get()) {
            log.warn("Network can't shutdown, it's not started ...");
            return;
        }

        messagingService.stop();
        this.discoveryPeersService.shutdown();
        started.set(false);
    }

    public NetworkConfig config() {
        return this.config;
    }

    protected void addPeer(Peer peer) {
        this.discoveryPeersService.addPeer(peer);
    }

    public Peer localPeer() {
        return peers.localPeer;
    }

    public <T> CompletableFuture<T> send(Address to, String action, Object request) {
        CompletableFuture<T> future = new CompletableFuture<>();
        messagingService.sendAndReceive(to, action, Hessian.serialize(request)).whenComplete((data, error) -> {
            if (error != null) {
                future.completeExceptionally(error);
                return;
            }
            T response = Hessian.parse(data);
            future.complete(response);
        });
        return future;
    }

    public <T> void registerHandler(String type, Consumer<T> handler, Executor executor) {
        messagingService.registerHandler(type, (address, payload) -> {
            handler.accept(Hessian.parse(payload));
        }, executor);
    }

    public <T, R> void registerHandler(String type, Function<T, R> handler, Executor executor) {
        messagingService.registerHandler(type, (address, payload) -> {
             R ret = handler.apply((T)Hessian.parse(payload));
             return Hessian.serialize(ret);
        }, executor);
    }

    public <T, R> void registerHandler(String type, Function<T, R> handler) {
        messagingService.registerHandler(type, (address, payload) -> {
            R ret = handler.apply((T)Hessian.parse(payload));
            return Hessian.serialize(ret);
        }, executor);
    }

    public <T, R> void registerCompletableFutureHandler(String type, Function<T, CompletableFuture<R>> handler) {
        messagingService.registerHandler(type, (address, payload) -> {
            CompletableFuture<R> resultFuture = handler.apply(Hessian.parse(payload));
            CompletableFuture<byte[]> future = new CompletableFuture<>();
            resultFuture.whenComplete((result, error) -> {
                if (error != null) {
                    future.completeExceptionally(error);
                } else {
                    future.complete(Hessian.serialize(result));
                }
            });
            return future;
        });
    }
}
