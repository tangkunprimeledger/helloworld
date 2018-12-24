package com.higgs.trust.network;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.MoreExecutors;
import com.higgs.trust.network.message.*;
import com.higgs.trust.network.utils.Hessian;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.higgs.trust.network.utils.Threads.namedThreads;

/**
 * @author duhongming
 * @date 2018/8/21
 */
public class MessagingService {

    private static final long MAX_TIMEOUT_MILLIS = 5000;
    private static final long TIMEOUT_INTERVAL = 50;
    private static final int CHANNEL_POOL_SIZE = 8;

    private static final byte[] EMPTY_PAYLOAD = new byte[0];

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final LocalClientConnection localClientConnection = new LocalClientConnection();
    private final LocalServerConnection localServerConnection = new LocalServerConnection(null);

    private final NetworkManage networkManage;
    private final NetworkConfig config;
    private final Address localAddress;
    private final Peer localPeer;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final Map<String, BiConsumer<NetworkRequest, ServerConnection>> handlers = new ConcurrentHashMap<>();
    final Map<Channel, RemoteClientConnection> clientConnections = Maps.newConcurrentMap();
    final Map<Channel, RemoteServerConnection> serverConnections = Maps.newConcurrentMap();
    private final AtomicLong messageIdGenerator = new AtomicLong(0);

    private ScheduledFuture<?> timeoutFuture;
    private long maxTimeoutMillis;

    private final Map<Address, List<CompletableFuture<Channel>>> channels = Maps.newConcurrentMap();

    private EventLoopGroup serverGroup;
    EventLoopGroup clientGroup;
    private ScheduledExecutorService timeoutExecutor;
    private NodeServer trustServer;
    private NodeClient trustClient;

    protected MessagingService(final NetworkManage networkManage) {
        this.networkManage = networkManage;
        this.config = networkManage.config();
        this.localPeer = networkManage.config().localPeer();
        this.localAddress = localPeer.getAddress();
        this.maxTimeoutMillis = config.timeout() <= 0 ? MAX_TIMEOUT_MILLIS : config.timeout() * 1000;
    }

    public Address address() {
        return localAddress;
    }

    public Peer peer() {
        return localPeer;
    }

    public CompletableFuture<MessagingService> start() {
        if (started.get()) {
            log.warn("Already running at local address: {}", localAddress);
            return CompletableFuture.completedFuture(this);
        }

        initEventLoopGroup();
        this.trustServer = new NodeServer(localAddress, new InboundMessageDispatcher(), this.serverGroup, this.clientGroup, this.config);
        this.trustClient = new NodeClient(localAddress, new InboundMessageDispatcher(), this.clientGroup, this.config);
        return trustServer.start().thenRun(() -> {
            timeoutExecutor = Executors.newSingleThreadScheduledExecutor(namedThreads("messaging-timeout-%d", log));
            timeoutFuture = timeoutExecutor.scheduleAtFixedRate(this::timeoutAllCallbacks, TIMEOUT_INTERVAL, TIMEOUT_INTERVAL, TimeUnit.MILLISECONDS);
            started.set(true);
            log.info("Started ...");
        }).thenApply(v -> this);
    }

    public boolean isRunning() {
        return started.get();
    }

    private void initEventLoopGroup() {
        clientGroup = new NioEventLoopGroup(config.clientThreadNum(), namedThreads("netty-network-event-nio-client-%d", log));
        serverGroup = new NioEventLoopGroup(0, namedThreads("netty-network-event-nio-server-%d", log));
    }

    /**
     * Times out response callbacks.
     */
    private void timeoutAllCallbacks() {
        localClientConnection.timeoutCallbacks();
        for (RemoteClientConnection connection : clientConnections.values()) {
            connection.timeoutCallbacks();
        }
    }

    public CompletableFuture<Void> sendAsync(Address address, String action, byte[] payload) {
        long id = messageIdGenerator.incrementAndGet();
        NetworkRequest message = new NetworkRequest(id, action, payload).sender(localAddress);
        return executeOnPooledConnection(address, action, c -> c.sendAsync(message), MoreExecutors.directExecutor());
    }

