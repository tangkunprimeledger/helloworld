package com.higgs.trust.slave.api.enums.utxo;

import lombok.Getter;

/**
 * utxo status enum
 *
 * @author lingchao
 * @create 2018年03月27日16:53
 */
@Getter public enum UTXOStatusEnum {
    UNSPENT("UNSPENT", "it is utxo"), SPENT("SPENT", "is is STXO"),;

    String code;
    String desc;

    UTXOStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
