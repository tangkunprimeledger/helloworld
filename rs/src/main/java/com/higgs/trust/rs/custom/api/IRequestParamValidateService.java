package com.higgs.trust.rs.custom.api;

/**
 * Created by young001 on 2017/7/5.
 */
public interface IRequestParamValidateService {
    public <T> T checkParamAndGet(String requestParamStr, Class<T> clazz);
}
