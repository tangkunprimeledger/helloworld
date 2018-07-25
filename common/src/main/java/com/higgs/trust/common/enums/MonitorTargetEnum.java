package com.higgs.trust.common.enums;

/**
 * @author tangfashuang
 * @date 2018/7/25 15:25
 * @desc monitor target enum
 */
public enum MonitorTargetEnum {
    SLAVE_BATCH_INSERT_PENDING_TX_ERROR("批量插入pengding_tx异常", "slave_batch_insert_pending_tx_error"),
    SLAVE_PENDING_TRANSACTION_IDEMPOTENT_EXCEPTION("批量插入pending_tx幂等", "slave_pending_transaction_idempotent_exception"),
    SLAVE_PENDING_TX_STATUS_EXCEPTION("pending_tx状态异常", "slave_pending_tx_status_exception"),
    SLAVE_PENDING_TX_TO_SIGNED_TX_EXCEPTION("pengding_tx转换成signed_tx异常", "slave_pending_tx_to_signed_tx_exception"),
    SLAVE_GENESIS_BLOCK_NOT_EXISTS("genesis block不存在", "slave_genesis_block_not_exists"),
    SLAVE_SEND_PACKAGE_TO_CONSENSUS_TIMEOUT("发送package给协议层超时", "slave_send_package_to_consensus_timeout"),
    SLAVE_PACKAGE_HASH_NOT_EQUAL("相同高度的packagehash不一致", "slave_package_hash_not_equal"),
    SLAVE_BLOCK_HEADER_NOT_EQUAL("blockheader不一致", "slave_block_header_not_equal")
    ;

    MonitorTargetEnum(String description, String monitorTarget) {
        this.description = description;
        this.monitorTarget = monitorTarget;
    }

    MonitorTargetEnum(String description) {
        this.description = description;
        this.monitorTarget = "";
    }

    private String description;

    /**
     * 对于这个异常监控点名字，达到监控异常点日志中进行监控采集使用
     */
    private String monitorTarget;

    public String getDescription() {
        return description;
    }

    public String getMonitorTarget() {
        return monitorTarget;
    }
}