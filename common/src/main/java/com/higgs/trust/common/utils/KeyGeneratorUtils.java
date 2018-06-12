package com.higgs.trust.common.utils;

import org.springframework.util.Base64Utils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * @author WangQuanzhou
 * @desc key generator util
 * @date 2018/6/4 17:27
 */
public class KeyGeneratorUtils {
    public static final String KEY_ALGORITHM = "RSA";
    public static final String PUB_KEY = "pubKey";
    public static final String PRI_KEY = "priKey";

    public static Map generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(1024);
        KeyPair keyPair = keyPairGen.generateKeyPair();

        RSAPublicKey publicKey = (RSAPublicKey)keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey)keyPair.getPrivate();
        String pubKey = Base64Utils.encodeToString(publicKey.getEncoded());
        String priKey = Base64Utils.encodeToString(privateKey.getEncoded());

        System.out.println(pubKey.length());
        System.out.println(priKey.length());

        Map map = new HashMap();
        map.put(PUB_KEY,pubKey);
        map.put(PRI_KEY,priKey);
        return map;
    }

    public static void main(String[] args) {
        try {
            generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
