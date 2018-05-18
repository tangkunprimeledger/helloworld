package com.higgs.trust.rs.custom.exceptions;

import com.higgs.trust.rs.custom.api.enums.CustomExceptionCodeEnum;
import com.higgs.trust.rs.custom.api.enums.CustomExceptionCodeEnum;

public class CustomException extends RuntimeException {
    /**
     * 异常错误代码
     */
    protected CustomExceptionCodeEnum code = CustomExceptionCodeEnum.BCSystemException;

    public CustomException() {
        super(CustomExceptionCodeEnum.BCSystemException.getDescription());
        this.code = CustomExceptionCodeEnum.BCSystemException;
    }
    /**
     * 创建一个<code>VitualNetException</code>
     *
     * @param code 错误码
     */
    public CustomException(CustomExceptionCodeEnum code) {
        super(code.getDescription());
        this.code = code;
    }

    /**
     * 创建一个<code>VitualNetException</code>
     *
     * @param code         错误码
     * @param errorMessage 错误描述
     */
    public CustomException(CustomExceptionCodeEnum code, String errorMessage) {
        super(errorMessage);
        this.code = code;
    }

    /**
     * 创建一个<code>VitualNetException</code>
     *
     * @param code  错误码
     * @param cause 异常
     */
    public CustomException(CustomExceptionCodeEnum code, Throwable cause) {
        super(code.getDescription(), cause);
        this.code = code;
    }

    /**
     * @param cause 异常
     */
    public CustomException(Throwable cause) {
        super(cause);
    }


    /**
     * 创建一个<code>VitualNetException</code>
     *
     * @param code         错误码
     * @param errorMessage 错误描述
     * @param cause        异常
     */
    public CustomException(CustomExceptionCodeEnum code, String errorMessage, Throwable cause) {
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
    public CustomExceptionCodeEnum getCode() {
        return code;
    }

    /**
     * @param code The code to set.
     */
    public void setCode(CustomExceptionCodeEnum code) {
        this.code = code;
    }
}
