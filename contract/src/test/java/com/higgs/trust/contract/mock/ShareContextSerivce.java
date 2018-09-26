package com.higgs.trust.contract.mock;

import com.higgs.trust.contract.ContractApiService;

import java.math.BigDecimal;
import java.math.BigInteger;

public class ShareContextSerivce extends ContractApiService {

    private ShareBlockSerivce blockSerivce;
    private Object ctxObj;

    public ShareContextSerivce() {
        blockSerivce = new ShareBlockSerivce();
    }

    public BigDecimal add(String x, String y) {
        System.out.println(y);
        return new BigDecimal(x).add(new BigDecimal(y));
    }

    public BigInteger bigInteger = new BigInteger("199999999999999999999999999999999999999");

    public ShareBlockSerivce getBlockSerivce() {
        return blockSerivce;
    }

    public Object getAdmin() {
        return getContext().getData("admin");
    }

    public String sayHello(String name) {
        return "Hello " + name + "  " + Thread.currentThread().getName();
    }

    public Object getCtxObj() {
        return ctxObj;
    }

    public void setCtxObj(Object ctxObj) {
        this.ctxObj = ctxObj;
    }
}
