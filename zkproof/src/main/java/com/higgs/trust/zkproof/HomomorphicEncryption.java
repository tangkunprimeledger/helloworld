package com.higgs.trust.zkproof;

import com.alibaba.fastjson.JSONObject;

import java.math.BigInteger;

public interface HomomorphicEncryption {

    enum  KEYSTAT{
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

    static String GenSubKey(String key, int seqno, int nodeNum){
        JSONObject ob = JSONObject.parseObject(key);
        if (ob.getString("key_type").compareTo("BGN") == 0){
            return BGNKey.GenSubKey(key, seqno, nodeNum);
        }
        return null;
    }

    static String MergeKey(String key1, String key2){
        if(key2 == null||key2.length()==0){
            return key1;
        }
        JSONObject ob1 = JSONObject.parseObject(key1);
        JSONObject ob2 = JSONObject.parseObject(key2);
        if (ob1.getString("key_type").compareTo("BGN") == 0
                &&ob2.getString("key_type").compareTo("BGN") == 0){
           return BGNKey.MergeKey(key1, key2);
        }
        return null;
    }

    static boolean ContainKey(String fullKey, String subKey){
        JSONObject ob1 = JSONObject.parseObject(fullKey);
        JSONObject ob2 = JSONObject.parseObject(subKey);
        if (ob1.getString("key_type").compareTo("BGN") == 0
                &&ob2.getString("key_type").compareTo("BGN") == 0){
            return BGNKey.ContainKey(fullKey, subKey);
        }
        return false;
    }


}
