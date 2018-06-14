package com.higgs.trust.slave.model.bo.manage;

import com.higgs.trust.slave.model.bo.action.Action;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author tangfashuang
 * @desc register rs action
 * @date 2018-03-27
 */
@Getter @Setter public class RegisterRS extends Action {
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
