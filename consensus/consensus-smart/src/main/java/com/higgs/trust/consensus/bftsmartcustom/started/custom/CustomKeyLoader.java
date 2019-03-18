package com.higgs.trust.consensus.bftsmartcustom.started.custom;

import bftsmart.reconfiguration.util.RSAKeyLoader;
import com.higgs.trust.consensus.util.CaKeyLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

/**
 * @author: zhouyafeng
 * @create: 2018/06/15 17:29
 * @description:
 */
@Component public class CustomKeyLoader extends RSAKeyLoader {

    @Autowired
    private CaKeyLoader caKeyLoader;

    @Autowired
    private NumberNameMapping numberNameMapping;

    @Override public PublicKey loadPublicKey(int id) throws Exception {
        String pubKey = caKeyLoader.loadPublicKey(getNodeNameById(id + ""));
        PublicKey ret = getPublicKeyFromString(pubKey);
        return ret;
    }

    @Override public PrivateKey loadPrivateKey() throws Exception {
        String priKey = caKeyLoader.loadPrivateKey();
        PrivateKey ret = getPrivateKeyFromString(priKey);
        return ret;
    }

    @Override public PublicKey loadPublicKey(String pubKeyStr) throws Exception {
        return getPublicKeyFromString(pubKeyStr);
    }

    private String getNodeNameById(String id) throws Exception {
        Map<String, String> map = numberNameMapping.getMapping();
        if (map == null || map.isEmpty()) {
            throw new Exception("The mapping is empty");
        }
        return map.get(id);
    }

}