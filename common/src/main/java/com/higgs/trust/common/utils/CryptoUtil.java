package com.higgs.trust.common.utils;

import com.higgs.trust.common.crypto.Crypto;
import com.higgs.trust.common.crypto.ecc.EccCrypto;
import com.higgs.trust.common.crypto.gm.GmCrypto;
import com.higgs.trust.common.crypto.rsa.RsaCrypto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * @author WangQuanzhou
 * @desc crypto selector class
 * @date 2018/8/15 15:25
 */
@Component @Slf4j public class CryptoUtil {

    private static String biz;
    private static String consensus;

    public static Crypto getBizCrypto() {
        log.info("crypto type for biz layer is {}", biz);
        return selector(biz);
    }

    public static Crypto getProtocolCrypto() {
        log.info("crypto type for consensus layer is {}", consensus);
        return selector(consensus);
    }

    private static Crypto selector(String usage) {
        switch (usage) {
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

    @NotNull @Value("${higgs.trust.crypto.biz:SM}")
    private void setBiz(String newBiz){
        biz = newBiz;
    }

    @NotNull @Value("${higgs.trust.crypto.consensus:RSA}")
    private void setConsensus(String newConsensus){
        consensus = newConsensus;
    }
}
