package com.higgs.trust.slave.api.vo;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lingchao
 * @date 2018/8/8
 */
@Getter
@Setter
public class NodeInfoVO extends BaseBO {
    /**
     * node name
     */
    private String nodeName;
    /**
     * block height
     */
    private Long height;
    /**
     * is master
     */
    private boolean isMaster;

    /**
     * node state
     */
    private String nodeState;
}
