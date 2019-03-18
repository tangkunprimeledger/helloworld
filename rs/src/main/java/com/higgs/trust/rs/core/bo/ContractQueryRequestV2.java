package com.higgs.trust.rs.core.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author Chen Jiawei
 * @date 2018-12-12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractQueryRequestV2 {
    /**
     * Height of block.
     */
    private Long blockHeight;
    /**
     * Contract address, hex string of 40 characters, e.g. 00a615668486da40f31fd050854fb137b317e056.
     */
    @NotBlank
    private String address;
    /**
     * Method signature, e.g. (uint256) get(uint256).
     */
    @NotBlank
    private String methodSignature;
    /**
     * Method input arguments, e.g. 4.
     */
    private Object[] parameters;
}
