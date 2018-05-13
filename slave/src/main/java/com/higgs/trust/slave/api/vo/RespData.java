package com.higgs.trust.slave.api.vo;

import com.higgs.trust.slave.api.enums.RespCodeEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.StandardToStringStyle;

/**
 * 返回数据 的 封装对象
 *
 * @param <T>
 * @author liuyu
 */
public class RespData<T> implements java.io.Serializable {
    private static final long serialVersionUID = 4917480918640310535L;

    // 消息代码，由RespCode中的code和subCode组成，分别是3位
    private String respCode = "000000";
    // 消息描述
    private String msg = "success";
    // 数据对象
    private T data;

    public RespData() {
    }

    public RespData(String respCode) {
        this.respCode = respCode;
    }

    public RespData(String respCode, String msg) {
        this.respCode = respCode;
        this.msg = msg;
    }

    public RespData(RespCodeEnum respCodeEnum) {
        this.respCode = respCodeEnum.getRespCode();
        this.msg = respCodeEnum.getMsg();
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getRespCode() {
        return respCode;
    }

    public void setCode(String respCode) {
        this.respCode = respCode;
    }

    public String getMsg() {
        if (msg == null || msg.length() == 0) {
            msg = "unknow error";
        }
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isSuccess(){
        return StringUtils.equals(respCode,"000000");
    }

    @Override public String toString() {
        try {
            // ref:https://stackoverflow.com/a/8200915，同时结合了MULTI_LINE_STYLE 和SHORT_PREFIX_STYLE
            StandardToStringStyle style = new StandardToStringStyle();
            style.setFieldSeparator(" ");
            style.setFieldSeparatorAtStart(true);
            style.setUseShortClassName(true);
            style.setUseIdentityHashCode(false);
            return new ReflectionToStringBuilder(this, style).toString();
        } catch (Exception e) {
            return super.toString();
        }
    }
}