    public CompletableFuture<byte[]> sendAndReceive(Address address, String action, byte[] payload) {
        return sendAndReceive(address, action, payload, null, MoreExecutors.directExecutor());
    }

    public CompletableFuture<byte[]> sendAndReceive(Address address, String action, byte[] payload, Executor executor) {
        return sendAndReceive(address, action, payload, null, executor);
    }

    public CompletableFuture<byte[]> sendAndReceive(Address address, String action, byte[] payload, Duration timeout) {
        return sendAndReceive(address, action, payload, timeout, MoreExecutors.directExecutor());
    }

    public CompletableFuture<byte[]> sendAndReceive(Address address, String action, byte[] payload, Duration timeout, Executor executor) {
        long messageId = messageIdGenerator.incrementAndGet();
        NetworkRequest request = new NetworkRequest(messageId, action, payload).sender(localAddress);
        return executeOnPooledConnection(address, action, c -> c.sendAndReceive(request, timeout), executor);
    }

    private List<CompletableFuture<Channel>> getChannelPool(Address address) {
        List<CompletableFuture<Channel>> channelPool = channels.get(address);
        if (channelPool != null) {
            return channelPool;
        }
        return channels.computeIfAbsent(address, e -> {
            List<CompletableFuture<Channel>> defaultList = new ArrayList<>(CHANNEL_POOL_SIZE);
            for (int i = 0; i < CHANNEL_POOL_SIZE; i++) {
                defaultList.add(null);
            }
            return Lists.newCopyOnWriteArrayList(defaultList);
        });
    }

    private int getChannelOffset(String messageType) {
        return Math.abs(messageType.hashCode() % CHANNEL_POOL_SIZE);
    }

    private CompletableFuture<Channel> getChannel(Address address, String messageType) {
        List<CompletableFuture<Channel>> channelPool = getChannelPool(address);
        int offset = getChannelOffset(messageType);

        CompletableFuture<Channel> channelFuture = channelPool.get(offset);
        if (channelFuture == null || channelFuture.isCompletedExceptionally()) {
            synchronized (channelPool) {
                channelFuture = channelPool.get(offset);
                if (channelFuture == null || channelFuture.isCompletedExceptionally()) {
                    channelFuture = openChannel(address);
                    channelPool.set(offset, channelFuture);
                }
            }
        }

        final CompletableFuture<Channel> future = new CompletableFuture<>();
        final CompletableFuture<Channel> finalFuture = channelFuture;
        finalFuture.whenComplete((channel, error) -> {
            if (error == null) {
                if (!channel.isActive()) {
                    CompletableFuture<Channel> currentFuture;
                    synchronized (channelPool) {
                        currentFuture = channelPool.get(offset);
                        if (currentFuture == finalFuture) {
                            channelPool.set(offset, null);
                        } else if (currentFuture == null) {
                            currentFuture = openChannel(address);
                            channelPool.set(offset, currentFuture);
                        }
                    }

                    final ClientConnection connection = clientConnections.remove(channel);
                    if (connection != null) {
                        connection.close();
                    }

                    if (currentFuture == finalFuture) {
                        getChannel(address, messageType).whenComplete((recursiveResult, recursiveError) -> {
                            if (recursiveError == null) {
                                future.complete(recursiveResult);
                            } else {
                                future.completeExceptionally(recursiveError);
                            }
                        });
                    } else {
                        currentFuture.whenComplete((recursiveResult, recursiveError) -> {
                            if (recursiveError == null) {
                                future.complete(recursiveResult);
                            } else {
                                future.completeExceptionally(recursiveError);
                            }
                        });
                    }
                } else {
                    future.complete(channel);
                }
            } else {
                future.completeExceptionally(error);
            }
        });
        return future;
    }

    private <T> CompletableFuture<T> executeOnPooledConnection(
            Address address,
            String type,
            Function<ClientConnection, CompletableFuture<T>> callback,
            Executor executor) {
        CompletableFuture<T> future = new CompletableFuture<T>();
        executeOnPooledConnection(address, type, callback, executor, future);
        return future;
    }

