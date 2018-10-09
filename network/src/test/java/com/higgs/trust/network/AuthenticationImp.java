package com.higgs.trust.network;

/**
 * @author duhongming
 * @date 2018/9/7
 */
public class AuthenticationImp implements Authentication {


    @Override
    public boolean validate(Peer peer, String signature) {
        return peer.getNodeName().equals(signature);
    }

    @Override
    public String sign(Peer localPeer, String privateKey) {
        return localPeer.getNodeName();
    }
}
