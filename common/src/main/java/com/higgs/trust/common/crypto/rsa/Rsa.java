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
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDCVkVmZ71dzS2MX36ndS9IhSUcu0oRyH0mP6XNS+DHFTjXaKwRFOfPHg6dDWH/fBCW0ZgRycQLKu5kJl8Q3K6k6irZUemadGIHKigHv6dTCQ03hMy8oeeYHfgXn4jsh0XIWk8gs73tnMumacV1X+fVX7F328FVMWFSe8Zt4rzVpQIDAQAB";
        String priKey =
            "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAJbbDfuK8DsOuvx3u1kx0aZGcuvx3vrnlIQ7aPhSLbOPbD2ec3SgujF9/l8AzPqSflTsGicJnLeXQGAAaa9Y4B8HmUZiX8Nn7buPpdFALOpd/78v8wxi1adRkILWeX6c1JEo+JbSMchUQZNv+7ovm5c8rjXCuNbAJu55CAZ/n74ZAgMBAAECgYA3TTx1/zwL2l2P2fCzRQEfHGpatoNQpX6bbxAPIEkiryw19pVKpvU62X5bo3aBURzA0wDPWMW7w9XUm7Iilskp5qgu7YCesDunKLkqVPPja4T/+2D5aUuk29udrN8umqlF7I+NaYV/rduBQmGNGTHD21RN6ZDjzs9FtaDv5rN6FQJBAM32uA2Xsnp828xUx+UsP8Cpn7lKRvbZR1RPMrHhpmLh1Na3fqVoUf7kDEuGyeY64Q5RqadnvwjnJYndwhTDNKcCQQC7gQ8ugDlo2Z03BZruOm0jZnlOpeg1mWeHL7cMmSvn/KOaJyi45eDlX5VLUdWFxmWtYoMHkD6EFIy7ORVggA8/AkEAk3JNnwV7cy7Rl30WQZ0k4sNMIjTniq5P3y53Z1rYZ6+uVCy20KlXEfemSadsAJMkLMEPiFXAMBpyCDmmSIDavwJAeTJVltAIy64FgcAcwamAS+Z7uItiieqrUWVVI06KY7wYH5b6KnFkKb7bqECwDHUN2cGYQjZJQmRqBsZB/AsqTwJAa5qdCUemkl3wKNdlqvEQGA7Ng+qvaR5qG9XmArGI9LLKJ6C6jPkNLymljsYAu9770TIWBcnTStmkd7iy2FWEFQ==";
        String sign = Rsa.sign("hello", priKey);
        System.out.println("签名结果：" + sign);
        System.out.println("验签结果：" + Rsa.verify("hello", sign, pubKey));
    }
}
