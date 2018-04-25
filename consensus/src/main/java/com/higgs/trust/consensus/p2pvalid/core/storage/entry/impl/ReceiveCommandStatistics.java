package com.higgs.trust.consensus.p2pvalid.core.storage.entry.impl;

import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import com.higgs.trust.consensus.p2pvalid.core.storage.entry.Closeable;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

/**
 * @author cwy
 */
@ToString
public class ReceiveCommandStatistics extends Closeable {
    private static final long serialVersionUID = -1L;

    private ValidCommand<?> validCommand;
    private Set<String> fromNodeNameSet;

    private ReceiveCommandStatistics(ValidCommand<?> validCommand) {
        this.validCommand = validCommand;
        this.fromNodeNameSet = new HashSet<>();
    }

    public static ReceiveCommandStatistics create(ValidCommand<?> validCommand) {
        return new ReceiveCommandStatistics(validCommand);
    }

    public void addFromNode(String nodeName) {
        this.fromNodeNameSet.add(nodeName);
    }

    public ValidCommand<?> getValidCommand() {
        return validCommand;
    }

    public Set<String> getFromNodeNameSet() {
        return fromNodeNameSet;
    }
}
