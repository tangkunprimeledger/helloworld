package com.higgs.trust.rs.core.bo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

/**
 * @author lingchao
 * @date 2019-01-06
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContractQueryStateV2BO implements Serializable {
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
     * Method, e.g. (uint256) get(uint256).
     */
    @NotBlank
    private String method;
    /**
     * Method input arguments, e.g. 4.
     */
    private Object[] parameters;
}
