package com.higgs.trust.slave.dao.po.config;

import com.higgs.trust.common.mybatis.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @desc node configuration
 * @author WangQuanzhou
 * @date 2018/6/5 10:27    
 */
@Getter @Setter public class ConfigPO extends BaseEntity {
    private String version;

    private boolean valid;

    private String pubKey;

    private String priKey;

    private String tmpPubKey;

    private String tmpPriKey;

    private String nodeName;

    private Date createTime;

    private Date updateTime;
}
