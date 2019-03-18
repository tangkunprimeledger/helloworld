package com.higgs.trust.slave.dao.po.config;

import com.higgs.trust.common.mybatis.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author WangQuanzhou
 * @desc TODO
 * @date 2018/6/5 13:05
 */
@Getter @Setter public class ClusterNodePO extends BaseEntity {

    private String nodeName;
    /**
     * node whether join in p2p
     */
    private boolean p2pStatus;
    /**
     * node whether registered as rs
     */
    private boolean rsStatus;

    private Date createTime;

    private Date updateTime;
}
