package com.higgs.trust.consensus.p2pvalid.core.storage.entry.impl;

import com.higgs.trust.consensus.p2pvalid.core.exchange.ValidCommandWrap;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author cwy
 */
@ToString
public class SendCommandStatistics implements Serializable{
    private static final long serialVersionUID = -1L;

    private ValidCommandWrap validCommandWrap;
    private Set<String> ackNodeNameSet;
    private Set<String> sendNodeNameSet;
    private boolean send;

    private SendCommandStatistics(ValidCommandWrap validCommandWrap) {
        this.validCommandWrap = validCommandWrap;
        this.sendNodeNameSet = validCommandWrap.getToNodeNames();
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

    public Set<String> getAckNodeNames() {
        return ackNodeNameSet;
    }

    public Set<String> getSendNodeNames() {
        return sendNodeNameSet;
    }

    public boolean isSend() {
        return send;
    }

    public void setSend() {
        send = true;
    }
}
