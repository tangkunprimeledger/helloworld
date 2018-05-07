package com.higgs.trust.slave.model.bo.action;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

/**
 * data identity action
 *
 * @author lingchao
 * @create 2018年03月30日18:15
 */
@Getter @Setter public class DataIdentityAction extends Action {
    /**
     * identity of data
     */
    @NotBlank @Length(max = 64) private String identity;
    /**
     * chain of owner
     */
    @NotBlank @Length(max = 24) private String chainOwner;
    /**
     * data owner
     */
    @NotBlank @Length(max = 24) private String dataOwner;
}
