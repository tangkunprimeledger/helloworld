package com.higgs.trust.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * crypto enum
 *
 * @author lingchao
 * @create 2018年09月14日14:29
 */
@Getter
public enum CryptoTypeEnum {
    RSA("RSA", "rsa type"),
    SM("SM", "sm type"),
    ECC("ECC", "ecc type");

    CryptoTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;

    }
    String code;
    String desc;

    /**
     * get CryptoTypeEnum by code
     *
     * @param code
     * @return
     */
    public static CryptoTypeEnum getByCode(String code) {
        for (CryptoTypeEnum cryptoTypeEnum : CryptoTypeEnum.values()) {
            if (StringUtils.equals(cryptoTypeEnum.getCode(), code)) {
                return cryptoTypeEnum;
            }
        }
        return null;
    }

}
