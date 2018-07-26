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
    SLAVE_BATCH_INSERT_PENDING_TX_ERROR("batch insert pending transaction error", "slave_batch_insert_pending_tx_error"),
    SLAVE_PENDING_TRANSACTION_IDEMPOTENT_EXCEPTION("batch insert pending transaction idempotent", "slave_pending_transaction_idempotent_exception"),
    SLAVE_PENDING_TX_STATUS_EXCEPTION("pending transaction status exception", "slave_pending_tx_status_exception"),
    SLAVE_PENDING_TX_TO_SIGNED_TX_EXCEPTION("pending transaction convert to signed transaction exception", "slave_pending_tx_to_signed_tx_exception"),
    SLAVE_GENESIS_BLOCK_NOT_EXISTS("genesis block is not exist", "slave_genesis_block_not_exists"),
    SLAVE_SEND_PACKAGE_TO_CONSENSUS_TIMEOUT("send package to consensus timeout", "slave_send_package_to_consensus_timeout"),
    SLAVE_PACKAGE_HASH_NOT_EQUAL("package is not equal", "slave_package_hash_not_equal"),
    SLAVE_BLOCK_HEADER_NOT_EQUAL("block header is not equal", "slave_block_header_not_equal"),
    SLAVE_PACKAGE_PROCESS_ERROR("package process unknown exception", "slave_package_process_error"),
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
    STARTUP_FAILED("启动失败", "startup_failed"),
    REJECTED_PACKAGE_COMMAND("拒绝package command", "rejected_package_command"),
    SUBMIT_CHANGE_MASTER_COMMAND_FAILED("提交切换master command失败", "submit_change_master_command_failed"),
    FAILOVER_BLOCK_ERROR("failover the block failed","failover_block_error"),
    SELF_CHECK_FAILED("self check failed", "self_check_failed"),
    SYNC_BLOCKS_FAILED("sync block failed", "sync_blocks_failed"),
    SLAVE_ACQUIRE_PUBKEY_ERROR("slave acquire pubKey error", "slave_acquire_pubKey_error"),
    SLAVE_GENERATE_GENIUS_BLOCK_ERROR("slave generate genius block error", "slave_generate_genius_block_error"),
    SLAVE_CA_UPDATE_ERROR("slave ca update error", "slave_ca_update_error"),
    SLAVE_CA_CANCEL_ERROR("slave ca cancel error", "slave_ca_cancel_error"),
    SLAVE_CA_AUTH_ERROR("slave ca auth error", "slave_ca_auth_error"),
    SLAVE_NODE_JOIN_ERROR("slave node join error", "slave_node_join_error"),
    SLAVE_NODE_LEAVE_ERROR("slave node leave error", "slave_node_leave_error"),
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