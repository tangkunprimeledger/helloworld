package com.higgs.trust.zkproof;

import com.alibaba.fastjson.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

public class EncryptAmount {
    private BigInteger b;
    private String eb;
    private BigDecimal bd;

    private static String keyType;

    private BigInteger r;

    private static HomomorphicEncryption he;
    private static HomomorphicEncryption heCheck;

    private String statues;

    private static final int FULL_RANDOM_BIT = 64;
    private static final int SAFE_RANDOM_BIT = 32;
    private static final int SAFE_ZERO_NUM = 10;
    private static final int FIX_SCALE = 10;

    private static final BigInteger SAFE_MASK = BigInteger.TEN.pow(new BigInteger("2").pow(FULL_RANDOM_BIT).toString().length()+ SAFE_ZERO_NUM);

    public static final BigInteger FULL_RANDOM = new BigInteger("2").pow(FULL_RANDOM_BIT).subtract(BigInteger.ONE);

    public enum  STATUES{
        unSafeRandom("unSafeRandom","随机数长度不足"),
        success("success", "成功加密"),
        tooBig("tooBig","原始数据过大"),
        tooBigRandom("tooBigRandom", "随机数过大");

        private String code;
        private String msg;

        STATUES(String code,String msg)
        {
            this.code = code;
            this.msg = msg;
        }

        public String getCode()
        {
            return  this.code;
        }
    }

    public EncryptAmount(BigInteger b, String eb, BigInteger r) {

        this.b = b;
        this.eb = eb;
        this.r = r;
        if (r.bitLength() <= SAFE_RANDOM_BIT ){
            statues = STATUES.unSafeRandom.getCode();
        }
        else if (he.tooBig(b) == true){
            statues = STATUES.tooBig.getCode();
        }
        else if (he.tooBigRandom(r) == true) {
            statues = STATUES.tooBigRandom.getCode();
        }
        else{
            statues = STATUES.success.getCode();
        }

    }

    public EncryptAmount(String value,BigInteger orgRandom) {

        bd = new BigDecimal(value);
        b = new BigInteger(bd.multiply(BigDecimal.TEN.pow(FIX_SCALE)).setScale(0).toString());
        if (orgRandom.bitLength() <= SAFE_RANDOM_BIT ){
           statues = STATUES.unSafeRandom.getCode();
           r = BigInteger.ZERO;
           b = BigInteger.ZERO;
           eb = Base58.encode(BigInteger.ZERO.toByteArray());
        }
        else if (he.tooBig(b)){
            statues = STATUES.tooBig.getCode();
            r = BigInteger.ZERO;
            b = BigInteger.ZERO;
            eb = Base58.encode(BigInteger.ZERO.toByteArray());
        }
        else if (he.tooBigRandom(orgRandom)){
            statues = STATUES.tooBigRandom.getCode();
            r = BigInteger.ZERO;
            b = BigInteger.ZERO;
            eb = Base58.encode(BigInteger.ZERO.toByteArray());
        }
        else{
            r = orgRandom;
            if (keyType.compareTo("Paillier") == 0) {
                b = b.multiply(SAFE_MASK).add(r);
            }
            eb = he.Encryption(b,r);
            statues = STATUES.success.getCode();
        }

    }

    public static void initHomomorphicEncryption(String type, int bits){
        keyType = type;
        do {

            if (keyType.compareTo("Paillier") == 0){
                he = new Paillier(bits);
                heCheck = new Paillier(he.exportFullKey());
            }
            else{
                he = new BGNEncryption(bits);
                heCheck = new BGNEncryption(he.exportFullKey());
            }
         }  while (heCheck.hasFullKey() == false);
    }

    public static boolean setHomomorphicEncryptionKey(String key) {

        JSONObject keyJ = JSONObject.parseObject(key);
        keyType = keyJ.getString("key_type");
        if (keyJ.getString("key_type").compareTo("Paillier") == 0){
            he = new Paillier(key);
        }
        else {
            he = new BGNEncryption(key);
        }

        return (he.hasPubKey() == true || he.hasFullKey() == true);

    }

    public  static String exportPubKey(){

        if (he.hasFullKey() == true || he.hasPubKey() == true){
            return he.exportPubKey();
        }

        return  null;
    }

    public  static String exportFullKey(){

        if (he.hasFullKey() == true || he.hasPubKey() == true){
            return he.exportFullKey();
        }
        return  null;
    }

    public static String cipherAdd(String em1, String em2){
        if (he != null && (he.hasPubKey() == true || he.hasFullKey() == true)){

            return  he.cipherAdd(em1, em2);
        }
        return Base58.encode(BigInteger.ZERO.toByteArray());
    }



    public EncryptAmount add(EncryptAmount amount){

        if(he.hasPubKey() == true || he.hasFullKey() == true) {
            return new EncryptAmount(b.add(amount.b),he.cipherAdd(eb, amount.eb),r.add(amount.r));
        }
        return  new EncryptAmount(BigInteger.ZERO,Base58.encode(BigInteger.ZERO.toByteArray()),BigInteger.ZERO);
    }

    public EncryptAmount subtract(EncryptAmount amount){

        if((he.hasPubKey() == true || he.hasFullKey() == true) && b.compareTo(amount.b) >= 0 && r.compareTo(amount.r) > 0
                && this.isAvailable() == true && amount.isAvailable() == true) {

            return  new EncryptAmount(b.subtract(amount.b) , he.Encryption(b.subtract(amount.b),r.subtract(amount.r)),r.subtract(amount.r));
        }

        return  new EncryptAmount(BigInteger.ZERO, Base58.encode(BigInteger.ZERO.toByteArray()),BigInteger.ZERO);
    }

    public String toString(){

       if (isAvailable() == true){
           return eb;

       }

       return Base58.encode(BigInteger.ZERO.toByteArray());
    }

    public static BigInteger Decryption(String str){
        if (he.hasFullKey() == true){
            return he.Decryption(str);
        }

        return BigInteger.ZERO;
    }

    public BigInteger getSubRandom(){
        Random rnd = new Random();
        return new BigDecimal(r.divide(new BigInteger("2")).toString()).multiply(new BigDecimal(String.valueOf(rnd.nextDouble()))).toBigInteger();
    }


    public BigInteger getRandom(){
        return  r;
    }

    public boolean hasUnSafeRondom(){
        return statues == STATUES.unSafeRandom.getCode();
    }

    public boolean isAvailable(){
        return statues == STATUES.success.getCode();
    }

    public HomomorphicEncryption getHe(){
        return this.he;
    }

}
