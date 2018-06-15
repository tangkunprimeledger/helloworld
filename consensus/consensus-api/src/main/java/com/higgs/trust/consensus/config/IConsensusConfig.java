package com.higgs.trust.consensus.config;

/**
 * @author: zhouyafeng
 * @create: 2018/06/15 16:42
 * @description:
 */
public interface IConsensusConfig {

    /**
     * get public key create the given nodeName
     *
     * @param nodeName
     * @return
     */
    String pubKey(String nodeName);

    /**
     * get the self private key
     *
     * @return
     */
    String privateKey();
}