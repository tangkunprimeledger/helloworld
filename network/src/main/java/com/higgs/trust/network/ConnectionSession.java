package com.higgs.trust.network;

import io.netty.util.AttributeKey;

/**
 * @author duhongming
 * @date 2018/9/6
 */
public final class ConnectionSession {

    public static final AttributeKey<ConnectionSession> ATTR_KEY_CONNECTION_SESSION = AttributeKey.newInstance("ConnectionSession");



    public static enum  ChannelType {
        OUTBOUND,
        INBOUND;
    }

    private Address remoteAddress;
    private ChannelType channelType;

    public ConnectionSession(Address remoteAddress, ChannelType channelType) {
        this.remoteAddress = remoteAddress;
        this.channelType = channelType;
    }

    public Address getRemoteAddress() {
        return remoteAddress;
    }

    public ChannelType getChannelType() {
        return channelType;
    }
}
