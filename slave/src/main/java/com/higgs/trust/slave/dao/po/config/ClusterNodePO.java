package com.higgs.trust.slave.dao.po.config;

import com.higgs.trust.slave.dao.po.BaseEntity;
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
    private boolean p2pStatus;
    private boolean rsStatus;

    private Date createTime;

    private Date updateTime;
}
