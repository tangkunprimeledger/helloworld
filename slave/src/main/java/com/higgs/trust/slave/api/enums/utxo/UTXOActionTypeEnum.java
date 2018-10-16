package com.higgs.trust.slave.api.enums.utxo;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * UTXO ActionPO Type enum
 *
 * @author lingchao
 * @create 2018年03月27日16:53
 */
@Getter public enum UTXOActionTypeEnum {
    ISSUE("ISSUE", "issue the state UTXO action, need all partners to sign the transaction"), NORMAL("NORMAL", "normal UTXO action , need  partners to sign the transaction"), DESTRUCTION("DESTRUCTION",
        "destruction UTXO action, need the owner to sign the transaction"),CRYPTO_ISSUE("CRYPTO_ISSUE","the same as issue"),CRYPTO_NORMAL("CRYPTO_NORMAL","the same as normal");

    String code;
    String desc;

    UTXOActionTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static UTXOActionTypeEnum getUTXOActionTypeEnumByName(String name) {
        for (UTXOActionTypeEnum utxoActionTypeEnum : UTXOActionTypeEnum.values()) {
            if (StringUtils.equals(utxoActionTypeEnum.name(), name)) {
                return utxoActionTypeEnum;
            }
        }
        return null;
    }
}
