package com.higgs.trust.slave.model.bo.contract;

import com.higgs.trust.slave.model.bo.action.Action;
import lombok.Getter;
import lombok.Setter;

/**
 * @author duhongming
 * @date 2018/6/21
 */
@Getter
@Setter
public class ContractStateMigrationAction extends Action {
    private String formInstanceAddress;
    private String toInstanceAddress;
}
