package com.higgs.trust.zkproof;

import java.math.BigInteger;

public interface HomomorphicEncryption {


    public enum  KEYSTAT{
        hasNoKey("0","不具有合法的公私钥"),
        hasPubKey("1","具有一个可能合法的公钥"),
        hasFullKey("2","具有一套合法的公私钥");

        private String code;
        private String msg;

        KEYSTAT(String code,String msg)
        {
            this.code = code;
            this.msg = msg;
        }

        public String getCode()
        {
            return  this.code;
        }
    }

    String exportFullKey();

    String exportPubKey();

    boolean hasFullKey();

    boolean hasPubKey();

    String cipherAdd(String em1, String em2);

    BigInteger Decryption(String em);

    String Encryption(BigInteger b, BigInteger r);

    boolean tooBig(BigInteger b);

    boolean tooBigRandom(BigInteger r);
}
