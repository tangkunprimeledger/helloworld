package com.higgs.trust.common.crypto.gm;

import com.higgs.trust.common.crypto.Crypto;
import com.higgs.trust.common.utils.SignUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "higgs.trust", name = "crypto", havingValue = "SM", matchIfMissing = true)
public class GmCrypto implements Crypto {

    @Autowired SM2 sm2;

    /**
     * @return
     * @desc generate pub/pri key pair
     */
    @Override public Object generateKeyPair() {
        return sm2.generateKeyPair();
    }

    /**
     * @param input     source to be encrypted
     * @param publicKey
     * @return String
     * @desc encrypt, suport ECC, SM2, RSA encrypt
     */
    @Override public String encrypt(String input, String publicKey) throws Exception {
        return SignUtils.byteArrayToHexStr(sm2.encrypt(input, publicKey));
    }

    /**
     * @param input
     * @param privateKey
     * @return String
     * @desc decrypt,  suport ECC, SM2, RSA decrypt
     */
    @Override public String decrypt(String input, String privateKey) throws Exception {
        return sm2.decrypt(input, privateKey);
    }

    /**
     * @param message
     * @param privateKey
     * @return
     * @desc sign message
     */
    @Override public String sign(String message, String privateKey) throws Exception {
        return sm2.sign(message, privateKey).toString();
    }

    /**
     * @param message
     * @param publicKey
     * @return
     * @desc verify signature
     */
    @Override public boolean verify(String message, String signature, String publicKey) throws Exception {
        return sm2.verify(message, signature, publicKey);
    }
}
