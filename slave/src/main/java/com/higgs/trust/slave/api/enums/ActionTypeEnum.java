package com.higgs.trust.slave.api.enums;

/**
 * @author WangQuanzhou
 * @desc action type enum class
 * @date 2018/3/26 17:26
 */
public enum ActionTypeEnum {

    OPEN_ACCOUNT("OPEN_ACCOUNT", "open account action"), ACCOUNTING("ACCOUNTING", "accounting action"), FREEZE("FREEZE",
        "freeze capital action"), UNFREEZE("UNFREEZE", "unfreeze capital action"), UTXO("UTXO",
        "UTXO action"), REGISTER_CONTRACT("REGISTER_CONTRACT", "register contract action"), BIND_CONTRACT(
        "BIND_CONTRACT", "bind contract action"), TRIGGER_CONTRACT("TRIGGER_CONTRACT",
        "trigger contract action"), REGISTER_RS("REGISTER_RS", "register RS action"), REGISTER_POLICY("REGISTER_POLICY",
        "register policy action"), CREATE_DATA_IDENTITY("CREATE_DATA_IDENTITY", "create data identity"), ISSUE_CURRENCY(
        "ISSUE_CURRENCY", "issue new currency"), CA_AUTH("CA_AUTH", "ca auth"), CA_CANCEL("CA_CANCEL",
        "ca cancel"), CA_UPDATE("CA_UPDATE", "ca update"), RS_CANCEL("RS_CANCEL", "cancel rs"),;

    String code;
    String desc;

    ActionTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ActionTypeEnum getActionTypeEnumBycode(String code) {
        for (ActionTypeEnum actionTypeEnum : ActionTypeEnum.values()) {
            if (actionTypeEnum.getCode().equals(code)) {
                return actionTypeEnum;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
