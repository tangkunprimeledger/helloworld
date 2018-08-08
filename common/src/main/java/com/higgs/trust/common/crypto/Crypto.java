package com.higgs.trust.common.crypto;

public interface Crypto {

    /**
     * @param
     * @return
     * @desc generate pub/pri key pair
     */
    Object generateKeyPair();

    /**
     * @param input     source to be encrypted
     * @param publicKey
     * @return String
     * @desc encrypt, suport ECC, SM2, RSA encrypt
     */
    String encrypt(String input, String publicKey) throws Exception;

    /**
     * @param input,     source to be decrypted
     * @param privateKey
     * @return String
     * @desc decrypt,  suport ECC, SM2, RSA decrypt
     */
    String decrypt(String input, String privateKey) throws Exception;

    /**
     * @param message
     * @param privateKey
     * @return
     * @desc sign message
     */
    String sign(String message, String privateKey) throws Exception;

    /**
     * @param message
     * @param publicKey
     * @return
     * @desc verify signature
     */
    boolean verify(String message, String signature, String publicKey) throws Exception ;



}
