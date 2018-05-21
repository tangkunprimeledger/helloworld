package com.higgs.trust.rs.custom.api.vo.manage;

import com.higgs.trust.rs.custom.api.vo.BaseVO;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

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
     * rs ids of related to policy
     */
    @NotEmpty
    private List<String> rsIds;
}
