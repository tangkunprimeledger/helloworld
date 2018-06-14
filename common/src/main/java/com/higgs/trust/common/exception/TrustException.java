/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.common.exception;

/**
 * @author suimi
 * @date 2018/6/12
 */
public class TrustException extends RuntimeException {

    private static final long serialVersionUID = -5816044871155545343L;

    protected ErrorInfo code;

    /**
     * 创建一个<code>BizException</code>对象
     *
     * @param e VN业务异常
     */
    public TrustException(TrustException e) {
        super(e);
        this.code = e.getCode();
    }

    /**
     * 创建一个<code>BizException</code>
     *
     * @param code 错误码
     */
    public TrustException(ErrorInfo code) {
        super(code.getDescription());
        this.code = code;
    }

    /**
     * 创建一个<code>BizException</code>
     *
     * @param code         错误码
     * @param errorMessage 错误描述
     */
    public TrustException(ErrorInfo code, String errorMessage) {
        super(errorMessage);
        this.code = code;
    }

    /**
     * 创建一个<code>BizException</code>
     *
     * @param code  错误码
     * @param cause 异常
     */
    public TrustException(ErrorInfo code, Throwable cause) {
        super(code.getDescription(), cause);
        this.code = code;
    }

    /**
     * 创建一个<code>VitualNetException</code>
     *
     * @param code         错误码
     * @param errorMessage 错误描述
     * @param cause        异常
     */
    public TrustException(ErrorInfo code, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.code = code;
    }

    // ~~~ 重写方法

    /**
     * @see java.lang.Throwable#toString()
     */
    @Override public final String toString() {
        String s = getClass().getName();
        String message = getLocalizedMessage();
        return s + ": " + code.getCode() + "[" + message + "]。";
    }

    /**
     * @see java.lang.Throwable#getMessage()
     */
    @Override public final String getMessage() {
        return code.getDescription() + "[" + code + "]";
    }

    // ~~~ bean方法

    /**
     * @return Returns the code.
     */
    public ErrorInfo getCode() {
        return code;
    }

    /**
     * @param code The code to set.
     */
    public void setCode(ErrorInfo code) {
        this.code = code;
    }

}
