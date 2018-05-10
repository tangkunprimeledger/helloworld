package com.higgs.trust.consensus.p2pvalid.core;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@NoArgsConstructor @Data public class ValidResponseWrap<T extends ResponseCommand> implements Serializable {

    private static final long serialVersionUID = 5358908034044478066L;
    public static final String SUCCESS_CODE = "SUCCESS";
    public static final String FAILED_CODE = "FAILED";
    private String code;
    private String message;
    protected Object result;
    private String sign;

    public static <T extends ResponseCommand> ValidResponseWrap<T> successResponse(Object result) {
        ValidResponseWrap<T> tValidResponseWrap = new ValidResponseWrap<>();
        tValidResponseWrap.setCode(SUCCESS_CODE);
        tValidResponseWrap.setResult(result);
        return tValidResponseWrap;
    }

    public static ValidResponseWrap failedResponse(String code, String message) {
        ValidResponseWrap<ResponseCommand> response = new ValidResponseWrap<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }

    public boolean isSucess() {
        return SUCCESS_CODE.equals(code);
    }
}
