package com.higgs.trust.network.message;

import java.io.Serializable;

/**
 * @author duhongming
 * @date 2018/8/31
 */
public class AuthenticationRequest implements Serializable {

    private String publicKey;
    private String nodeName;
    private long nonce;
    private int httpPort;
    private String signature;

    public AuthenticationRequest(String nodeName, String publicKey, long nonce, int httpPort, String signature) {
        this.nodeName = nodeName;
        this.publicKey = publicKey;
        this.nonce = nonce;
        this.httpPort = httpPort;
        this.signature = signature;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
