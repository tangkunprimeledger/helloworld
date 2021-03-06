/**
 * Copyright (c) 2007-2013 Alysson Bessani, Eduardo Alchieri, Paulo Sousa, and the authors indicated in the @author tags
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bftsmart.communication.client.netty;

import bftsmart.communication.client.CommunicationSystemClientSide;
import bftsmart.communication.client.ReplyReceiver;
import bftsmart.reconfiguration.ClientViewController;
import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.util.Logger;
import bftsmart.tom.util.TOMUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GenericFutureListener;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

/**
 * @author Paulo
 */
@Sharable public class NettyClientServerCommunicationSystemClientSide extends SimpleChannelInboundHandler<TOMMessage>
    implements CommunicationSystemClientSide {

    private int clientId;
    protected ReplyReceiver trr;
    //******* EDUARDO BEGIN **************//
    private ClientViewController controller;
    //******* EDUARDO END **************//
    private Map<Integer, NettyClientServerSession> sessionTable = new HashMap<>();
    private ReentrantReadWriteLock rl;
    //the signature engine used in the system
    private Signature signatureEngine;
    private int signatureLength;
    private boolean closed = false;

    private EventLoopGroup workerGroup;

    private SyncListener listener;

    public NettyClientServerCommunicationSystemClientSide(int clientId, ClientViewController controller) {
        super();
        this.clientId = clientId;
        this.workerGroup = new NioEventLoopGroup();
        try {
            SecretKeyFactory fac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");

            this.controller = controller;
            this.listener = new SyncListener();

            //this.st = new Storage(BENCHMARK_PERIOD);
            this.rl = new ReentrantReadWriteLock();
            signatureLength = TOMUtil.getSignatureSize(controller);

            ChannelFuture future = null;
            int[] currV = controller.getCurrentViewProcesses();
            for (int i = 0; i < currV.length; i++) {
                try {

                    String str = this.clientId + ":" + currV[i];
                    PBEKeySpec spec = new PBEKeySpec(str.toCharArray());
                    SecretKey authKey = fac.generateSecret(spec);

                    //EventLoopGroup workerGroup = new NioEventLoopGroup();

                    //try {
                    Bootstrap b = new Bootstrap();
                    b.group(workerGroup);
                    b.channel(NioSocketChannel.class);
                    b.option(ChannelOption.SO_KEEPALIVE, true);
                    b.option(ChannelOption.TCP_NODELAY, true);
                    b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);

                    b.handler(getChannelInitializer());

                    // Start the client.
                    future = b.connect(controller.getRemoteAddress(currV[i]));

                    //******* EDUARDO BEGIN **************//

                    //creates MAC stuff
                    Mac macSend = Mac.getInstance(controller.getStaticConf().getHmacAlgorithm());
                    macSend.init(authKey);
                    Mac macReceive = Mac.getInstance(controller.getStaticConf().getHmacAlgorithm());
                    macReceive.init(authKey);
                    NettyClientServerSession cs =
                        new NettyClientServerSession(future.channel(), macSend, macReceive, currV[i]);
                    sessionTable.put(currV[i], cs);

                    Logger
                        .println("Connecting to replica " + currV[i] + " at " + controller.getRemoteAddress(currV[i]));
                    //******* EDUARDO END **************//

                    future.awaitUninterruptibly();

                    if (!future.isSuccess()) {
                        System.err.println("Impossible to connect to " + currV[i]);
                    }

                } catch (NullPointerException ex) {
                    //What the fuck is this??? This is not possible!!!
                    Logger.printError("Should fix the problem, and I think it has no other implications :-), "
                        + "but we must make the servers store the view in a different place.", ex);
                } catch (InvalidKeyException ex) {
                    Logger.printError(ex.getMessage(), ex);
                } catch (Exception ex) {
                    Logger.printError(ex.getMessage(), ex);
                }
            }
        } catch (NoSuchAlgorithmException ex) {
            Logger.printError(ex.getMessage(), ex);
        }
    }

    @Override public void updateConnections() {
        int[] currV = controller.getCurrentViewProcesses();
        try {
            //open connections with new servers
            for (int i = 0; i < currV.length; i++) {
                rl.readLock().lock();
                if (sessionTable.get(currV[i]) == null) {

                    rl.readLock().unlock();
                    rl.writeLock().lock();

                    SecretKeyFactory fac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
                    try {
                        // Configure the client.

                        //EventLoopGroup workerGroup = new NioEventLoopGroup();
                        if (workerGroup == null) {
                            workerGroup = new NioEventLoopGroup();
                        }

                        //try {
                        Bootstrap b = new Bootstrap();
                        b.group(workerGroup);
                        b.channel(NioSocketChannel.class);
                        b.option(ChannelOption.SO_KEEPALIVE, true);
                        b.option(ChannelOption.TCP_NODELAY, true);
                        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);

                        b.handler(getChannelInitializer());

                        // Start the client.
                        ChannelFuture future = b.connect(controller.getRemoteAddress(currV[i]));

                        String str = this.clientId + ":" + currV[i];
                        PBEKeySpec spec = new PBEKeySpec(str.toCharArray());
                        SecretKey authKey = fac.generateSecret(spec);

                        //creates MAC stuff
                        Mac macSend = Mac.getInstance(controller.getStaticConf().getHmacAlgorithm());
                        macSend.init(authKey);
                        Mac macReceive = Mac.getInstance(controller.getStaticConf().getHmacAlgorithm());
                        macReceive.init(authKey);
                        NettyClientServerSession cs =
                            new NettyClientServerSession(future.channel(), macSend, macReceive, currV[i]);
                        sessionTable.put(currV[i], cs);

                        Logger.println(
                            "Connecting to replica " + currV[i] + " at " + controller.getRemoteAddress(currV[i]));
                        //******* EDUARDO END **************//

                        future.awaitUninterruptibly();

                        if (!future.isSuccess()) {
                            Logger.printError("Impossible to connect to " + currV[i]);
                        }

                    } catch (InvalidKeyException ex) {
                        Logger.printError(ex.getMessage(), ex);
                    } catch (InvalidKeySpecException ex) {
                        Logger.printError(ex.getMessage(), ex);
                    }
                    rl.writeLock().unlock();
                } else {
                    rl.readLock().unlock();
                }
            }
        } catch (NoSuchAlgorithmException ex) {
            Logger.printError(ex.getMessage(), ex);
        }
    }

    @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof ClosedChannelException) {
            Logger.println("Connection with replica closed.");
        } else if (cause instanceof ConnectException) {
            Logger.println("Impossible to connect to replica.");
        } else {
            Logger.println("Replica disconnected.");
        }
        Logger.printError(cause.getMessage(), cause);
    }

    @Override public void channelRead0(ChannelHandlerContext ctx, TOMMessage sm) throws Exception {

        if (closed) {

            closeChannelAndEventLoop(ctx.channel());

            return;
        }
        trr.replyReceived(sm);
    }

    @Override public void channelActive(ChannelHandlerContext ctx) {

        if (closed) {

            closeChannelAndEventLoop(ctx.channel());

            return;
        }
        Logger.println("Channel active");
    }

    public void reconnect(final ChannelHandlerContext ctx) {

        rl.writeLock().lock();
        Logger.println("try to reconnect");

        //Iterator sessions = sessionTable.values().iterator();

        ArrayList<NettyClientServerSession> sessions = new ArrayList<NettyClientServerSession>(sessionTable.values());
        for (NettyClientServerSession ncss : sessions) {
            if (ncss.getChannel() == ctx.channel()) {
                try {
                    // Configure the client.
                    //EventLoopGroup workerGroup = ctx.channel().eventLoop();
                    if (workerGroup == null) {
                        workerGroup = new NioEventLoopGroup();
                    }

                    //try {
                    Bootstrap b = new Bootstrap();
                    b.group(workerGroup);
                    b.channel(NioSocketChannel.class);
                    b.option(ChannelOption.SO_KEEPALIVE, true);
                    b.option(ChannelOption.TCP_NODELAY, true);
                    b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);

                    b.handler(getChannelInitializer());

                    if (controller.getRemoteAddress(ncss.getReplicaId()) != null) {

                        ChannelFuture future = b.connect(controller.getRemoteAddress(ncss.getReplicaId()));

                        //creates MAC stuff
                        Mac macSend = ncss.getMacSend();
                        Mac macReceive = ncss.getMacReceive();
                        NettyClientServerSession cs =
                            new NettyClientServerSession(future.channel(), macSend, macReceive, ncss.getReplicaId());
                        sessionTable.remove(ncss.getReplicaId());
                        sessionTable.put(ncss.getReplicaId(), cs);

                        Logger.println("re-connecting to replica " + ncss.getReplicaId() + " at " + controller
                            .getRemoteAddress(ncss.getReplicaId()));
                    } else {
                        // This cleans an olde server from the session table
                        sessionTable.remove(ncss.getReplicaId());
                    }
                } catch (NoSuchAlgorithmException ex) {
                    Logger.printError(ex.getMessage(), ex);
                }
            }
        }

        //closes all other channels to avoid messages being sent to only a subset of the replicas
        /*Enumeration sessionElements = sessionTable.elements();
        while (sessionElements.hasMoreElements()){
            ((NettyClientServerSession) sessionElements.nextElement()).getChannel().close();
        }*/
        rl.writeLock().unlock();
    }

    @Override public void setReplyReceiver(ReplyReceiver trr) {
        this.trr = trr;
    }

    @Override public void send(boolean sign, int[] targets, TOMMessage sm) {

        //        listener.waitForChannels(targets.length); // wait for the previous transmission to complete

        Logger.println(
            "Sending request from " + sm.getSender() + " with sequence number " + sm.getSequence() + " to " + Arrays
                .toString(targets));

        if (sm.serializedMessage == null) {

            //serialize message
            DataOutputStream dos = null;
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                dos = new DataOutputStream(baos);
                sm.wExternal(dos);
                dos.flush();
                sm.serializedMessage = baos.toByteArray();
            } catch (IOException ex) {
                Logger.println("Impossible to serialize message: " + sm);
            } finally {
                try {
                    dos.close();
                } catch (IOException ex) {
                    Logger.printError(ex.getMessage(), ex);
                }
            }
        }

        //Logger.println("Sending message with "+sm.serializedMessage.length+" bytes of content.");

        //produce signature
        if (sign && sm.serializedMessageSignature == null) {
            sm.serializedMessageSignature =
                signMessage(controller.getStaticConf().getRSAPrivateKey(), sm.serializedMessage);
        }

        int sent = 0;
        for (int i = targets.length - 1; i >= 0; i--) {
            sm.destination = targets[i];

            rl.readLock().lock();
            Channel channel = ((NettyClientServerSession)sessionTable.get(targets[i])).getChannel();
            rl.readLock().unlock();
            if (channel.isActive()) {
                sm.signed = sign;
                ChannelFuture f = channel.writeAndFlush(sm);

                f.addListener(listener);

                sent++;
            } else {
                Logger.println("Channel to " + targets[i] + " is not connected");
            }

            try {
                sm = (TOMMessage)sm.clone();
            } catch (CloneNotSupportedException e) {
                Logger.printError(e.getMessage(), e);
            }
        }

        if (targets.length > controller.getCurrentViewF() && sent < controller.getCurrentViewF() + 1) {
            //if less than f+1 servers are connected send an exception to the client
            throw new RuntimeException("Impossible to connect to servers!");
        }
        if (targets.length == 1 && sent == 0)
            throw new RuntimeException("Server not connected");
    }

    public void sign(TOMMessage sm) {
        //serialize message
        DataOutputStream dos = null;
        byte[] data = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            dos = new DataOutputStream(baos);
            sm.wExternal(dos);
            dos.flush();
            data = baos.toByteArray();
            sm.serializedMessage = data;
        } catch (IOException ex) {
        } finally {
            try {
                dos.close();
            } catch (IOException ex) {
                Logger.printError(ex.getMessage(), ex);
            }
        }

        //******* EDUARDO BEGIN **************//
        //produce signature
        byte[] data2 = signMessage(controller.getStaticConf().getRSAPrivateKey(), data);
        //******* EDUARDO END **************//

        sm.serializedMessageSignature = data2;
    }

    public byte[] signMessage(PrivateKey key, byte[] message) {
        //long startTime = System.nanoTime();
        try {
            if (signatureEngine == null) {
                signatureEngine = Signature.getInstance("SHA1withRSA");
            }
            byte[] result = null;

            signatureEngine.initSign(key);
            signatureEngine.update(message);
            result = signatureEngine.sign();

            //st.store(System.nanoTime() - startTime);
            return result;
        } catch (Exception e) {
            Logger.printError(e.getMessage(), e);
            return null;
        }
    }

    @Override public void close() {
        this.closed = true;
        //Iterator sessions = sessionTable.values().iterator();
        rl.readLock().lock();
        ArrayList<NettyClientServerSession> sessions = new ArrayList<>(sessionTable.values());
        rl.readLock().unlock();
        for (NettyClientServerSession ncss : sessions) {
            Channel c = ncss.getChannel();
            closeChannelAndEventLoop(c);
        }
    }

    private ChannelInitializer getChannelInitializer() throws NoSuchAlgorithmException {

        Mac macDummy = Mac.getInstance(controller.getStaticConf().getHmacAlgorithm());

        final NettyClientPipelineFactory nettyClientPipelineFactory =
            new NettyClientPipelineFactory(this, sessionTable, macDummy.getMacLength(), controller, rl,
                signatureLength);

        ChannelInitializer channelInitializer = new ChannelInitializer<SocketChannel>() {
            @Override public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(nettyClientPipelineFactory.getDecoder());
                ch.pipeline().addLast(nettyClientPipelineFactory.getEncoder());
                ch.pipeline().addLast(nettyClientPipelineFactory.getHandler());

            }
        };
        return channelInitializer;
    }

    @Override public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
        scheduleReconnect(ctx, 10);
    }

    @Override public void channelInactive(final ChannelHandlerContext ctx) {
        scheduleReconnect(ctx, 10);
    }

    private void closeChannelAndEventLoop(Channel c) {
        // once having an event in your handler (EchoServerHandler)
        // Close the current channel
        c.close();
        // Then close the parent channel (the one attached to the bind)
        if (c.parent() != null)
            c.parent().close();
        //c.eventLoop().shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    private void scheduleReconnect(final ChannelHandlerContext ctx, int time) {
        if (closed) {
            closeChannelAndEventLoop(ctx.channel());
            return;
        }

        final EventLoop loop = ctx.channel().eventLoop();
        loop.schedule(new Runnable() {
            @Override public void run() {
                reconnect(ctx);
            }
        }, time, TimeUnit.SECONDS);
    }

    private class SyncListener implements GenericFutureListener<ChannelFuture> {

        private int remainingFutures;

        private final Lock futureLock;
        private final Condition enoughCompleted;

        public SyncListener() {

            this.remainingFutures = 0;

            this.futureLock = new ReentrantLock();
            this.enoughCompleted = futureLock.newCondition();
        }

        @Override public void operationComplete(ChannelFuture f) {

            //                this.futureLock.lock();
            //
            //                this.remainingFutures--;
            //
            //                if (this.remainingFutures <= 0) {
            //
            //                    this.enoughCompleted.signalAll();
            //                }
            //
            //                Logger.println("(SyncListener.operationComplete) " + this.remainingFutures + " channel operations remaining to complete");
            //                Logger.println(("(SyncListener.operationComplete) " + this.remainingFutures + " channel operations remaining to complete");
            //                this.futureLock.unlock();

        }

        public void waitForChannels(int n) {

            this.futureLock.lock();
            if (this.remainingFutures > 0) {
                try {
                    this.enoughCompleted.await(1000,
                        TimeUnit.MILLISECONDS); // timeout if a malicous replica refuses to acknowledge the operation as completed
                } catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(NettyClientServerCommunicationSystemClientSide.class.getName())
                        .log(Level.SEVERE, null, ex);
                }

            }

            this.remainingFutures = n;

            this.futureLock.unlock();
        }

    }

}
