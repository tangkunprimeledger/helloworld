package com.higgs.trust.contract;

/**
 * @author duhongming
 * @date 2018/6/11
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
