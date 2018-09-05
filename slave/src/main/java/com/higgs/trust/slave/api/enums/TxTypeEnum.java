package com.higgs.trust.slave.api.enums;

/**
 * @author liuyu
 * @desc the transaction type enum class
 * @date 2018/09/04 17:26
 */
public enum TxTypeEnum {
    DEFAULT("DEFAULT", "default type"),
    RS("RS", "rs register or cancel"),
    POLICY("POLICY", "policy register"),
    CA("CA", "ca auth、update or cancel"),
    NODE("NODE", "node join or leave"),
    UTXO("UTXO", "utxo issue or destroy"),
    CONTRACT("CONTRACT", "contract issue or destroy"),
    ;

    String code;
    String desc;

    TxTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * get type by code
     *
     * @param code
     * @return
     */
    public static TxTypeEnum getBycode(String code) {
        for (TxTypeEnum typeEnum : TxTypeEnum.values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
    /**
     * is target type
     *
     * @param code
     * @param target
     * @return
     */
    public static boolean isTargetType(String code,TxTypeEnum target) {
        TxTypeEnum txTypeEnum = getBycode(code);
        if(txTypeEnum == null){
            return false;
        }
        return txTypeEnum == target;
    }
}
