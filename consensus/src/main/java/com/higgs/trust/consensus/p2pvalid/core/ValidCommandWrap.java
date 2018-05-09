package com.higgs.trust.consensus.p2pvalid.core;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author cwy
 */
@Getter
@Setter
@ToString
public class ValidCommandWrap implements Serializable{
    private static final long serialVersionUID = -1L;
    private ValidCommand<?> validCommand;
    private String fromNode;
    private String sign;
    private String commandClass;
}
