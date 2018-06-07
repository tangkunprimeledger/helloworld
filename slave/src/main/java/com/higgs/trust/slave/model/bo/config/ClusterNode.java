package com.higgs.trust.slave.model.bo.config;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/5 13:05
 */
@Getter @Setter public class ClusterNode extends BaseBO {

    private String nodeName;
    private String p2pStatus;
    private String rsStatus;
}
