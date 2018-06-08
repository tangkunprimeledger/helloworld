package com.higgs.trust.rs.common.enums;

/**
 * @Description:
 * @author: pengdi
 **/
public enum RsCoreErrorEnum {
    //@formatter:off
    //\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\//
    //                      公共类错误码[000-099,999]                           //
    //\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\//

    RS_CORE_UNKNOWN_EXCEPTION("999", "其它未知异常", true),

    RS_CORE_CONFIGURATION_ERROR("000", "配置错误", true),

    //\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\//
    //                         请求校检[100-299]                                //
    //\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\//
    RS_CORE_PARAM_VALIDATE_ERROR("100", "param validate error", false),
    RS_CORE_IDEMPOTENT("101", "request idempotent", false),
    RS_CORE_TX_VERIFY_SIGNATURE_FAILED("102", "transaction verify signature failed", false),

    RS_CORE_TX_POLICY_NOT_EXISTS_FAILED("201", "the transaction policy is not exists failed", false),
    RS_CORE_TX_UPDATE_STATUS_FAILED("202", "update transaction status failed", false),
    RS_CORE_TX_UPDATE_SIGN_DATAS_FAILED("203", "update transaction signDatas failed", false),
    RS_CORE_TX_BIZ_TYPE_IS_NULL("204", "tx bizType is null", false),
    RS_CORE_TX_CORE_TX_CALLBACK_NOT_SET("205", "tx core tx callback not set", false),
    RS_CORE_TX_SYNC_EXECUTE_FAILED("206", "rs core tx sync execute failed", false),
    RS_CORE_TX_GET_OTHER_SIGN_ERROR("207", "tx core get other sign data error", false),
    RS_CORE_VOTE_SET_RESULT_ERROR("208", "set vote result is error", false),
    RS_CORE_VOTE_RULE_NOT_EXISTS_ERROR("209", "the transaction vote rule is not exists error", false),
    RS_CORE_VOTE_PATTERN_NOT_EXISTS_ERROR("210", "the transaction vote pattern is not exists error", false),
    RS_CORE_VOTE_VOTERS_IS_EMPTY_ERROR("211", "required voters is empty error", false),
    RS_CORE_VOTE_DECISION_FAIL("212", "vote decision is fail", false),
    RS_CORE_VOTE_REQUEST_RECORD_NOT_EXIST("213", "voteRequestRecord is not exist", false),
    RS_CORE_VOTE_ALREADY_HAS_RESULT_ERROR("214", "voteRequestRecord result already has error", false),
    RS_CORE_VOTE_RECEIPTING_HAS_ERROR("215", "vote receipting has error", false),
    RS_CORE_TX_NOT_EXIST_ERROR("216", "core_tx not exist error", false),
    RS_CORE_VOTE_NOT_STATUS_ERROR("217", "core_tx status is error", false),


    RS_CORE_GENERATE_KEY_ERROR("301", "rs core generate key error", false),
    RS_CORE_WRITE_FILE_ERROR("302", "rs core write file error", false),
    RS_CORE_CA_NOT_EXIST_ERROR("303", "rs core ca not exist error", false),
    RS_CORE_INVALID_NODE_NAME_EXIST_ERROR("304", "rs core invalid node name error", false),
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
    RsCoreErrorEnum(String code, String description, boolean needRetry) {
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
    public static RsCoreErrorEnum getByCode(String code) {
        for (RsCoreErrorEnum scenario : values()) {
            if (scenario.getCode().equals(code)) {

                return scenario;
            }
        }
        return null;
    }
}