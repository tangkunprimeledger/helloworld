package com.higgs.trust.consensus.p2pvalid.core;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.consensus.p2pvalid.dao.po.ReceiveCommandPO;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cwy
 */
@Slf4j
public class ValidCommit<T extends ValidCommand<?>> {

    private ReceiveCommandPO receiveCommandPO;
    private ValidCommand<?> validCommand;

    private ValidCommit(ReceiveCommandPO receiveCommandPO) {
        this.receiveCommandPO = receiveCommandPO;
        this.validCommand = (ValidCommand<?>) JSON.parse(receiveCommandPO.getValidCommand());
    }

    public static ValidCommit of(ReceiveCommandPO receiveCommandPO) {
        return new ValidCommit(receiveCommandPO);
    }

    public T operation() {
        return (T)validCommand.getT() ;
    }

    public Class<? extends ValidCommand> type() {
        return validCommand.getClass();
    }

    public void close() {
        log.info("add to gc {}", receiveCommandPO);
    }

}
