package com.higgs.trust.contract.mock;

import com.higgs.trust.contract.ContractApiService;

public class ShareBlockSerivce extends ContractApiService {
    public int getCurrentBlockHeight() {
        return 1;
    }
}
