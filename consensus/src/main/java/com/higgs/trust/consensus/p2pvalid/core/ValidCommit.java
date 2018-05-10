package com.higgs.trust.consensus.p2pvalid.core;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.consensus.p2pvalid.core.storage.ReceiveService;
import com.higgs.trust.consensus.p2pvalid.dao.po.ReceiveCommandPO;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cwy
 */
@Slf4j
public class ValidCommit<T extends ValidCommand<?>> {

    private ReceiveCommandPO receiveCommand;
    private ValidCommand<?> validCommand;

    private ValidCommit(ReceiveCommandPO receiveCommand) {
        this.receiveCommand = receiveCommand;
        this.validCommand = (ValidCommand<?>) JSON.parse(receiveCommand.getValidCommand());
    }

    public static ValidCommit of(ReceiveCommandPO receiveCommandPO) {
        return new ValidCommit(receiveCommandPO);
    }

    public T operation() {
        return (T)validCommand;
    }

    public Class<? extends ValidCommand> type() {
        return validCommand.getClass();
    }

    public void close() {
        receiveCommand.setClosed(ReceiveService.COMMAND_CLOSED);
    }

}
