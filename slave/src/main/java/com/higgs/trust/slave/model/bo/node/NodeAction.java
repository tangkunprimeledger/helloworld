package com.higgs.trust.slave.model.bo.node;

import com.higgs.trust.slave.model.bo.action.Action;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/5 16:15
 */
@Getter @Setter public class NodeAction<T> extends Action {
    /**
     * node name of join or leave
     */
    @NotNull private String nodeName;
    /**
     * the sign of original value
     */
    @NotNull private String selfSign;
    /**
     * the original value for sign
     */
    @NotNull private String signValue;
    /**
     * node pubKey
     */
    @NotNull private String pubKey;
}
