package com.higgs.trust.config.crypto;

import com.higgs.trust.common.crypto.Crypto;
import com.higgs.trust.common.crypto.ecc.EccCrypto;
import com.higgs.trust.common.crypto.gm.GmCrypto;
import com.higgs.trust.common.crypto.rsa.RsaCrypto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * @author WangQuanzhou
 * @desc crypto selector class
 * @date 2018/8/15 15:25
 */
@Order(1) @Component @Slf4j public class CryptoUtil {

    public static String biz;
    public static String consensus;

    public static Crypto getBizCrypto() {
        if (log.isDebugEnabled()) {
            log.trace("crypto type for biz layer is {}", biz);
        }
        return selector(biz);
    }

    public static Crypto getProtocolCrypto() {
        if (log.isDebugEnabled()) {
            log.trace("crypto type for consensus layer is {}", consensus);
        }
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

    @NotNull @Value("${higgs.trust.crypto.biz:SM}") public void setBiz(String newBiz) {
        log.info("set biz,newBiz={}", newBiz);
        biz = newBiz;
    }

    @NotNull @Value("${higgs.trust.crypto.consensus:RSA}") public void setConsensus(String newConsensus) {
        log.info("set biz,newConsensus={}", newConsensus);
        consensus = newConsensus;
    }

}
