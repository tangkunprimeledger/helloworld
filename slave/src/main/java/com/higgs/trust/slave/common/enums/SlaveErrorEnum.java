package com.higgs.trust.slave.common.enums;

/**
 * @Description:
 * @author: pengdi
 **/
public enum SlaveErrorEnum {
    //@formatter:off
    //\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\//
    //                      公共类错误码[000-099,999]                           //
    //\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\//

    SLAVE_UNKNOWN_EXCEPTION("999", "其它未知异常", true),

    SLAVE_CONFIGURATION_ERROR("000", "配置错误", true),

    //\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\//
    //                         请求校检[100-299]                                //
    //\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\//
    SLAVE_PARAM_VALIDATE_ERROR("100", "param validate error", false),
    SLAVE_IDEMPOTENT("101", "request idempotent", false),
    SLAVE_TX_VERIFY_SIGNATURE_FAILED("102", "transaction verify signature failed", false),
    SLAVE_PACKAGE_VERIFY_SIGNATURE_FAILED("103", "package verify master node signature failed", false),
    SLAVE_PACKAGE_SIGN_SIGNATURE_FAILED("103", "package sign signature failed", false),
    //\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\//
    //                         查询相关[300-399]                                //
    //\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\//
    SLAVE_CONSENSUS_GET_RESULT_FAILED("301", "get the consensus result failed.", true),

    //\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\//
    //                         Failover相关[400-499]                           //
    //\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\//
    SLAVE_FAILOVER_STATE_NOT_ALLOWED("400", "node state not allowed current operation ", false),
    SLAVE_FAILOVER_STATE_CHANGE_FAILED("401", "node state change failed ", false),
    SLAVE_FAILOVER_START_HEIGHT_ERROR("410", "the start height error, please check", false),
    SLAVE_FAILOVER_GET_VALIDATING_BLOCKS_FAILED("411", "get and validating the blocks from other node failed", false),
    SLAVE_FAILOVER_GET_VALIDATING_HEADERS_FAILED("412", "get and validating the block headers from other node failed", false),
    SLAVE_FAILOVER_SYNC_BLOCK_VALIDATING_FAILED("413", "the package of block validating failed when sync block.", false),
    SLAVE_FAILOVER_SYNC_BLOCK_PERSIST_RESULT_INVALID("414", "the package of block persist result invalid after sync block.", false),
    SLAVE_FAILOVER_BLOCK_VALIDATE_RESULT_INVALID("415", "the package of block validating result invalid after failover block.", false),
    SLAVE_FAILOVER_BLOCK_PERSIST_RESULT_INVALID("416", "the package of block persist result invalid after failover block.", false),
    SLAVE_FAILOVER_CONSENSUS_VALIDATE_NOT_EXIST("417","consensus validate result not exist",false),
    SLAVE_FAILOVER_CONSENSUS_PERSIST_NOT_EXIST("418","consensus persist result not exist",false),

    //\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\//
    //                         内部处理相关[500-699]                            //
    //\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\//
    SLAVE_TX_OUT_NOT_EXISTS_ERROR("501", "txOut is not exists", false),
    SLAVE_DATA_IDENTITY_NOT_EXISTS_ERROR("502", "dataidentity is not exists", false),
    SLAVE_UTXO_IS_DOUBLE_SPEND_ERROR("503", "utxo is double spend", false),
    SLAVE_UTXO_CONTRACT_PROCESS_FAIL_ERROR("504", "utxo contract process fail", false),
    SLAVE_POLICY_EXISTS_ERROR("505", "policy is already exists", false),
    SLAVE_RS_EXISTS_ERROR("506", "RS is already exists", false),
    SLAVE_DATA_NOT_UPDATE_EXCEPTION("507", "data not update  exception", false),
    SLAVE_POLICY_IS_NOT_EXISTS_EXCEPTION("508", "policy is not exists exception", false),
    SLAVE_ACTION_HANDLER_IS_NOT_EXISTS_EXCEPTION("509", "action handler is not exists exception", false),
    SLAVE_SNAPSHOT_BIZ_KEY_NOT_EXISTED_EXCEPTION("510", "snapshot core key not existed exception", false),
    SLAVE_SNAPSHOT_NOT_INIT_EXCEPTION("511", "snapshot  not init exception", false),
    SLAVE_SNAPSHOT_TRANSACTION_NOT_STARTED_EXCEPTION("512", "snapshot transaction not started exception", false),
    SLAVE_SNAPSHOT_TRANSACTION_HAS_STARTED_EXCEPTION("513", "snapshot transaction has started exception", false),
    SLAVE_SNAPSHOT_NULL_POINTED_EXCEPTION("514", "snapshot  null point  exception", false),
    SLAVE_SNAPSHOT_QUERY_EXCEPTION("515", "snapshot  query exception", false),
    SLAVE_S_TX_OUT_NOT_EXISTS_ERROR("516", "S txOut is not exists", false),
    SLAVE_ACTION_NOT_EXISTS_EXCEPTION("517", "action not exists exception", false),
    SLAVE_SNAPSHOT_CACHE_SIZE_NOT_ENOUGH_EXCEPTION("518", "snapshot cache size not enough exception", false),
    SLAVE_SNAPSHOT_GET_NO_LOCK_EXCEPTION("519", "snapshot get no lock exception", false),


