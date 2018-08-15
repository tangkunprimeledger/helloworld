package com.higgs.trust.common.utils;

import com.higgs.trust.common.crypto.Crypto;
import com.higgs.trust.common.crypto.ecc.EccCrypto;
import com.higgs.trust.common.crypto.gm.GmCrypto;
import com.higgs.trust.common.crypto.rsa.RsaCrypto;
import org.springframework.beans.factory.annotation.Value;

import javax.validation.constraints.NotNull;

/**
 * @author WangQuanzhou
 * @desc crypto selector class
 * @date 2018/8/15 15:25
 */
public class CryptoUtil {

    @NotNull @Value("${higgs.trust.crypto.biz:SM}") private static String biz;
    @NotNull @Value("${higgs.trust.crypto.consensus:RSA}") private static String consensus;

    public static Crypto getBizCrypto() {
        switch (biz) {
            case "RSA":
                return RsaCrypto.getSingletonInstance();
            case "SM":
                return GmCrypto.getSingletonInstance();
            case "ECC":
                return EccCrypto.getSingletonInstance();
            default:
        }
        return null;
    }

    public static Crypto getProtocolCrypto() {
        switch (consensus) {
            case "RSA":
                return RsaCrypto.getSingletonInstance();
            case "SM":
                return GmCrypto.getSingletonInstance();
            case "ECC":
                return EccCrypto.getSingletonInstance();
            default:
        }
        return null;
    }
}
