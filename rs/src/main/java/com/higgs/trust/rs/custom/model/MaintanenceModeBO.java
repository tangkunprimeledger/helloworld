package com.higgs.trust.rs.custom.model;

import com.higgs.trust.rs.common.BaseBO;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author yangjiyun
 * @create 2017 -06-21 15:32
 */
@Getter @Setter public class MaintanenceModeBO extends BaseBO {
    @NotNull private Boolean maintanenceMode;

}
