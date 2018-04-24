package com.higgs.trust.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Base64Utils;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author yangjiyun
 * @Description: 签名验签工具类
 * @date 2018/1/12 19:14
 */
@Slf4j public class SignUtils {

    public static final String KEY_ALGORITHM = "RSA";
    public static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    private static final String EQUAL_SIGN = "=";
    private static final String DELIMITER = "&";

    /**
     * BASE64解码
     *
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] decryptBASE64(String key) throws Exception {
        return Base64Utils.decodeFromString(key);
    }

    /**
     * BASE64编码
     *
     * @param key
     * @return
     * @throws Exception
     */
    public static String encryptBASE64(byte[] key) throws Exception {
        return Base64Utils.encodeToString(key);
    }

    /**
     * 用私钥对信息生成数字签名
     *
     * @param dataString // 签名对象
     * @return
     * @throws Exception
     */
    public static String sign(String dataString, String privateKey) throws Exception {
        log.info("sign data string:{}", dataString);

        //解码私钥
        byte[] keyBytes = decryptBASE64(privateKey);
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

        return encryptBASE64(signature.sign());
    }

    /**
     * 校验数字签名
     *
     * @param verifyString 验签对象
     * @param sign         数字签名
     * @return
     * @throws Exception
     */
    public static boolean verify(String verifyString, String sign, String publicKey) throws Exception {
        //解码公钥
        byte[] keyBytes = decryptBASE64(publicKey);
        //构造X509EncodedKeySpec对象
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
        //指定加密算法
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        //取公钥匙对象
        PublicKey publicKey2 = keyFactory.generatePublic(x509EncodedKeySpec);

        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicKey2);
        signature.update(verifyString.getBytes("UTF-8"));

        return signature.verify(decryptBASE64(sign));
    }
}