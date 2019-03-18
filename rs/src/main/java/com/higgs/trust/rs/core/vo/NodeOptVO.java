package com.higgs.trust.rs.core.vo;

import com.higgs.trust.rs.common.BaseBO;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author liuyu
 * @description
 * @date 2018-09-05
 */
@Getter
@Setter
public class NodeOptVO extends BaseBO {
    /**
     * node name of join or leave
     */
    @NotNull private String nodeName;
    /**
     * the sign of original value
     */
    @NotNull private String sign;
    /**
     * the original value for sign
     */
    @NotNull private String signValue;
    /**
     * node pubKey
     */
    @NotNull private String pubKey;
}
