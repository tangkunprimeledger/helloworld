package com.higgs.trust.rs.custom.api.enums;

public enum BankChainExceptionCodeEnum {
    //TODO 修改 异常
    DatabaseAccessException("数据库访问异常", "bankchain_db_exception_count"),
    DataNotUpdatedException("数据未更新异常", "bankchain_data_not_updated_exception_count"),
    DatabaseCannotAcquireLockException("数据库无法获取锁异常", "bankchain_data_can_not_acquire_lock"),
    RedisCannotAcquireLockException("redis无法获取锁异常", "bankchain_redis_can_not_acquire_lock"),
    BitcoinjEckeyException("bitcoinj公钥得到地址，参数异常", "bankchain_bitcoinj_exception_eckey"),
    SignatureError("bitcoinj签名异常，签名与消息不匹配", "bankchain_bitcoinj_exception_eckey"),
    DAOParamInvalidException("DAO参数异常", "bankchain_dao_param_invalid_count"),
    BCPolicyNotDefException("policy未定义异常", "bankchain_policy_not_def_count"),
    BCRequestParamInvalidException("请求BC参数异常", "bankchain_request_param_invalid_count"),
    BCServiceVersionNotSupportException("服务版本不支持异常", "bankchain_request_param_invalid_count"),
    BCSystemException("系统服务异常", "bankchain_sys_exception_count"),
    ServiceRespNullException("业务返回null异常", "bankchain_sys_exception_count"),
    POCheckInvalidException("业务PO检查异常", "bankchain_db_integrate_exception_count"),
    VnParamInvalidException("VN调用PP参数异常", "bankchain_cb_param_invalid_count"),
    VnProcessCriticalException("请求VN返回无法处理的严重异常", "bankchain_vn_process_critical_exception_count"),
    VnResponseException("请求VN返回数据异常", "bankchain_vn_response_exception_count"),
    PrimeSlaveVoteFailException("slave投票失败", "bankchain_slave_fail_count"),
    UndefinedPrimeSlaveVoteResultException("slav未知投票结果", "bankchain_undefined_slave_vote_result_count"),
    BCRequestLimitedException("系统限流中", "bankchain_request_limited_exception_count"),

    OpenAccountCallbackProcessException("开户回调处理失败", "open_account_callback_process_exception"),
    StorageIdentityMngProcessException("存证failover处理失败", "storage_identity_mng_process_exception"),
    BCServiceBizParamInvalidException("BC业务参数异常", "bankchain_biz_request_param_invalid_count"),
    BCAccountNotExistException("账户不存在异常", "coninchain_account_not_exist_exception_count"),
    BCAccountExistedException("账户不存在异常", "coninchain_account_existed_exception_count"),
    ISSUE_COIN_VN_CALLBACK_ERROR("发币VN回调处理异常", "coninchain_issue_coin_vn_callback_error_count"),
    IdentityCallbackProcessException("存证回调处理失败", "identity_callback_process_exception"),
    ;
    BankChainExceptionCodeEnum(String description, String monitorTarget) {
        this.description = description;
        this.monitorTarget = monitorTarget;
    }

    BankChainExceptionCodeEnum(String description) {
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
