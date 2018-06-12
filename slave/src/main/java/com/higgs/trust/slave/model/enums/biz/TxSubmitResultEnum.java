package com.higgs.trust.slave.model.enums.biz;

/**
 * @author tangfashuang
 * @date 2018/05/12 20:46
 * @desc transaction submit result enum
 */
public enum TxSubmitResultEnum {
    PARAM_INVALID("100000", "transaction param invalid"),
    PENDING_TX_IDEMPOTENT("200000", "pending transaction idempotent"),
    TX_IDEMPOTENT("300000", "transaction idempotent"),
    NODE_STATE_IS_NOT_RUNNING("400000", "node state is not running"),
    TX_QUEUE_SIZE_TOO_LARGE("500000", "pending transaction queue size is too large");

    TxSubmitResultEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private String code;

    private String desc;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static TxSubmitResultEnum getByCode(String code) {
        for (TxSubmitResultEnum enumeration : values()) {
            if (enumeration.getCode().equals(code)) {
                return enumeration;
            }
        }
        return null;
    }
}
