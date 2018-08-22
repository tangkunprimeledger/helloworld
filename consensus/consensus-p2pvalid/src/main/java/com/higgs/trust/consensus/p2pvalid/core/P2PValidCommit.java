package com.higgs.trust.consensus.p2pvalid.core;

import lombok.ToString;

/**
 * @author liuyu
 */
@ToString
public class P2PValidCommit<T extends ValidCommand<?>> extends ValidBaseCommit<T> {

    public static final int COMMAND_NORMAL = 0;
    public static final int COMMAND_APPLIED = 1;

    private int status;

    public P2PValidCommit(ValidCommand<?> validCommand) {
        super((T)validCommand);
        this.status = COMMAND_NORMAL;
    }

    public void close() {
        this.status = COMMAND_APPLIED;
    }

    public boolean isClosed() {
        return this.status == COMMAND_APPLIED;
    }

}
