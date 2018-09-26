package com.higgs.trust.network;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * @author duhongming
 * @date 2018/8/21
 */
public final class Address implements Serializable {
    private static final int DEFAULT_PORT = 7070;

    private int port;
    private String host;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public static Address local() {
        return from(DEFAULT_PORT);
    }

    public Address(String host, int port) {
        if (host == null || host.trim().equals("")) {
            try {
                InetAddress inetAddress = getLocalAddress();
                this.host = inetAddress.getHostAddress();
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException("Failed to locate host", e);
            }
        } else {
            this.host = host;
        }
        this.port = port;
    }

    public static Address from(int port) {
        try {
            InetAddress inetAddress = getLocalAddress();
            return new Address(inetAddress.getHostAddress(), port);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Failed to locate host", e);
        }
    }

    private static InetAddress getLocalAddress() throws UnknownHostException {
        try {
            return InetAddress.getLocalHost();
        } catch (Exception ignore) {
            return InetAddress.getByName(null);
        }
    }

    @Override
    public String toString() {
        return String.format("%s:%s", host, port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Address that = (Address) obj;
        return this.port == that.port && Objects.equals(this.host, that.host);
    }
}
