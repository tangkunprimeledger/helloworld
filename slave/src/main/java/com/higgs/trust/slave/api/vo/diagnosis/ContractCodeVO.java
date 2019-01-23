package com.higgs.trust.slave.api.vo.diagnosis;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Chen Jiawei
 * @date 2019-01-21
 */
@Getter
@Setter
public class ContractCodeVO extends BaseBO {
    /**
     * Contract address, a hex string with 40 characters.
     */
    private String address;
    /**
     * Height of block in which contract exists.
     */
    private Long height;
    /**
     * Contract stored code.
     */
    private String code;
}
