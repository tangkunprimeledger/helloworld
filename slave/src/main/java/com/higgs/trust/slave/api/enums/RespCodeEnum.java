package com.higgs.trust.slave.api.enums;

/**
 * Created by young001 on 2017/6/17.
 */
public enum RespCodeEnum {
    // 分类参考：https://doc.open.alipay.com/docs/doc.htm?spm=a219a.7395905.0.0.25RpvU&treeId=262&articleId=105806&docType=1
    // 成功统一返回000000这个码
    SUCCESS("000", "000", "success"),

    //    参数异常类，参数异常类不需要细分，失败的原因写在msg中即可，统一返回参数不合法
    PARAM_NOT_VALID("100", "002", "param valid failed"),

    //    业务异常类
    PARAM_NOT_ILLEGAL("200", "000", "param is illegal"),

    // 系统异常
    SYS_FAIL("500", "000", "system busy"),

    SYS_HANDLE_TIMEOUT("500", "001", "system handle timeout");

    RespCodeEnum(String mainCode, String subCode, String msg) {
        this.mainCode = mainCode;
        this.subCode = subCode;
        this.msg = msg;
    }

    public String getRespCode() {
        return this.mainCode + this.subCode;
    }

    public String getMsg() {
        return msg;
    }

    /**
     * 根据name 获取 RespCodeEnum
     *
     * @param respCode
     * @return
     */
    public static RespCodeEnum getRespCodeEnumByRespCode(String respCode) {
        for (RespCodeEnum respCodeEnum : RespCodeEnum.values()) {
            if (respCodeEnum.getRespCode().equals(respCode)) {
                return respCodeEnum;
            }
        }
        return null;
    }

    /**
     * 主码
     */
    private String mainCode;
    /**
     * 子码
     */
    private String subCode;
    /**
     * 返回信息
     */
    private String msg;

    public String getMainCode() {
        return mainCode;
    }

    public String getSubCode() {
        return subCode;
    }

}
