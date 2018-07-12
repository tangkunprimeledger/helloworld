package com.higgs.trust.consensus.p2pvalid.exception;

import com.higgs.trust.consensus.p2pvalid.enums.P2pErrorEnum;

public class P2pException extends RuntimeException {
    /**
     * 异常错误代码
     */
    protected P2pErrorEnum code = P2pErrorEnum.P2P_UNKNOWN_EXCEPTION;

    /**
     * 创建一个<code>BizException</code>对象
     *
     * @param e VN业务异常
     */
    public P2pException(P2pException e) {
        super(e);
        this.code = e.getCode();
    }

    /**
     * 创建一个<code>BizException</code>
     *
     * @param code 错误码
     */
    public P2pException(P2pErrorEnum code) {
        super(code.getDescription());
        this.code = code;
    }

    /**
     * 创建一个<code>BizException</code>
     *
     * @param code         错误码
     * @param errorMessage 错误描述
     */
    public P2pException(P2pErrorEnum code, String errorMessage) {
        super(errorMessage);
        this.code = code;
    }

    /**
     * 创建一个<code>BizException</code>
     *
     * @param code  错误码
     * @param cause 异常
     */
    public P2pException(P2pErrorEnum code, Throwable cause) {
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
    public P2pException(P2pErrorEnum code, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.code = code;
    }

    // ~~~ 重写方法

    /**
     * @see Throwable#toString()
     */
    @Override public final String toString() {
        String s = getClass().getName();
        String message = getLocalizedMessage();
        return s + ": " + code.getCode() + "[" + message + "]。";
    }

    /**
     * @see Throwable#getMessage()
     */
    @Override public final String getMessage() {
        return code.getDescription() + "[" + code + "]";
    }

    // ~~~ bean方法

    /**
     * @return Returns the code.
     */
    public P2pErrorEnum getCode() {
        return code;
    }

    /**
     * @param code The code to set.
     */
    public void setCode(P2pErrorEnum code) {
        this.code = code;
    }
}