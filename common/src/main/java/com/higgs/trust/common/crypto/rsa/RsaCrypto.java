package com.higgs.trust.common.crypto.rsa;

import com.higgs.trust.common.crypto.Crypto;
import com.higgs.trust.common.crypto.KeyPair;
import com.higgs.trust.common.utils.RsaKeyGeneratorUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration @Slf4j
@ConditionalOnProperty(prefix = "higgs.trust", name = "crypto", havingValue = "RSA", matchIfMissing = false)
public class RsaCrypto implements Crypto {
    /**
     * @return
     * @desc generate pub/pri key pair
     */
    @Override public KeyPair generateKeyPair() {
        Map<String, String> map = RsaKeyGeneratorUtils.generateKeyPair();
        return new KeyPair(map.get("pubKey"), map.get("priKey"));
    }

    /**
     * @param input     source to be encrypted
     * @param publicKey
     * @return String
     * @desc encrypt, suport ECC, SM2, RSA encrypt
     */
    @Override public String encrypt(String input, String publicKey) throws Exception {
        log.warn("encrypt not support for rsa until now");
        return null;
    }

    /**
     * @param input
     * @param privateKey
     * @return String
     * @desc decrypt,  suport ECC, SM2, RSA decrypt
     */
    @Override public String decrypt(String input, String privateKey) throws Exception {
        log.warn("decrypt not support for rsa until now");
        return null;
    }

    /**
     * @param message
     * @param privateKey
     * @return
     * @desc sign message
     */
    @Override public String sign(String message, String privateKey) {
        return Rsa.sign(message, privateKey);
    }

    /**
     * @param message
     * @param signature
     * @param publicKey
     * @return
     * @desc verify signature
     */
    @Override public boolean verify(String message, String signature, String publicKey) {
        return Rsa.verify(message, signature, publicKey);
    }
}
