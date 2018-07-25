package com.higgs.trust.common.enums;

/**
 * @author tangfashuang
 * @date 2018/7/25 15:25
 * @desc monitor target enum
 */
public enum MonitorTargetEnum {

    SLAVE_DATA_NOT_EXIST_EXCEPTION("slave data not exist exception", "slave_data_not_exist_exception"),
    SLAVE_DATA_NOT_UPDATED_EXCEPTION("slave data not updated exception", "slave_data_not_updated_exception"),
    SLAVE_DUPLICATE_KEY_EXCEPTION("slave duplicate key exception", "slave_duplicate_key_exception"),
    SLAVE_BATCH_INSERT_PENDING_TX_ERROR("批量插入pengding_tx异常", "slave_batch_insert_pending_tx_error"),
    SLAVE_PENDING_TRANSACTION_IDEMPOTENT_EXCEPTION("批量插入pending_tx幂等", "slave_pending_transaction_idempotent_exception"),
    SLAVE_PENDING_TX_STATUS_EXCEPTION("pending_tx状态异常", "slave_pending_tx_status_exception"),
    SLAVE_PENDING_TX_TO_SIGNED_TX_EXCEPTION("pengding_tx转换成signed_tx异常", "slave_pending_tx_to_signed_tx_exception"),
    SLAVE_GENESIS_BLOCK_NOT_EXISTS("genesis block不存在", "slave_genesis_block_not_exists"),
    SLAVE_SEND_PACKAGE_TO_CONSENSUS_TIMEOUT("发送package给协议层超时", "slave_send_package_to_consensus_timeout"),
    SLAVE_PACKAGE_HASH_NOT_EQUAL("相同高度的packagehash不一致", "slave_package_hash_not_equal"),
    SLAVE_BLOCK_HEADER_NOT_EQUAL("blockheader不一致", "slave_block_header_not_equal"),
    SLAVE_PACKAGE_PROCESS_ERROR("package处理发生未知异常", "slave_package_process_error"),
    RS_WAIT_TIME_OUT_ERROR("RS等待超时异常", "rs_wait_time_out_error"),
    RS_SUBMIT_TO_SLAVE_ERROR("RS提交交易到SLAVE发生未知异常", "rs_submit_to_slave_error"),
    SLAVE_UTXO_ACTION_TYPE_NOT_LEGAL_EXCEPTION("UTXO action type not legal exception", "slave_utxo_action_type_not_legal_exception"),
    SLAVE_UTXO_CONTRACT_ADDRESS_NOT_LEGAL_EXCEPTION("UTXO contract address not legal exception", "slave_utxo_contract_address_not_legal_exception"),
    SLAVE_UTXO_ACTION_INDEX_NOT_LEGAL_EXCEPTION("UTXO contract address not legal exception", "slave_utxo_action_index_not_legal_exception"),
    SLAVE_UTXO_DATA_IDENTITY_NOT_LEGAL_EXCEPTION("UTXO action data identity not legal exception", "slave_utxo_data_identity_not_legal_exception"),
    SLAVE_SNAPSHOT_DUPLICATE_KEY_EXCEPTION("slave snapshot duplicate key exception", "slave_snapshot_duplicate_key_exception"),
    SLAVE_SNAPSHOT_DATA_NOT_EXIST_EXCEPTION("slave snapshot data not exist exception", "slave_snapshot_data_not_exist_exception"),
    SLAVE_SNAPSHOT_BIZ_KEY_NOT_EXCEPTION("slave snapshot biz key not exception", "slave_snapshot_biz_key_not_exception"),
    SLAVE_SNAPSHOT_QUERY_EXCEPTION("slave snapshot query exception", "slave_snapshot_query_exception"),
    SLAVE_SNAPSHOT_PACKAGE_OVERSIZE_EXCEPTION("slave snapshot package oversize exception", "slave_snapshot_package_oversize_exception"),
    SLAVE_SNAPSHOT_FLUSH_EXCEPTION("slave snapshot flush exception", "slave_snapshot_flush_exception"),
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