package com.higgs.trust.rs.custom.api.vo.manage;

import com.higgs.trust.rs.custom.api.vo.BaseVO;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author tangfashuang
 */
@Getter
@Setter
public class RegisterRsVO extends BaseVO{

    @NotBlank
    @Length(max = 64)
    private String requestId;

    /**
     * rs id
     */
    @NotBlank
    @Length(max = 32)
    private String rsId;

    /**
     * description of the rs
     */
    @NotBlank
    @Length(max = 128)
    private String desc;
}
