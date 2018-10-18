package com.higgs.trust.network.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author duhongming
 * @date 2018/8/24
 */
public class AuthenticationHandler extends ChannelInboundHandlerAdapter {

    private final boolean isServerSide;

    public AuthenticationHandler(boolean isServerSide) {
        this.isServerSide = isServerSide;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (!isServerSide) {
//            AuthenticationRequest authenticationRequest = new AuthenticationRequest("higgs-trust", "");
//            NetworkRequest request = new NetworkRequest(0, "authenticate", Hessian.serialize(authenticationRequest));
            System.out.println("Start Authentication ......");
        }
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("End Authentication ......");
        ctx.pipeline().remove(this);
    }
}