    private <T> void executeOnPooledConnection(
            Address address,
            String type,
            Function<ClientConnection, CompletableFuture<T>> callback,
            Executor executor,
            CompletableFuture<T> future) {
        if (address.equals(localAddress)) {
            callback.apply(localClientConnection).whenComplete((result, error) -> {
                if (error == null) {
                    executor.execute(() -> future.complete(result));
                } else {
                    executor.execute(() -> future.completeExceptionally(error));
                }
            });
            return;
        }

        getChannel(address, type).whenComplete((channel, channelError) -> {
            if (channelError == null) {
                final ClientConnection connection = getOrCreateRemoteClientConnection(channel);
                callback.apply(connection).whenComplete((result, sendError) -> {
                    if (sendError == null) {
                        executor.execute(() -> future.complete(result));
                    } else {
                        final Throwable cause = Throwables.getRootCause(sendError);
                        if (!(cause instanceof TimeoutException) && !(cause instanceof MessagingException)) {
                            channel.close().addListener(f -> {
                                connection.close();
                                clientConnections.remove(channel);
                            });
                        }
                        executor.execute(() -> future.completeExceptionally(sendError));
                    }
                });
            } else {
                executor.execute(() -> future.completeExceptionally(channelError));
            }
        });
    }

    private RemoteClientConnection getOrCreateRemoteClientConnection(Channel channel) {
        RemoteClientConnection connection = clientConnections.get(channel);
        if (connection == null) {
            connection = clientConnections.computeIfAbsent(channel, RemoteClientConnection::new);
        }
        return connection;
    }

    public void registerHandler(String type, BiConsumer<Address, byte[]> handler, Executor executor) {
        handlers.put(type, (message, connection) -> executor.execute(() ->
            handler.accept(message.sender(), message.payload())));
    }

    public void registerHandler(String type, BiFunction<Address, byte[], byte[]> handler, Executor executor) {
        handlers.put(type, (message, connection) -> executor.execute(() -> {
            byte[] responsePayload = null;
            NetworkResponse.Status status = NetworkResponse.Status.OK;
            try {
                responsePayload = handler.apply(message.sender(), message.payload());
            } catch (Exception e) {
                log.warn("An error occurred in a message handler: {}", e);
                status = NetworkResponse.Status.ERROR_HANDLER_EXCEPTION;
            }
            connection.reply(message, status, Optional.ofNullable(responsePayload));
        }));
    }

    public void registerHandler(String type, BiFunction<Address, byte[], CompletableFuture<byte[]>> handler) {
        handlers.put(type, (request, connection) -> {
            CompletableFuture<byte[]> future = handler.apply(request.sender(), request.payload());
            future.whenComplete((result, error) -> {
                NetworkResponse.Status status;
                if (error == null) {
                    status = NetworkResponse.Status.OK;
                } else {
                    log.warn("An error occurred in a message handler: {}", error);
                    status = NetworkResponse.Status.ERROR_HANDLER_EXCEPTION;
                }
                connection.reply(request, status, Optional.ofNullable(result));
            });
        });
    }

    public void unregisterHandler(String type) {
        handlers.remove(type);
    }

    private CompletableFuture<Channel> openChannel(Address address) {
        return trustClient.connect(address);
    }

