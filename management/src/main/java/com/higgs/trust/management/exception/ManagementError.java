/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.management.exception;

import com.higgs.trust.common.exception.ErrorInfo;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;

/**
 * @author suimi
 * @date 2018/6/13
 */
public enum ManagementError implements ErrorInfo {

    //@formatter:off
    //\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\//
    //                         Failover相关[400-500]                           //
    //\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\//
    MANAGEMENT_FAILOVER_STATE_NOT_ALLOWED("400", "node state not allowed current operation ", false),
    MANAGEMENT_FAILOVER_START_HEIGHT_ERROR("410", "the start height error, please check", false),
    MANAGEMENT_FAILOVER_GET_VALIDATING_BLOCKS_FAILED("411", "get and validating the blocks from other node failed", false),
    MANAGEMENT_FAILOVER_GET_VALIDATING_HEADERS_FAILED("412", "get and validating the block headers from other node failed", false),
    MANAGEMENT_FAILOVER_SYNC_BLOCK_VALIDATING_FAILED("413", "the package of block validating failed when sync block.", false),
    MANAGEMENT_FAILOVER_SYNC_BLOCK_PERSIST_RESULT_INVALID("414", "the package of block persist result invalid after sync block.", false),
    MANAGEMENT_FAILOVER_BLOCK_VALIDATE_RESULT_INVALID("415", "the package of block validating result invalid after failover block.", false),
    MANAGEMENT_FAILOVER_BLOCK_PERSIST_RESULT_INVALID("416", "the package of block persist result invalid after failover block.", false),
    MANAGEMENT_FAILOVER_CONSENSUS_VALIDATE_NOT_EXIST("417","consensus validate result not exist",false),
    MANAGEMENT_FAILOVER_CONSENSUS_PERSIST_NOT_EXIST("418","consensus persist result not exist",false),
    ;
    //@formatter:on

    /**
     * 枚举编码
     */
    private final String code;

    /**
     * 描述说明
     */
    private final String description;

    /**
     * 是否需要重试
     */
    private final boolean needRetry;

    /**
     * 私有构造函数。
     *
     * @param code        枚举编码
     * @param description 描述说明
     */
    private ManagementError(String code, String description, boolean needRetry) {
        this.code = code;
        this.description = description;
        this.needRetry = needRetry;
    }

    /**
     * @return Returns the code.
     */
    public String getCode() {
        return code;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return
     */
    public boolean isNeedRetry() {
        return needRetry;
    }

    /**
     * 通过枚举<code>code</code>获得枚举
     *
     * @param code 枚举编码
     * @return 错误场景枚举
     */
    public static ManagementError getByCode(String code) {
        for (ManagementError scenario : values()) {
            if (scenario.getCode().equals(code)) {

                return scenario;
            }
        }
        return null;
    }

}
