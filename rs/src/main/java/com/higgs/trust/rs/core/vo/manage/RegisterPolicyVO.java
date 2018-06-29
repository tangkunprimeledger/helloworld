package com.higgs.trust.rs.core.vo.manage;

import com.higgs.trust.rs.core.vo.BaseVO;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author tangfashuang
 * @date 2018/05/18 14:13
 * @desc
 */
@Getter
@Setter
public class RegisterPolicyVO extends BaseVO {

    @NotBlank
    @Length(max = 64)
    private String requestId;
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
     * rs vote pattern 1.SYNC 2.ASYNC
     */
    @NotNull private String votePattern;

    /**
     * callback type of slave 1.ALL 2.SELF
     */
    @NotNull private String callbackType;

    @NotNull
    private String decisionType;

    /**
     * rs ids of related to policy
     */
    @NotEmpty
    private List<String> rsIds;
}
