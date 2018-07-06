package com.higgs.trust.slave.model.bo.node;

import com.higgs.trust.slave.model.bo.action.Action;
import lombok.Getter;
import lombok.Setter;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/5 16:15
 */
@Getter @Setter public class NodeAction<T> extends Action {

    private String nodeName;

}
