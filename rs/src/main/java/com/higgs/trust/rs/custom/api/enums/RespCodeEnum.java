package com.higgs.trust.rs.custom.api.enums;

/**
 * Created by young001 on 2017/6/17.
 */


public enum RespCodeEnum {
    //maincode意义：    000-》成功； 100-》参数异常；200-》业务异常；500-》系统异常 600->失败大类

//    与沈腾沟通，在成功类，只存放请求时正常接受并且是正常以成功方式内容返回的，比如鉴权成功是放在000成功类中，如果鉴权失败放在
//    200业务异常中，表示这个请求业务业务处理失败，包括外部业务系统异常、内部业务处理逻辑异常、请求没有成功的方式返回，比如鉴权
//    如果鉴权失败，那么也放在业务异常类中，不放在处理成功类型中，这样业务系统调用的时候只要判断返回码即可，这种返回的时候bizResponse
//    为null即可，不用解析bizResponse中的ppAuthenorizeFlag
//    分类参考：https://doc.open.alipay.com/docs/doc.htm?spm=a219a.7395905.0.0.25RpvU&treeId=262&articleId=105806&docType=1

    SUCCESS("000", "000", "操作成功"),
    PROPERTIES_SETTER_SUCCESS("000", "000", "修改内存参数成功"),

    //异步存证
    ASYNC_SEND_IDENTITY_REQUEST("000", "000", "下发存证请求成功"),
    GET_IDENTITY_REQUEST_SUCCESS("000", "000", "获取存证信息成功"),

    //票据处理
    CREATE_BILL_PROCESS("000", "001", "创建票据处理中"),

    SIGNATURE_VERIFY_FAIL("100", "000", "签名验证失败"),
    PARAM_NOT_VALID("100", "001", "请求参数校验失败"),
    REQUEST_REPEAT_CHECK_VALID("100", "007", "重复请求"),
    PARAM_CHECK_EMPTY_VALID("100", "008", "请求参数为null"),
    PUBKEY_OR_ADDRESS_CHECK_VALID("100", "009", "公钥与地址不匹配"),
    PARAM_ADDRESS_NOT_VALID("100", "022", "地址或公钥格式不合法"),
    IDENTITY_NOT_EXIST("100", "023", "存证信息不存在"),



    SYS_LIMITED("500", "003", "系统限流，请稍后重试"),
    SYS_FAIL_RETRY("500", "001", "系统繁忙，请稍后重试"),
    SYS_MAINTAIN("500", "002", "bankchain维护中"),
    SYS_FAIL("500", "000", "系统异常"),
    SYS_DATABASE_FAIL("500", "000", "数据库异常，请重试");

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
  /*  public boolean equals(String code) {
        return this.getCode().equals(code);
    }*/

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
