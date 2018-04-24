package com.higgs.trust.consensus.p2pvalid.core.exception;

/**
 * @author cwy
 */
public class ReceiveException  extends RuntimeException{
    static final long serialVersionUID = -1L;
    public ReceiveException(String message){
        super(message);
    }
}
