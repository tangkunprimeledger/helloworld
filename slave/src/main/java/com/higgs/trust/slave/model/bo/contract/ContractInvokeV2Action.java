package com.higgs.trust.slave.model.bo.contract;

import com.higgs.trust.slave.model.bo.action.Action;
import lombok.Getter;
import lombok.Setter;

/**
 * @author kongyu
 * @description the action of contract invoke
 * @date 2018-11-30
 */
@Getter
@Setter
public class ContractInvokeV2Action extends Action {

    private String address;
    private Object[] args;

}
