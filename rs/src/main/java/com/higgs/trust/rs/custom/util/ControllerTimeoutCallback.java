package com.higgs.trust.rs.custom.util;

/**
 * Created by young001 on 2017/9/25.
 */
public interface ControllerTimeoutCallback {
    /**
     * 当controller超时之后执行callback
     */
    public void callback();
}
