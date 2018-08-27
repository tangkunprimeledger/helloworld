package com.higgs.trust.common.crypto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author WangQuanzhou
 * @desc KeyPair  Base64 Encoded
 * @date 2018/8/10 15:44
 */
@Getter @Setter public class KeyPair {
    private String pubKey;
    private String priKey;

    public KeyPair(String pubKey, String priKey) {
        this.pubKey = pubKey;
        this.priKey = priKey;
    }
}
