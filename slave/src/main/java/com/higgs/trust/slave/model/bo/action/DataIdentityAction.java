package com.higgs.trust.slave.model.bo.action;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

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
    @NotNull @Length(max = 64) private String identity;
    /**
     * chain of owner
     */
    @NotNull @Length(max = 24) private String chainOwner;
    /**
     * data owner
     */
    @NotNull @Length(max = 24) private String dataOwner;
}
