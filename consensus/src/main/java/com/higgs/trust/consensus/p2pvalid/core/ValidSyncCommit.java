package com.higgs.trust.consensus.p2pvalid.core;

import lombok.extern.slf4j.Slf4j;

/**
 * @author cwy
 */
@Slf4j public class ValidSyncCommit<T extends ValidCommand<?>> extends ValidBaseCommit<T> {

    public ValidSyncCommit(T validCommand) {
        super(validCommand);
    }
}
