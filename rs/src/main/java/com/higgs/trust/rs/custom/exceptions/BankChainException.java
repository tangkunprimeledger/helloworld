package com.higgs.trust.rs.custom.exceptions;

import com.higgs.trust.rs.custom.api.enums.BankChainExceptionCodeEnum;

public class BankChainException extends RuntimeException {
    /**
     * 异常错误代码
     */
    protected BankChainExceptionCodeEnum code = BankChainExceptionCodeEnum.BCSystemException;

    public BankChainException() {
        super(BankChainExceptionCodeEnum.BCSystemException.getDescription());
        this.code = BankChainExceptionCodeEnum.BCSystemException;
    }
    /**
     * 创建一个<code>VitualNetException</code>
     *
     * @param code 错误码
     */
    public BankChainException(BankChainExceptionCodeEnum code) {
        super(code.getDescription());
        this.code = code;
    }

    /**
     * 创建一个<code>VitualNetException</code>
     *
     * @param code         错误码
     * @param errorMessage 错误描述
     */
    public BankChainException(BankChainExceptionCodeEnum code, String errorMessage) {
        super(errorMessage);
        this.code = code;
    }

    /**
     * 创建一个<code>VitualNetException</code>
     *
     * @param code  错误码
     * @param cause 异常
     */
    public BankChainException(BankChainExceptionCodeEnum code, Throwable cause) {
        super(code.getDescription(), cause);
        this.code = code;
    }

    /**
     * @param cause 异常
     */
    public BankChainException(Throwable cause) {
        super(cause);
    }


    /**
     * 创建一个<code>VitualNetException</code>
     *
     * @param code         错误码
     * @param errorMessage 错误描述
     * @param cause        异常
     */
    public BankChainException(BankChainExceptionCodeEnum code, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.code = code;
    }

    // ~~~ 重写方法

    /**
     * @see Throwable#toString()
     */
    @Override
    public final String toString() {
        String s = getClass().getName();
        String message = getLocalizedMessage();
        return s + ": " + code.getDescription() + "[" + message + "]。";
    }

    // ~~~ bean方法

    /**
     * @return Returns the code.
     */
    public BankChainExceptionCodeEnum getCode() {
        return code;
    }

    /**
     * @param code The code to set.
     */
    public void setCode(BankChainExceptionCodeEnum code) {
        this.code = code;
    }
}
