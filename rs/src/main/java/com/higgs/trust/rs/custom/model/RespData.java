package com.higgs.trust.rs.custom.model;

import com.higgs.trust.rs.custom.api.enums.RespCodeEnum;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.StandardToStringStyle;

/**
 * 返回数据 的 封装对象
 * 
 * @author liuyu
 * 
 * @param <T>
 */
public class RespData<T> implements java.io.Serializable {
	private static final long serialVersionUID = 4917480918640310535L;

	private String respCode = "000000";// 消息代码，由RespCode中的code和subCode组成，分别是3位
	private String msg = "success";// 消息描述
	private T data;// 数据对象
	private String signature;
	private transient boolean success;
	public RespData() {
	}

	public RespData(String respCode) {
		this.respCode = respCode; 
	}

	public RespData(String respCode, String msg) {
		this.respCode = respCode;
		this.msg = msg;
	}

	public RespData(RespCodeEnum respCodeEnum, String msg) {
		this.respCode = respCodeEnum.getRespCode();
		this.msg = msg;
	}


	public RespData(RespCodeEnum respCodeEnum) {
		this.respCode = respCodeEnum.getRespCode();
		this.msg = respCodeEnum.getMsg();
	}

	public static <T> RespData<T> success(T data) {
		RespData<T> respData = new RespData<T>();
		respData.setCode("000");
		respData.setData(data);
		return respData;
	}

	public static <T> RespData<T> error(String respCode, String message, T data) {
		RespData<T> respData = new RespData<T>();
		respData.setCode(respCode);
		respData.setMsg(message);
		respData.setData(data);
		return respData;
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
	public void setCode(RespCodeEnum respCodeEnum) {
		this.respCode = respCodeEnum.getRespCode();
		this.msg = respCodeEnum.getMsg();
	}

	public String getMsg() {
		if(msg == null || msg.length() == 0){
			msg = "unknow error";
		}
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	@Override
	public String toString() {
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

	public boolean isSuccess() {
		success = RespCodeEnum.SUCCESS.getRespCode().equals(respCode);
		return success;
	}
}
