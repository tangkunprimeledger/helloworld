package com.higgs.trust.consensus.p2pvalid.core.exchange;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgs.trust.config.crypto.CryptoUtil;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author cwy
 */
@Getter @Setter @ToString public class SendValidCommandWrap implements Serializable {

    private static final long serialVersionUID = -1L;
    private ValidCommand<?> validCommand;
    private String fromNodeName;
    private String messageDigest;
    private String sign;
    private Class<? extends ValidCommand> commandClass;
    private Set<String> toNodeNames = new HashSet<>();
    private Long traceId;

    public SendValidCommandWrap() {
    }

    private SendValidCommandWrap(ValidCommand<?> validCommand) {
        this.validCommand = validCommand;
        this.commandClass = validCommand.getClass();
        HashFunction function = Hashing.sha256();
        String hash = function.hashString(validCommand.messageDigest(), Charsets.UTF_8).toString();
        this.messageDigest = hash;
    }

    public static SendValidCommandWrap of(ValidCommand<?> validCommand) {
        SendValidCommandWrap validCommandWrap = new SendValidCommandWrap(validCommand);
        return validCommandWrap;
    }

    public SendValidCommandWrap fromNodeName(String fromNodeName) {
        this.fromNodeName = fromNodeName;
        return this;
    }

    public SendValidCommandWrap sign(String privateKey) throws Exception {
        this.sign = CryptoUtil.getProtocolCrypto().sign(messageDigest, privateKey);
        return this;
    }

    public SendValidCommandWrap addToNodeNames(Collection<String> toNodeNames) {
        this.toNodeNames.addAll(toNodeNames);
        return this;
    }

    public SendValidCommandWrap addToNodeName(String toNodeName) {
        this.toNodeNames.add(toNodeName);
        return this;
    }

    public Long getTraceId() {
        return traceId;
    }

    public void setTraceId(Long traceId) {
        this.traceId = traceId;
    }
}
