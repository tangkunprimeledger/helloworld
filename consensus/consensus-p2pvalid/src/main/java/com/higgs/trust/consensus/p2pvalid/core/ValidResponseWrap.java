package com.higgs.trust.consensus.p2pvalid.core;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@Data
@Slf4j
public class ValidResponseWrap<T extends ResponseCommand> implements Serializable {

    private static final long serialVersionUID = 5358908034044478066L;
    public static final String SUCCESS_CODE = "SUCCESS";
    public static final String FAILED_CODE = "FAILED";
    private String code;
    private String message;
    private T resultOne;
    private List<T> resultList;

    private String sign;

    public static <T extends ResponseCommand> ValidResponseWrap<T> successResponse(Object result) {
        ValidResponseWrap<T> tValidResponseWrap = new ValidResponseWrap<>();
        tValidResponseWrap.setCode(SUCCESS_CODE);
        if (result == null) {
            return tValidResponseWrap;
        }
        if (result instanceof ResponseCommand) {
            tValidResponseWrap.setResultOne((T) result);
            return tValidResponseWrap;
        }
        if (result instanceof List) {
            tValidResponseWrap.setResultList((List) result);
            return tValidResponseWrap;
        }
        
        log.error("result({}) is not ResponseCommand type", result.getClass().getName());
        return tValidResponseWrap;
    }

    public static ValidResponseWrap failedResponse() {
        return failedResponse("");
    }

    public static ValidResponseWrap failedResponse(String message) {
        return failedResponse(FAILED_CODE, message);
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

    public Object result() {
        return resultOne == null ? resultList : resultOne;
    }
}
