package com.higgs.trust.rs.custom.exceptions;

import com.higgs.trust.rs.custom.api.enums.RespCodeEnum;

public class BillException extends RuntimeException {
    /**
     * 异常错误代码
     */

    protected RespCodeEnum code = RespCodeEnum.SYS_FAIL;

    /**
     * 创建一个<code>BizException</code>对象
     *
     * @param e VN业务异常
     */
    public BillException(BillException e) {
        super(e);
        this.code = e.getCode();
    }

    /**
     * 创建一个<code>BizException</code>
     *
     * @param code 错误码
     */
    public BillException(RespCodeEnum code) {
        super(code.getMsg());
        this.code = code;
    }

    /**
     * 创建一个<code>BizException</code>
     *
     * @param code         错误码
     * @param errorMessage 错误描述
     */
    public BillException(RespCodeEnum code, String errorMessage) {
        super(errorMessage);
        this.code = code;
    }

    /**
     * 创建一个<code>BizException</code>
     *
     * @param code  错误码
     * @param cause 异常
     */
    public BillException(RespCodeEnum code, Throwable cause) {
        super(code.getMsg(), cause);
        this.code = code;
    }

    /**
     * 创建一个<code>VitualNetException</code>
     *
     * @param code         错误码
     * @param errorMessage 错误描述
     * @param cause        异常
     */
    public BillException(RespCodeEnum code, String errorMessage, Throwable cause) {
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
        return s + ": " + code.getRespCode() + "[" + message + "]。";
    }

    /**
     * @see java.lang.Throwable#getMessage()
     */
    @Override public final String getMessage() {
        return code.getMsg() + "[" + code + "]";
    }

    // ~~~ bean方法

    /**
     * @return Returns the code.
     */
    public RespCodeEnum getCode() {
        return code;
    }

    /**
     * @param code The code to set.
     */
    public void setCode(RespCodeEnum code) {
        this.code = code;
    }
}