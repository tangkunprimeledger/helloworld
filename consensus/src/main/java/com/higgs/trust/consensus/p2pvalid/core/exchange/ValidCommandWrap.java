package com.higgs.trust.consensus.p2pvalid.core.exchange;

import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author cwy
 */
@Getter
@ToString
public class ValidCommandWrap implements Serializable {

    private static final long serialVersionUID = -1L;
    private ValidCommand<?> validCommand;
    private String fromNodeName;
    private String messageDigest;
    private String sign;
    private Class<? extends ValidCommand> commandClass;
    private Set<String> toNodeNames;

    public ValidCommandWrap(){}

    private ValidCommandWrap(ValidCommand<?> validCommand) {
        toNodeNames = new HashSet<>();
        this.validCommand = validCommand;
        this.commandClass = validCommand.getClass();
    }

    public static ValidCommandWrap of(ValidCommand<?> validCommand) {
        ValidCommandWrap validCommandWrap = new ValidCommandWrap(validCommand);
        return validCommandWrap;
    }

    public ValidCommandWrap fromNodeName(String fromNodeName) {
        this.fromNodeName = fromNodeName;
        return this;
    }

    public ValidCommandWrap messageDigest(String messageDigest) {
        this.messageDigest = messageDigest;
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
