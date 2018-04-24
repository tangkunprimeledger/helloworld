package com.higgs.trust.slave.model.bo.manage;

import com.higgs.trust.slave.model.bo.action.Action;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Set;

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
    private Set<String> rsIdSet;

}
