package com.higgs.trust.consensus.p2pvalid.core.exchange;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
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
@Getter
@Setter
@ToString
public class ValidCommandWrap implements Serializable {

    private static final long serialVersionUID = -1L;
    private ValidCommand<?> validCommand;
    private String fromNodeName;
    private String messageDigest;
    private String sign;
    private Class<? extends ValidCommand> commandClass;
    private Set<String> toNodeNames = new HashSet<>();

    public ValidCommandWrap() {
    }

    private ValidCommandWrap(ValidCommand<?> validCommand) {
        this.validCommand = validCommand;
        this.commandClass = validCommand.getClass();
        HashFunction function = Hashing.sha256();
        String hash = function.hashString(validCommand.messageDigest(), Charsets.UTF_8).toString();
        this.messageDigest = hash;
    }

    public static ValidCommandWrap of(ValidCommand<?> validCommand) {
        ValidCommandWrap validCommandWrap = new ValidCommandWrap(validCommand);
        return validCommandWrap;
    }

    public ValidCommandWrap fromNodeName(String fromNodeName) {
        this.fromNodeName = fromNodeName;
        return this;
    }

    public ValidCommandWrap sign(String sign) {
        this.sign = sign;
        return this;
    }

    public ValidCommandWrap addToNodeNames(Collection<String> toNodeNames) {
        this.toNodeNames.addAll(toNodeNames);
        return this;
    }

    public ValidCommandWrap addToNodeName(String toNodeName) {
        this.toNodeNames.add(toNodeName);
        return this;
    }


}
