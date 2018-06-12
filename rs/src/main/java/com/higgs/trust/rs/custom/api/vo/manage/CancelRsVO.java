package com.higgs.trust.rs.custom.api.vo.manage;

import com.higgs.trust.rs.custom.api.vo.BaseVO;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

@Getter
@Setter
public class CancelRsVO extends BaseVO{
    @NotBlank
    @Length(max = 64)
    private String requestId;

    /**
     * rs id
     */
    @NotBlank
    @Length(max = 32)
    private String rsId;
}