    SLAVE_MERKLE_PARAM_NOT_VALID_EXCEPTION("600", "slave merkle param not valid exception", false),
    SLAVE_MERKLE_NON_EXIST_EXCEPTION("601", "slave merkle non exist exception", false),
    SLAVE_MERKLE_UPDATE_PARENT_EXCEPTION("602", "slave merkle update parent exception", false),
    SLAVE_MERKLE_CALCULATE_INDEX_EXCEPTION("603", "slave merkle calculate index exception", false),
    SLAVE_MERKLE_CALCULATE_HASH_EXCEPTION("604", "slave merkle calculate hash exception", false),
    SLAVE_MERKLE_NODE_NON_EXIST_EXCEPTION("605", "slave merkle node non exist exception", false),
    SLAVE_MERKLE_NODE_ADD_IDEMPOTENT_EXCEPTION("606", "slave merkle node add idempotent exception", false),
    SLAVE_MERKLE_NODE_ADD_EXCEPTION("607", "slave merkle node add exception", false),
    SLAVE_MERKLE_NODE_UPDATE_EXCEPTION("608", "slave merkle node update exception", false),
    SLAVE_MERKLE_ALREADY_EXIST_EXCEPTION("609", "slave merkle already exist exception", false),
    SLAVE_MERKLE_NODE_BUILD_DUPLICATE_EXCEPTION("610", "slave merkle node build duplicate exception", false),

    SLAVE_ACCOUNT_CURRENCY_NOT_EXISTS_ERROR("800", "currency is not exists", false),
    SLAVE_ACCOUNT_TRIAL_BALANCE_ERROR("801", "trial balance check error", false),
    SLAVE_ACCOUNT_IS_NOT_EXISTS_ERROR("802", "account is not exists error", false),
    SLAVE_ACCOUNT_CHANGE_BALANCE_ERROR("803", "change account balance is error", false),
    SLAVE_ACCOUNT_BALANCE_IS_NOT_ENOUGH_ERROR("804", "account balance is not enough error", false),
    SLAVE_ACCOUNT_STATUS_IS_DESTROY_ERROR("805", "account status is destroy error", false),
    SLAVE_ACCOUNT_FUND_DIRECTION_IS_NULL_ERROR("806", "account fund direction is null error", false),
    SLAVE_ACCOUNT_CURRENCY_IS_NOT_CONSISTENT_ERROR("807", "account currency is not consistent error", false),
    SLAVE_ACCOUNT_CHECK_DATA_OWNER_ERROR("808", "account check data owner error", false),
    SLAVE_ACCOUNT_FREEZE_AMOUNT_ERROR("809", "account check freeze amount error", false),
    SLAVE_ACCOUNT_FREEZE_ERROR("810", "account freeze error", false),
    SLAVE_ACCOUNT_FREEZE_RECORD_IS_NOT_EXISTS_ERROR("811", "account freeze record is not exists error", false),
    SLAVE_ACCOUNT_UNFREEZE_AMOUNT_ERROR("812", "account check unfreeze amount error", false),
    SLAVE_ACCOUNT_UNFREEZE_ERROR("813", "account unfreeze error", false),
    SLAVE_ACCOUNT_MERKLE_TREE_NOT_EXIST_ERROR("814", "account merkletree not exist error", false),
    SLAVE_PACKAGE_BUILD_TX_ROOT_HASH_ERROR("815", "package build root hash error", false),
    SLAVE_PACKAGE_BUILD_TX_RECEIPT_ROOT_HASH_ERROR("816", "package build receipt root hash error", false),
    SLAVE_PACKAGE_GET_BLOCK_ERROR("817", "package get block error", false),
    SLAVE_PACKAGE_BLOCK_HEIGHT_UNEQUAL_ERROR("818", "package block height unequal error", false),
    SLAVE_PACKAGE_TXS_IS_EMPTY_ERROR("819", "package txs is empty error", false),
    SLAVE_PACKAGE_VALIDATING_ERROR("820", "package validating error", false),
    SLAVE_PACKAGE_UPDATE_STATUS_ERROR("821", "package update status error", false),
    SLAVE_PACKAGE_HEADER_IS_NULL_ERROR("822", "package block hash is null error", false),
    SLAVE_PACKAGE_HEADER_IS_UNEQUAL_ERROR("823", "package hash is unequal error", false),
    SLAVE_PACKAGE_PERSISTING_ERROR("824", "package persisting error", false),
    SLAVE_PACKAGE_TWO_HEADER_UNEQUAL_ERROR("825", "package consensus header unequal tempHeader error", false),
    SLAVE_PACKAGE_UPDATE_PENDING_TX_ERROR("826", "package pending tx update error", false),
    SLAVE_PACKAGE_NO_SUCH_STATUS("827", "package status is invalid", false),
    SLAVE_PACKAGE_IS_NOT_EXIST("828", "package is not exist", false),
    SLAVE_PACKAGE_NOT_SUITABLE_HEIGHT("829", "current package height is not suitable", false),
    SLAVE_AMOUNT_ILLEGAL("830", "amount is illegal", false),
    SLAVE_ACCOUNT_CURRENCY_ALREADY_EXISTS_ERROR("831", "currency is already exists", false),
    SLAVE_CONTRACT_NOT_EXIST_ERROR("832", "contract is not exist", false),
    SLAVE_LAST_PACKAGE_NOT_FINISH("833", "last package is not finished, just waiting", false),
    SLAVE_PACKAGE_REPLICATE_FAILED("834", "package replicated to consensus failed", false),
    SLAVE_PACKAGE_CALLBACK_ERROR("835", "package callback rs has error", false),
    SLAVE_RS_CALLBACK_NOT_REGISTER_ERROR("836", "rs callback not register error", false),
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
    private SlaveErrorEnum(String code, String description, boolean needRetry) {
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
    public static SlaveErrorEnum getByCode(String code) {
        for (SlaveErrorEnum scenario : values()) {
            if (scenario.getCode().equals(code)) {

                return scenario;
            }
        }
        return null;
    }
}