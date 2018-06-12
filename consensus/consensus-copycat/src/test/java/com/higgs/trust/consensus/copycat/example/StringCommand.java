package com.higgs.trust.consensus.copycat.example;

import com.higgs.trust.consensus.core.command.AbstractConsensusCommand;

/**
 * The actual command you will use to consensus
 */
public class StringCommand extends AbstractConsensusCommand<String> {
    private static final long serialVersionUID = 1L;

    public StringCommand(String value) {
        super(value);
    }
}