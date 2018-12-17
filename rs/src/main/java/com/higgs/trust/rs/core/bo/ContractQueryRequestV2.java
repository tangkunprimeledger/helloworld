package com.higgs.trust.rs.core.bo;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Chen Jiawei
 * @date 2018-12-12
 */
@Getter
@Setter
public class ContractQueryRequestV2 {
    /**
     * Height of block.
     */
    private int blockHeight;
    /**
     * Contract address, hex string of 40 characters, e.g. 00a615668486da40f31fd050854fb137b317e056.
     */
    private String address;
    /**
     * Method signature, e.g. (uint256) get(uint256).
     */
    private String methodSignature;
    /**
     * Method input arguments, e.g. 4.
     */
    private Object[] parameters;
}
