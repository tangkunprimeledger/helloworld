package com.higgs.trust.consensus.p2pvalid.core.storage.entry.impl;

import com.higgs.trust.consensus.p2pvalid.core.exchange.ValidCommandWrap;
import com.higgs.trust.consensus.p2pvalid.core.storage.entry.Closeable;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

/**
 * @author cwy
 */
@ToString
public class SendCommandStatistics extends Closeable {
    private static final long serialVersionUID = -1L;

    private ValidCommandWrap validCommandWrap;
    private Set<String> ackNodeNameSet;

    private SendCommandStatistics(ValidCommandWrap validCommandWrap) {
        this.validCommandWrap = validCommandWrap;
        this.ackNodeNameSet = new HashSet<>();
    }

    public static SendCommandStatistics of(ValidCommandWrap validCommandWrap) {
        return new SendCommandStatistics(validCommandWrap);
    }

    public void addAckNodeName(String nodeName) {
        ackNodeNameSet.add(nodeName);
    }

    public ValidCommandWrap getValidCommandWrap() {
        return validCommandWrap;
    }

    public Set<String> getAckNodeName() {
        return ackNodeNameSet;
    }
}
