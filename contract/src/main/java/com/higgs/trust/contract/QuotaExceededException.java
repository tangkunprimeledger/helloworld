package com.higgs.trust.contract;

/**
 * @author duhongming
 * @date 2018/6/7
 */
public class QuotaExceededException extends RuntimeException {
    public QuotaExceededException(String message) {
        super(message);
    }
}