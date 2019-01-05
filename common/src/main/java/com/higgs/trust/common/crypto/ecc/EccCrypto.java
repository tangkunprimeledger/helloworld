package com.higgs.trust.common.crypto.ecc;

import com.higgs.trust.common.crypto.Crypto;
import com.higgs.trust.common.crypto.KeyPair;
import lombok.extern.slf4j.Slf4j;
import org.spongycastle.util.encoders.Hex;

/**  
 * @desc class for ecc crypto and sign
 * @author WangQuanzhou
 * @date 2018/8/20 11:50
 */  
@Slf4j public class EccCrypto implements Crypto {

    /**
     * @param
     * @return
     * @desc default constructor
     */
    private EccCrypto() {
    }

    /**
     * @desc inner class
     */
    private static class SingletonHolder {
        // 私有的 静态的  final类型的
        private static final EccCrypto INSTANCE = new EccCrypto();
    }

    /**
     * @return
     * @desc generate pub/pri key pair
     */
    @Override public KeyPair generateKeyPair() {
        return ECKey.generateEncodedKeyPair();
    }

    /**
     * @param input     source to be encrypted
     * @param publicKey
     * @return String
     * @desc encrypt, suport ECC, SM2, RSA encrypt
     */
    @Override public String encrypt(String input, String publicKey) throws Exception {
        log.warn("encrypt not support for ecc until now");
        return null;
    }

    /**
     * @param input
     * @param privateKey
     * @return String
     * @desc decrypt,  suport ECC, SM2, RSA decrypt
     */
    @Override public String decrypt(String input, String privateKey) throws Exception {
        log.warn("decrypt not support for ecc until now");
        return null;
    }

    /**
     * @param message
     * @param privateKey
     * @return
     * @desc sign message   Base64 Encoded
     */
    @Override public String sign(String message, String privateKey) {
        ECKey newEcKey = ECKey.fromPrivate(Hex.decode(privateKey));
        return newEcKey.signMessage(message);
    }

    /**
     * @param message
     * @param signature
     * @param publicKey
     * @return
     * @desc verify signature
     */
    @Override public boolean verify(String message, String signature, String publicKey) {
        return ECKey.verify(message, signature, publicKey);
    }

    /**
     * @return
     * @desc get singleton instance
     */
    public static final EccCrypto getSingletonInstance() {
        return SingletonHolder.INSTANCE;
    }
}
