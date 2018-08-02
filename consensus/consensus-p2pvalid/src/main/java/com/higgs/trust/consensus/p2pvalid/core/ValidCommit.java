package com.higgs.trust.consensus.p2pvalid.core;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.consensus.p2pvalid.core.storage.ReceiveService;
import com.higgs.trust.consensus.p2pvalid.dao.po.ReceiveCommandPO;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cwy
 */
@Slf4j public class ValidCommit<T extends ValidCommand<?>> extends ValidBaseCommit<T> {

    private ReceiveCommandPO receiveCommand;

    private ValidCommit(ReceiveCommandPO receiveCommand) {
        super((T)JSON.parseObject(receiveCommand.getValidCommand(), ValidCommand.class));
        this.receiveCommand = receiveCommand;
    }

    public static ValidCommit of(ReceiveCommandPO receiveCommandPO) {
        return new ValidCommit(receiveCommandPO);
    }

    public void close() {
        receiveCommand.setStatus(ReceiveService.COMMAND_APPLIED);
    }

}
