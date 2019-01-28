package com.higgs.trust.common.crypto.rsa;

import com.higgs.trust.common.utils.Base64Util;
import lombok.extern.slf4j.Slf4j;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Slf4j public class Rsa {

    public static final String KEY_ALGORITHM = "RSA";
    public static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    private static final String EQUAL_SIGN = "=";
    private static final String DELIMITER = "&";

    /**
     * 用私钥对信息生成数字签名s
     *
     * @param dataString // 签名对象
     * @return
     * @throws Exception
     */
    public static String sign(String dataString, String privateKey) {
        try {
            //解码私钥
            byte[] keyBytes = Base64Util.decryptBASE64(privateKey);
            //构造PKCS8EncodedKeySpec对象
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
            //指定加密算法
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            //取私钥匙对象
            PrivateKey privateKey2 = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
            //用私钥对信息生成数字签名
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey2);
            signature.update(dataString.getBytes("UTF-8"));

            return Base64Util.encryptBASE64(signature.sign());
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("sign failed, priKey = {}", privateKey);
            }
            throw new RuntimeException("sign utils sign failed", e);
        }
    }

    /**
     * 校验数字签名
     *
     * @param verifyString 验签对象
     * @param sign         数字签名
     * @return
     * @throws Exception
     */
    public static boolean verify(String verifyString, String sign, String publicKey) {
        try {
            //解码公钥
            byte[] keyBytes = Base64Util.decryptBASE64(publicKey);
            //构造X509EncodedKeySpec对象
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
            //指定加密算法
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            //取公钥匙对象
            PublicKey publicKey2 = keyFactory.generatePublic(x509EncodedKeySpec);

            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey2);
            signature.update(verifyString.getBytes("UTF-8"));
            return signature.verify(Base64Util.decryptBASE64(sign));
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("verify failed, pubKey = {}", publicKey);
            }
            throw new RuntimeException("sign utils verify failed", e);
        }
    }

    public static void main(String[] args) {
        String pubKey =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCtM0Sx9KskNepKi9YAFHO+GrWem80TwGVGRHwTY13mLISHJHaa4+2i8JHQLsFHxK/NKUEDmrbOX65IbZDcDfIxFe90lss2H3QayAONxAwOygotl5Ih5bZFlicaUowmhXYz77fW9tY0p5rfojUEnpJzJMQJ/+umOAGVY40o/XW/sQIDAQAB";
        String priKey =
            "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAK0zRLH0qyQ16kqL1gAUc74atZ6bzRPAZUZEfBNjXeYshIckdprj7aLwkdAuwUfEr80pQQOats5frkhtkNwN8jEV73SWyzYfdBrIA43EDA7KCi2XkiHltkWWJxpSjCaFdjPvt9b21jSnmt+iNQSeknMkxAn/66Y4AZVjjSj9db+xAgMBAAECgYAFRQwQBgu2/FT2k66dLgIfhJyGCOOCeFYcfzTvOhS/ThdyLS/Wopy2Wm91UBbdYiSfL2QlAe4R9WNOHNdNerclrjsYP7QrQD+n6/Jmn9cVJVrak91Q5g9+p0PcnUta/+Bin8dFdKfiv1gnJoX3rs5/rtgDEfsWzg+Mq7ZIU2LwgQJBAPQRQcy4uBeQ3kRXrMQFXrHQnetAwo2bfPjqczBHIi8MDJQuWWpm/x8vAPO4GDQFyKnr1fqRuXAzAtnOTLDSOokCQQC1qwod+4RlfgwJJoqxv7FSgwGWP9BnNBy8wOexCapPTdhAVTUmEU7LlGBDhyr4AbC6DJhrRMTVXsiN/P5+CHHpAkBjrvSv97YAsgOF6EVA3myZtXn6Tr3ndl0pkSAw1KzKiGJO10tf4OdMRjdeU49XGBGoDRGdQI17nOoKKW87PPH5AkAfkrBZcaMa+IMi+/3S0pwA2R/newO/TAFKlMQvspxU2BSjaaupCA0HuKvaUJ2ZKMIMM7AxxGvpIWX9t2CLivAxAkBS2YRHPiSpMd7wIno9gxHBUodXLwzvtLc10Jv2gzk/UH1lP5EqUTU+6cSLFIGxa8RZNy9BcuX/AX5KNnUKmIe7";

        String sign = Rsa.sign("hello", priKey);
        System.out.println("签名结果：" + sign);
        System.out.println("验签结果：" + Rsa.verify("hello", sign, pubKey));
    }
}
