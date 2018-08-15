package com.higgs.trust.common.crypto.gm;

import com.higgs.trust.common.crypto.Crypto;
import com.higgs.trust.common.crypto.KeyPair;
import com.higgs.trust.common.utils.Base64Util;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "higgs.trust", name = "crypto", havingValue = "SM", matchIfMissing = true)
public class GmCrypto implements Crypto {

    /**
     * @return
     * @desc generate pub/pri key pair
     */
    @Override public KeyPair generateKeyPair() {
        return SM2.generateEncodedKeyPair();
    }

    /**
     * @param input     source to be encrypted
     * @param publicKey
     * @return String
     * @desc encrypt, suport ECC, SM2, RSA encrypt
     */
    @Override public String encrypt(String input, String publicKey) throws Exception {
        return Base64Util.byteArrayToHexStr(SM2.encrypt(input, publicKey));
    }

    /**
     * @param input
     * @param privateKey
     * @return String
     * @desc decrypt,  suport ECC, SM2, RSA decrypt
     */
    @Override public String decrypt(String input, String privateKey) throws Exception {
        return SM2.decrypt(input, privateKey);
    }

    /**
     * @param message
     * @param privateKey
     * @return
     * @desc sign message
     */
    @Override public String sign(String message, String privateKey) {
        return SM2.sign(message, privateKey).toString();
    }

    /**
     * @param message
     * @param publicKey
     * @return
     * @desc verify signature
     */
    @Override public boolean verify(String message, String signature, String publicKey) {
        return SM2.verify(message, signature, publicKey);
    }
}
