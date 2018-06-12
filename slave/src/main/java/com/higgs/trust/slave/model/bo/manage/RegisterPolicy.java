package com.higgs.trust.slave.model.bo.manage;

import com.higgs.trust.slave.api.enums.manage.DecisionTypeEnum;
import com.higgs.trust.slave.model.bo.action.Action;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author tangfashuang
 * @desc register policy action
 * @date 2018-03-27
 */
@Getter @Setter public class RegisterPolicy extends Action {
    /**
     * policy id
     */
    @NotBlank
    @Length(max = 32)
    private String policyId;

    /**
     * policy name
     */
    @NotBlank
    @Length(max = 64)
    private String policyName;

    /**
     * rs ids of related to policy
     */
    @NotEmpty
    private List<String> rsIds;

    /**
     * the decision type for vote ,1.FULL_VOTE,2.ONE_VOTE
     */
    @NotNull
    private DecisionTypeEnum decisionType;
    /**
     * the contract address for vote rule
     */
    @NotNull
    private String contractAddr;

}
