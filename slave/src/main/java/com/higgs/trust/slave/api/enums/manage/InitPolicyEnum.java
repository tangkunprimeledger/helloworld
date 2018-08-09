package com.higgs.trust.slave.api.enums.manage;

/**
 * @author tangfashuang
 * @date 2018/04/13 16:33
 * @desc initial policy
 *
 */
public enum InitPolicyEnum {
    NA("NA", "000000", VotePatternEnum.SYNC,DecisionTypeEnum.FULL_VOTE, "NA type"),
    REGISTER_POLICY("REGISTER_POLICY", "000001",VotePatternEnum.ASYNC,DecisionTypeEnum.FULL_VOTE, "register policy"),
    REGISTER_RS("REGISTER_RS", "000002",VotePatternEnum.ASYNC,DecisionTypeEnum.FULL_VOTE, "register rs"),
    UTXO_ISSUE("UTXO_ISSUE", "000003", VotePatternEnum.SYNC,DecisionTypeEnum.FULL_VOTE,"utxo issue"),
    UTXO_DESTROY("UTXO_DESTROY", "000004", VotePatternEnum.SYNC,DecisionTypeEnum.FULL_VOTE,"utxo destroy"),
    CONTRACT_ISSUE("CONTRACT_ISSUE", "000005", VotePatternEnum.SYNC,DecisionTypeEnum.FULL_VOTE,"contract issue"),
    CONTRACT_DESTROY("CONTRACT_DESTROY", "000006",VotePatternEnum.SYNC,DecisionTypeEnum.FULL_VOTE, "contract destroy"),
    CA_AUTH("CA_AUTH", "000007",VotePatternEnum.ASYNC,DecisionTypeEnum.FULL_VOTE,"ca auth"),
    CA_UPDATE("CA_UPDATE", "000008",VotePatternEnum.ASYNC,DecisionTypeEnum.FULL_VOTE,"ca update"),
    CA_CANCEL("CA_CANCEL", "000009",VotePatternEnum.ASYNC,DecisionTypeEnum.FULL_VOTE,"ca cancel"),
    CANCEL_RS("CANCEL_RS", "000010", VotePatternEnum.ASYNC,DecisionTypeEnum.FULL_VOTE, "cancel rs"),
    NODE_JOIN("NODE_JOIN", "000011",VotePatternEnum.ASYNC,DecisionTypeEnum.FULL_VOTE,"node join"),
    NODE_LEAVE("NODE_LEAVE", "000012",VotePatternEnum.ASYNC,DecisionTypeEnum.FULL_VOTE,"node leave"),;
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
