package com.higgs.trust.rs.core.api;

import com.higgs.trust.rs.common.TxCallbackHandler;
import org.springframework.stereotype.Repository;

/**
 * @author liuyu
 * @description
 * @date 2018-05-12
 */
@Repository
public class TxCallbackRegistor {
    private TxCallbackHandler coreTxCallback;

    public void registCallback(TxCallbackHandler callback){
        this.coreTxCallback = callback;
    }

    public TxCallbackHandler getCoreTxCallback() {
        return coreTxCallback;
    }
}
