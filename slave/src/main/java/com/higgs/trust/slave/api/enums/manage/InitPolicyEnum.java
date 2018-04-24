package com.higgs.trust.slave.api.enums.manage;

/**
 * @author tangfashuang
 * @date 2018/04/13 16:33
 * @desc initial policy
 *
 */
public enum InitPolicyEnum {
    REGISTER("REGISTER", "000000", "register rs or policy"),
    UTXO_ISSUE("UTXO_ISSUE", "000001", "utxo issue"),
    UTXO_DESTROY("UTXO_DESTROY", "000002", "utxo destroy"),
    CONTRACT_ISSUE("CONTRACT_ISSUE", "000003", "contract issue"),
    CONTRACT_DESTROY("CONTRACT_DESTROY", "000004", "contract destroy");

    private String type;

    private String policyId;

    private String desc;

    InitPolicyEnum(String type, String policyId, String desc) {
        this.type = type;
        this.policyId = policyId;
        this.desc = desc;
    }

    public static InitPolicyEnum getInitPolicyEnumByType(String type) {
        for (InitPolicyEnum initPolicyEnum : InitPolicyEnum.values()) {
            if (initPolicyEnum.getType().equals(type)) {
                return initPolicyEnum;
            }
        }
        return null;
    }

    public static InitPolicyEnum getInitPolicyEnumByPolicyId(String policyId) {
        for (InitPolicyEnum initPolicyEnum : InitPolicyEnum.values()) {
            if (initPolicyEnum.getPolicyId().equals(policyId)) {
                return initPolicyEnum;
            }
        }
        return null;
    }

    public String getType() {
        return type;
    }

    public String getPolicyId() {
        return policyId;
    }

    public String getDesc() {
        return desc;
    }
}
