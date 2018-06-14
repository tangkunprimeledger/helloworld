package com.higgs.trust.rs.custom.api.enums;

public enum CustomExceptionCodeEnum {
    //TODO 修改 异常
    DAOParamInvalidException("DAO参数异常", "bankchain_dao_param_invalid_count"),
    BCPolicyNotDefException("policy未定义异常", "bankchain_policy_not_def_count"),
    BCServiceVersionNotSupportException("服务版本不支持异常", "bankchain_request_param_invalid_count"),
    BCSystemException("系统服务异常", "bankchain_sys_exception_count"),
    ServiceRespNullException("业务返回null异常", "bankchain_sys_exception_count"),
    POCheckInvalidException("业务PO检查异常", "bankchain_db_integrate_exception_count"),

    IdentityCallbackProcessException("存证回调处理失败", "identity_callback_process_exception"),

    RegisterRsCallbackProcessException("注册RS回调处理异常", "register_RS_callback_process_exception"),
    RegisterPolicyCallbackProcessException("注册policy回调处理异常", "register_policy_callback_process_exception"),
    CancelRsCallBackProcessException("注销RS回调处理异常", "cancel_rs_callback_process_exception"),
    ;
    CustomExceptionCodeEnum(String description, String monitorTarget) {
        this.description = description;
        this.monitorTarget = monitorTarget;
    }

    CustomExceptionCodeEnum(String description) {
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
