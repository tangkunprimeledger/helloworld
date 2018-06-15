package com.higgs.trust.consensus.bftsmartcustom.started.custom;


import bftsmart.reconfiguration.util.RSAKeyLoader;
import org.apache.commons.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.FileReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author: zhouyafeng
 * @create: 2018/06/15 15:51
 * @description:
 */
public interface CustomRSAKeyLoader {

    PublicKey loadPublicKey(int id) throws Exception;

    PrivateKey loadPrivateKey() throws Exception;

    default PublicKey loadPublicKey(String pubKeyStr) throws Exception {
        return getPublicKeyFromString(pubKeyStr);
    }

    default PrivateKey getPrivateKeyFromString(String key) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(key));
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
        return privateKey;
    }

    default PublicKey getPublicKeyFromString(String key) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.decodeBase64(key));
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        return publicKey;
    }
}