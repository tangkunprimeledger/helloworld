package com.higgs.trust.slave.common.exception;

import com.higgs.trust.slave.common.enums.SlaveErrorEnum;

public class SlaveException extends RuntimeException {
    /**
     * 异常错误代码
     */
    protected SlaveErrorEnum code = SlaveErrorEnum.SLAVE_UNKNOWN_EXCEPTION;

    /**
     * 创建一个<code>BizException</code>对象
     *
     * @param e VN业务异常
     */
    public SlaveException(SlaveException e) {
        super(e);
        this.code = e.getCode();
    }

    /**
     * 创建一个<code>BizException</code>
     *
     * @param code 错误码
     */
    public SlaveException(SlaveErrorEnum code) {
        super(code.getDescription());
        this.code = code;
    }

    /**
     * 创建一个<code>BizException</code>
     *
     * @param code         错误码
     * @param errorMessage 错误描述
     */
    public SlaveException(SlaveErrorEnum code, String errorMessage) {
        super(errorMessage);
        this.code = code;
    }

    /**
     * 创建一个<code>BizException</code>
     *
     * @param code  错误码
     * @param cause 异常
     */
    public SlaveException(SlaveErrorEnum code, Throwable cause) {
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
    public SlaveException(SlaveErrorEnum code, String errorMessage, Throwable cause) {
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
    public SlaveErrorEnum getCode() {
        return code;
    }

    /**
     * @param code The code to set.
     */
    public void setCode(SlaveErrorEnum code) {
        this.code = code;
    }
}