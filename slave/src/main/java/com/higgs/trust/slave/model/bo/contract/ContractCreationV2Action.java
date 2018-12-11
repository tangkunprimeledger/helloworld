package com.higgs.trust.slave.model.bo.contract;

import com.higgs.trust.slave.model.bo.action.Action;
import lombok.Getter;
import lombok.Setter;

/**
 * @author tangkun
 * @description smart contract create action v2
 * @date 2018-11-29
 */
@Getter
@Setter
public class ContractCreationV2Action extends Action {

    private String version;
    private String code;

    /**
     * if transfer，which is tx's nonce
     */
    private Long nonce;

}
