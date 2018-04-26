package com.higgs.trust.consensus.p2pvalid.core.storage.entry.impl;

import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import lombok.ToString;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author cwy
 */
@ToString
public class ReceiveCommandStatistics implements Serializable{

    private static final long serialVersionUID = -1L;
    private boolean closed;
    private boolean applied;
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

    public boolean isClosed() {
        return closed;
    }

    public void close() {
        closed = true;
    }

    public boolean isApply() {
        return applied;
    }

    public void apply() {
        applied = true;
    }
}
