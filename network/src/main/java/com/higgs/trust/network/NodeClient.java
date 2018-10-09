package com.higgs.trust.network;

import com.higgs.trust.network.codec.MessageDecoder;
import com.higgs.trust.network.codec.MessageEncoder;
import com.higgs.trust.network.handler.AuthenticationHandler;
import com.higgs.trust.network.message.AuthenticationRequest;
import com.higgs.trust.network.message.NetworkRequest;
import com.higgs.trust.network.utils.Hessian;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * @author duhongming
 * @date 2018/9/5
 */
public class NodeClient {

    protected static final AttributeKey<String> channelAttrKey = AttributeKey.newInstance("session");

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final EventLoopGroup clientGroup;
    private final NetworkConfig config;
    private final Address localAddress;
    private final ChannelHandler messageDispatcher;

    public NodeClient(final Address localAddress, final ChannelHandler messageDispatcher,
                      final EventLoopGroup clientGroup, final NetworkConfig config) {
        this.clientGroup = clientGroup;
        this.config = config;
        this.localAddress = localAddress;
        this.messageDispatcher = messageDispatcher;
    }

    private Bootstrap bootstrapClient(Address address) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK,
                new WriteBufferWaterMark(10 * 32 * 1024, 10 * 64 * 1024));
        bootstrap.option(ChannelOption.SO_RCVBUF, 1024 * 1024);
        bootstrap.option(ChannelOption.SO_SNDBUF, 1024 * 1024);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000);
        bootstrap.group(clientGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.remoteAddress(address.getHost(), address.getPort());
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel channel) {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast("encoder", new MessageEncoder(localAddress));
                pipeline.addLast("decoder", new MessageDecoder());
                pipeline.addLast("auth", new AuthenticationHandler(false));
                pipeline.addLast("messageHandler", messageDispatcher);
            }
        });
        return bootstrap;
    }

    public CompletableFuture<Channel> connect(Address address) {
        CompletableFuture<Channel> retFuture = new CompletableFuture<>();
        Bootstrap bootstrap = bootstrapClient(address);
        ChannelFuture f = bootstrap.connect();
        f.addListener(future -> {
            if (future.isSuccess()) {
                log.debug("Established a new connection to {}", address);
                f.channel().attr(channelAttrKey).set("");
                ConnectionSession session = new ConnectionSession(address ,ConnectionSession.ChannelType.OUTBOUND);
                f.channel().attr(ConnectionSession.ATTR_KEY_CONNECTION_SESSION).set(session);

                Peer local = config.localPeer();
                AuthenticationRequest authenticationRequest = new AuthenticationRequest(config.nodeName(),
                        config.publicKey(), local.getNonce(), local.getHttpPort(), config.signature());
                NetworkRequest request = new NetworkRequest(0, NetworkRequest.AUTH_ACTION_NAME, Hessian.serialize(authenticationRequest));
                f.channel().writeAndFlush(request);
                retFuture.complete(f.channel());
            } else {
                log.debug("Connection to {} error {}", address, future.cause().getMessage());
                retFuture.completeExceptionally(future.cause());
            }
        });
        return retFuture;
    }
}