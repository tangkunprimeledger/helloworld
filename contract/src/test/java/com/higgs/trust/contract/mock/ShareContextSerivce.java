package com.higgs.trust.contract.mock;

import com.higgs.trust.contract.ContractApiService;
import com.higgs.trust.contract.SmartContractException;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

public class ShareContextSerivce extends ContractApiService {

    private ShareBlockSerivce blockSerivce;

    public ShareContextSerivce() {
        blockSerivce = new ShareBlockSerivce();
    }

    public ShareBlockSerivce getBlockSerivce() {
        return blockSerivce;
    }

    public Object getAdmin() {
        return getContext().getData("admin");
    }

    public String sayHello(String name) {
        return "Hello " + name + "  " + Thread.currentThread().getName() + " address:" + getContext().getContract()
            .getAddress();
    }
}
