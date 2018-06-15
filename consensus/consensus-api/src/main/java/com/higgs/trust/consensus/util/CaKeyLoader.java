package com.higgs.trust.consensus.util;


/**
 * @author: zhouyafeng
 * @create: 2018/06/15 15:51
 * @description:
 */
public interface CaKeyLoader {

    String loadPublicKey(String nodeName) throws Exception;

    String loadPrivateKey() throws Exception;

}