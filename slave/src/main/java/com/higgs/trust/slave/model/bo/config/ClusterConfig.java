package com.higgs.trust.slave.model.bo.config;

import com.higgs.trust.slave.model.bo.BaseBO;
import lombok.Getter;
import lombok.Setter;

/**
 * @author WangQuanzhou
 * @desc cluster configuration
 * @date 2018/6/5 10:27
 */
@Getter @Setter public class ClusterConfig extends BaseBO {
    private String clusterName;

    private int nodeNum;

    private int faultNum;
}
