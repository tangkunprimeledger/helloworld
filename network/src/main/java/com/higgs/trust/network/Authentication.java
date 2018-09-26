package com.higgs.trust.network;

/**
 * @author duhongming
 * @date 2018/9/7
 */
public interface Authentication {

    /**
     * validate
     * @param peer
     * @param signature
     * @return
     */
    boolean validate(Peer peer, String signature);


    /**
     * sign
     * @param localPeer
     * @param privateKey
     * @return
     */
    String sign(Peer localPeer, String privateKey);
}
