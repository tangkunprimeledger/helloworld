package com.higgs.trust.slave.api.enums.manage;

/**
 * @author tangfashuang
 * @date 2018/04/13 16:33
 * @desc initial policy
 *
 */
public enum InitPolicyEnum {
    REGISTER_POLICY("REGISTER_POLICY", "000001",VotePatternEnum.ASYNC,DecisionTypeEnum.FULL_VOTE, "register policy"),
    REGISTER_RS("REGISTER_RS", "000002",VotePatternEnum.ASYNC,DecisionTypeEnum.FULL_VOTE, "register rs"),
    UTXO_ISSUE("UTXO_ISSUE", "000003", VotePatternEnum.SYNC,DecisionTypeEnum.FULL_VOTE,"utxo issue"),
    UTXO_DESTROY("UTXO_DESTROY", "000004", VotePatternEnum.SYNC,DecisionTypeEnum.FULL_VOTE,"utxo destroy"),
    CONTRACT_ISSUE("CONTRACT_ISSUE", "000005", VotePatternEnum.SYNC,DecisionTypeEnum.FULL_VOTE,"contract issue"),
    CONTRACT_DESTROY("CONTRACT_DESTROY", "000006",VotePatternEnum.SYNC,DecisionTypeEnum.FULL_VOTE, "contract destroy"),
    CA_AUTH("CA_AUTH", "000007",VotePatternEnum.SYNC,DecisionTypeEnum.FULL_VOTE,"CA AUTH"),
    CA_UPDATE("CA_UPDATE", "000008",VotePatternEnum.SYNC,DecisionTypeEnum.FULL_VOTE,"CA UPDATE"),
    CA_CANCEL("CA_CANCEL", "000009",VotePatternEnum.SYNC,DecisionTypeEnum.FULL_VOTE,"CA CANCEL"),
    CANCEL_RS("CANCEL_RS", "000010", VotePatternEnum.SYNC,DecisionTypeEnum.FULL_VOTE, "cancel rs"),
    NA("NA", "000011", VotePatternEnum.SYNC,DecisionTypeEnum.FULL_VOTE, "NA type");
    private String type;

    private String policyId;

    private VotePatternEnum votePattern;

    private DecisionTypeEnum decisionType;

    private String desc;

    InitPolicyEnum(String type, String policyId,VotePatternEnum votePattern,DecisionTypeEnum decisionType,String desc) {
        this.type = type;
        this.policyId = policyId;
        this.votePattern = votePattern;
        this.decisionType = decisionType;
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

    public VotePatternEnum getVotePattern(){ return votePattern;}

    public DecisionTypeEnum getDecisionType() {
        return decisionType;
    }

    public String getDesc() {
        return desc;
    }
}
