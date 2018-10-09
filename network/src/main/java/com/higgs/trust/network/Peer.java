package com.higgs.trust.network;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author duhongming
 * @date 2018/8/22
 */
public class Peer implements Serializable {

    public static final int MAX_TRY_CONNECT_TIMES = 10;

    public static final int STATE_OFFLINE = 0;
    public static final int STATE_ALIVE = 1;
    public static final int STATE_NEW = 2;

    private long nonce;
    private Address address;
    private String publicKey = "";
    private String nodeName = "";
    private int httpPort;
    private boolean isSlave;
    private transient boolean connected = false;

    public Peer(Address address) {
        this.address = address;
    }

    public Peer(Address address, String publicKey) {
        this(address);
        this.publicKey = publicKey;
    }

    public Peer(Address address, String nodeName, String publicKey) {
        this(address, publicKey);
        this.nodeName = nodeName;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public boolean isSlave() {
        return isSlave;
    }

    public void setSlave(boolean slave) {
        isSlave = slave;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void update(Peer newPeer) {
        nonce = newPeer.nonce;
        publicKey = newPeer.publicKey;
        nodeName = newPeer.nodeName;
        httpPort = newPeer.httpPort;
        isSlave = newPeer.isSlave;
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, publicKey, nodeName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        Peer that = (Peer) obj;
        return nodeName.equals(nodeName) && publicKey.equals(that.publicKey) && this.address.equals(that.address);
    }

    @Override
    public String toString() {
        String pubKey = (publicKey != null && publicKey.length() > 24)
                ? String.format("%s...%s", publicKey.substring(0, 12), publicKey.substring(this.publicKey.length() - 12))
                : publicKey;
        return String.format("%s, name=%s, nonce=%s publicKey=%s, connected=%s",
                address.toString(), nodeName, nonce, pubKey, connected);
    }
}
