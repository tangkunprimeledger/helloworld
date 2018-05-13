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
    RS_CORE_TX_CORE_TX_CALLBACK_NOT_SET("204", "tx core tx callback not set", false),
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