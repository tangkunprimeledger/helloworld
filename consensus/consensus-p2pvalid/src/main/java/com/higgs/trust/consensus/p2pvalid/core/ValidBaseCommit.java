package com.higgs.trust.consensus.p2pvalid.core;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.consensus.p2pvalid.core.storage.ReceiveService;
import com.higgs.trust.consensus.p2pvalid.dao.po.ReceiveCommandPO;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cwy
 */
@Slf4j public class ValidBaseCommit<T extends ValidCommand<?>> {

    private T validCommand;

    protected ValidBaseCommit(T validCommand) {
        this.validCommand = validCommand;
    }

    public T operation() {
        return validCommand;
    }

    public Class<? extends ValidCommand> type() {
        return validCommand.getClass();
    }

    public void close() {
    }

}
