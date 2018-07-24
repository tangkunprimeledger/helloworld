package com.higgs.trust.contract.mock;

import com.higgs.trust.contract.ContractApiService;

import java.math.BigInteger;

public class ShareContextSerivce extends ContractApiService {

    private ShareBlockSerivce blockSerivce;

    public ShareContextSerivce() {
        blockSerivce = new ShareBlockSerivce();
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
}
