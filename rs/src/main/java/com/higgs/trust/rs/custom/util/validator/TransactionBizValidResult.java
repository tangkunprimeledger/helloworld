package com.higgs.trust.rs.custom.util.validator;

import com.higgs.trust.rs.custom.model.RespData;

/**
 * Transaction 检查结果， 构造方法默认校验失败
 *
 * @author lingchao
 * @create 2018年02月19日15:15
 */
public class TransactionBizValidResult {

    /**
     * 检查结果
     */
    private boolean isSuccess;
    /**
     * 结果信息
     */
    private  String msg;

    /**
     * 返回结果
     */
    private RespData respData;


    public TransactionBizValidResult(boolean isSuccess, String msg, RespData respData){
        this.isSuccess = isSuccess;
        this.msg = msg;
        this.respData = respData;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public RespData getRespData() {
        return respData;
    }

    public void setRespData(RespData respData) {
        this.respData = respData;
    }
}
