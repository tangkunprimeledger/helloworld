package com.higgs.trust.consensus.p2pvalid.core;

import com.higgs.trust.consensus.p2pvalid.core.storage.entry.impl.ReceiveCommandStatistics;

/**
 * @author cwy
 */
public class ValidCommit<T extends ValidCommand<?>> {

    private ReceiveCommandStatistics receiveCommandStatistics;

    private ValidCommit(ReceiveCommandStatistics receiveCommandStatistics) {
        this.receiveCommandStatistics = receiveCommandStatistics;
    }

    public static ValidCommit of(ReceiveCommandStatistics receiveCommandStatistics) {
        return new ValidCommit(receiveCommandStatistics);
    }

    public T operation() {
        return (T) receiveCommandStatistics.getValidCommand();
    }

    public Class<? extends ValidCommand> type() {
        return receiveCommandStatistics.getValidCommand().getClass();
    }

    public void close() {
        receiveCommandStatistics.close();
    }

}