    public CompletableFuture<Void> stop() {
        if (started.compareAndSet(true, false)) {
            return CompletableFuture.supplyAsync(() -> {
                boolean interrupted = false;
                try {
                    try {
                        this.trustServer.closeChannel();
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                    Future<?> serverShutdownFuture = serverGroup.shutdownGracefully();
                    Future<?> clientShutdownFuture = clientGroup.shutdownGracefully();
                    try {
                        serverShutdownFuture.sync();
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                    try {
                        clientShutdownFuture.sync();
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                    timeoutFuture.cancel(false);
                    timeoutExecutor.shutdown();
                } finally {
                    log.info("Stopped");
                    if (interrupted) {
                        Thread.currentThread().interrupt();
                    }
                }
                return null;
            });
        }
        return CompletableFuture.completedFuture(null);
    }

    @ChannelHandler.Sharable
    private class InboundMessageDispatcher extends SimpleChannelInboundHandler<Object> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object rawMessage) {
            NetworkMessage message = (NetworkMessage) rawMessage;
            try {
                if (message.isRequest()) {
                    NetworkRequest networkRequest = (NetworkRequest) message;
                    if (NetworkRequest.AUTH_ACTION_NAME.equals(networkRequest.actionName())) {
                        Object req = Hessian.parse(message.payload());
                        if (req instanceof AuthenticationRequest) {
                            AuthenticationRequest request = Hessian.parse(message.payload());
                            Peer newPeer = new Peer(networkRequest.sender(), request.getNodeName(), request.getPublicKey());
                            newPeer.setNonce(request.getNonce());
                            newPeer.setHttpPort(request.getHttpPort());
                            newPeer.setSlave(request.isBackupNode());
                            if (!config.authentication().validate(newPeer, request.getSignature())) {
                                log.warn("authentication fail: {}", newPeer);
                                ctx.writeAndFlush(new NetworkResponse(message.id(), NetworkResponse.Status.UNAUTHORIZED, EMPTY_PAYLOAD))
                                        .addListener(ChannelFutureListener.CLOSE);
                                return;
                            }
                            ConnectionSession session = new ConnectionSession(networkRequest.sender(), ConnectionSession.ChannelType.OUTBOUND);
                            ctx.channel().attr(ConnectionSession.ATTR_KEY_CONNECTION_SESSION).set(session);
                            AuthenticationResponse response = new AuthenticationResponse("OK");
                            response.setPeers(Lists.newArrayList(networkManage.getPeers()));
                            response.setPeer(networkManage.localPeer());

                            networkManage.addPeer(newPeer);
                            networkManage.updatePeerConnected(networkRequest.sender(), true);
                            ctx.writeAndFlush(new NetworkResponse(message.id(), Hessian.serialize(response)));
                        }
                        return;
                    }

                    ConnectionSession session = ctx.channel().attr(ConnectionSession.ATTR_KEY_CONNECTION_SESSION).get();
                    if (session == null) {
                        log.warn("Illegal request from {} ...", ctx.channel().remoteAddress());
                        return;
                    }

                    RemoteServerConnection connection = serverConnections.get(ctx.channel());
                    if (connection == null) {
                        connection = serverConnections.computeIfAbsent(ctx.channel(), RemoteServerConnection::new);
                    }
                    connection.dispatch((NetworkRequest) message);
                } else {
                    // id=0 is AuthenticationResponse
                    if (message.id() == 0) {
                        AuthenticationResponse response = (AuthenticationResponse)Hessian.parse(message.payload());
                        networkManage.addPeer(response.getPeer());
                        return;
                    }
                    RemoteClientConnection connection = getOrCreateRemoteClientConnection(ctx.channel());
                    connection.dispatch((NetworkResponse) message);
                }
            } catch (RejectedExecutionException e) {
                log.warn("Unable to dispatch message due to {}", e.getMessage());
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
            log.error("Exception inside channel handling pipeline.", cause);

            RemoteClientConnection clientConnection = clientConnections.remove(context.channel());
            if (clientConnection != null) {
                clientConnection.close();
            }

            RemoteServerConnection serverConnection = serverConnections.remove(context.channel());
            if (serverConnection != null) {
                serverConnection.close();
            }
            context.close();
        }

        @Override
        public void channelInactive(ChannelHandlerContext context) {
            ConnectionSession session = context.channel().attr(ConnectionSession.ATTR_KEY_CONNECTION_SESSION).get();
            if (session != null) {
                Address address = session.getRemoteAddress();
                log.warn("与节点{}连接断开", address);
                networkManage.notifyListeners(NetworkListener.Event.LEAVE, address);
            }
            RemoteClientConnection clientConnection = clientConnections.remove(context.channel());
            if (clientConnection != null) {
                clientConnection.close();
            }

            RemoteServerConnection serverConnection = serverConnections.remove(context.channel());
            if (serverConnection != null) {
                serverConnection.close();
            }
            context.close();
        }

        @Override
        public final boolean acceptInboundMessage(Object msg) {
            return msg instanceof NetworkMessage;
        }
    }

    /**
     * Represents the client side of a connection to a local or remote server.
     */
    private interface ClientConnection {

        /**
         * Sends a message to the other side of the connection.
         *
         * @param request the message to send
         * @return a completable future to be completed once the message has been sent
         */
        CompletableFuture<Void> sendAsync(NetworkRequest request);

        /**
         * Sends a message to the other side of the connection, awaiting a reply.
         *
         * @param request the message to send
         * @param timeout the response timeout
         * @return a completable future to be completed once a reply is received or the request times out
         */
        CompletableFuture<byte[]> sendAndReceive(NetworkRequest request, Duration timeout);

        /**
         * Closes the connection.
         */
        default void close() {
        }
    }

    /**
     * Represents the server side of a connection.
     */
    private interface ServerConnection {

        /**
         * Sends a reply to the other side of the connection.
         *
         * @param message the message to which to reply
         * @param status  the reply status
         * @param payload the response payload
         */
        void reply(NetworkRequest message, NetworkResponse.Status status, Optional<byte[]> payload);

        /**
         * Closes the connection.
         */
        default void close() {
        }
    }

    /**
     * Remote connection implementation.
     */
    private abstract class AbstractClientConnection implements ClientConnection {
        final Map<Long, Callback> futures = Maps.newConcurrentMap();
        final AtomicBoolean closed = new AtomicBoolean(false);

        /**
         * Times out callbacks for this connection.
         */
        void timeoutCallbacks() {
            // Store the current time.
            long currentTime = System.currentTimeMillis();

            // Iterate through future callbacks and time out callbacks that have been alive
            // longer than the current timeout according to the message type.
            Iterator<Map.Entry<Long, Callback>> iterator = futures.entrySet().iterator();
            while (iterator.hasNext()) {
                Callback callback = iterator.next().getValue();
                long elapsedTime = currentTime - callback.time();

                // If a timeout for the callback was provided and the timeout elapsed, timeout the future but don't
                // record the response time.
                if (callback.timeout() > 0 && elapsedTime > callback.timeout()) {
                    iterator.remove();
                    callback.completeExceptionally(new TimeoutException("Request timed out in " + elapsedTime + " milliseconds"));
                } else {
                    if (callback.timeout() == 0 && elapsedTime > maxTimeoutMillis) {
                        iterator.remove();
                        callback.completeExceptionally(new TimeoutException("Request timed out in " + elapsedTime + " milliseconds"));
                    }
                }
            }
        }

        protected void registerCallback(long id, String subject, Duration timeout, CompletableFuture<byte[]> future) {
            futures.put(id, new Callback(subject, timeout, future));
        }

        protected Callback completeCallback(long id) {
            return futures.remove(id);
        }

        protected Callback failCallback(long id) {
            return futures.remove(id);
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                for (Callback callback : futures.values()) {
                    callback.completeExceptionally(new ConnectException());
                }
            }
        }
    }

    /**
     * Local connection implementation.
     */
    private final class LocalClientConnection extends AbstractClientConnection {
        @Override
        public CompletableFuture<Void> sendAsync(NetworkRequest request) {
            BiConsumer<NetworkRequest, ServerConnection> handler = handlers.get(request.actionName());
            if (handler != null) {
                log.trace("{} - Received message type {} from {}", localAddress, request.actionName(), request.sender());
                handler.accept(request, localServerConnection);
            } else {
                log.debug("{} - No handler for message type {} from {}", localAddress, request.actionName(), request.sender());
            }
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<byte[]> sendAndReceive(NetworkRequest request, Duration timeout) {
            CompletableFuture<byte[]> future = new CompletableFuture<>();
            future.whenComplete((r, e) -> completeCallback(request.id()));
            registerCallback(request.id(), request.actionName(), timeout, future);
            BiConsumer<NetworkRequest, ServerConnection> handler = handlers.get(request.actionName());
            if (handler != null) {
                log.trace("{} - Received message type {} from {}", localAddress, request.actionName(), request.sender());
                handler.accept(request, new LocalServerConnection(future));
            } else {
                log.debug("{} - No handler for message type {} from {}", localAddress, request.actionName(), request.sender());
                new LocalServerConnection(future)
                    .reply(request, NetworkResponse.Status.ERROR_NO_HANDLER, Optional.empty());
            }
            return future;
        }
    }

    /**
     * Local server connection.
     */
    private static final class LocalServerConnection implements ServerConnection {
        private final CompletableFuture<byte[]> future;

        LocalServerConnection(CompletableFuture<byte[]> future) {
            this.future = future;
        }

        @Override
        public void reply(NetworkRequest message, NetworkResponse.Status status, Optional<byte[]> payload) {
            if (future != null) {
                if (status == NetworkResponse.Status.OK) {
                    future.complete(payload.orElse(EMPTY_PAYLOAD));
                } else if (status == NetworkResponse.Status.ERROR_NO_HANDLER) {
                    future.completeExceptionally(new MessagingException.NoRemoteHandler());
                } else if (status == NetworkResponse.Status.ERROR_HANDLER_EXCEPTION) {
                    future.completeExceptionally(new MessagingException.RemoteHandlerFailure());
                } else if (status == NetworkResponse.Status.PROTOCOL_EXCEPTION) {
                    future.completeExceptionally(new MessagingException.ProtocolException());
                }
            }
        }
    }

    /**
     * Remote connection implementation.
     */
    private final class RemoteClientConnection extends AbstractClientConnection {
        private final Channel channel;

        RemoteClientConnection(Channel channel) {
            this.channel = channel;
        }

        @Override
        public CompletableFuture<Void> sendAsync(NetworkRequest request) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            channel.writeAndFlush(request).addListener(channelFuture -> {
                if (!channelFuture.isSuccess()) {
                    future.completeExceptionally(channelFuture.cause());
                } else {
                    future.complete(null);
                }
            });
            return future;
        }

        @Override
        public CompletableFuture<byte[]> sendAndReceive(NetworkRequest request, Duration timeout) {
            CompletableFuture<byte[]> future = new CompletableFuture<>();
            registerCallback(request.id(), request.actionName(), timeout, future);
            channel.writeAndFlush(request).addListener(channelFuture -> {
                if (!channelFuture.isSuccess()) {
                    Callback callback = failCallback(request.id());
                    if (callback != null) {
                        callback.completeExceptionally(channelFuture.cause());
                    }
                }
            });
            return future;
        }

        /**
         * Dispatches a message to a local handler.
         *
         * @param response the message to dispatch
         */
        private void dispatch(NetworkResponse response) {
            Callback callback = completeCallback(response.id());
            if (callback != null) {
                if (response.status() == NetworkResponse.Status.OK) {
                    callback.complete(response.payload());
                } else if (response.status() == NetworkResponse.Status.ERROR_NO_HANDLER) {
                    callback.completeExceptionally(new MessagingException.NoRemoteHandler());
                } else if (response.status() == NetworkResponse.Status.ERROR_HANDLER_EXCEPTION) {
                    callback.completeExceptionally(new MessagingException.RemoteHandlerFailure());
                } else if (response.status() == NetworkResponse.Status.PROTOCOL_EXCEPTION) {
                    callback.completeExceptionally(new MessagingException.ProtocolException());
                }
            } else {
                log.debug("Received a reply for message id:[{}] but was unable to locate the request handle", response.id());
            }
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                for (Callback callback : futures.values()) {
                    callback.completeExceptionally(new ConnectException());
                }
            }
        }
    }

    /**
     * Remote server connection.
     */
    private final class RemoteServerConnection implements ServerConnection {
        private final Channel channel;

        RemoteServerConnection(Channel channel) {
            this.channel = channel;
        }

        /**
         * Dispatches a message to a local handler.
         *
         * @param request the message to dispatch
         */
        private void dispatch(NetworkRequest request) {

            BiConsumer<NetworkRequest, ServerConnection> handler = handlers.get(request.actionName());
            if (handler != null) {
                log.trace("{} - Received message type {} from {}", localAddress, request.actionName(), request.sender());
                handler.accept(request, this);
            } else {
                log.debug("{} - No handler for message type {} from {}", localAddress, request.actionName(), request.sender());
                reply(request, NetworkResponse.Status.ERROR_NO_HANDLER, Optional.empty());
            }
        }

        @Override
        public void reply(NetworkRequest request, NetworkResponse.Status status, Optional<byte[]> payload) {
            NetworkResponse response = new NetworkResponse(request.id(), status, payload.orElse(EMPTY_PAYLOAD));
            channel.writeAndFlush(response, channel.voidPromise());
        }
    }
}
