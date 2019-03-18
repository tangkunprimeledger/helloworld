package com.higgs.trust.network.message;

import com.higgs.trust.network.Address;

/**
 * @author duhongming
 * @date 2018/8/21
 */
public class NetworkRequest extends NetworkMessage {

    /**
     * The action name of authentication request.
     */
    public static final String AUTH_ACTION_NAME = "AUTHENTICATE";

    private String actionName;
    private Address sender;

    public NetworkRequest(long id, String actionName, byte[] payload) {
        super(id, payload);
        this.actionName = actionName;
    }

    public NetworkRequest sender(Address sender) {
        this.sender = sender;
        return this;
    }

    public Address sender() {
        return this.sender;
    }

    public String actionName() {
        return this.actionName;
    }

    @Override
    public Type type() {
        return Type.REQUEST;
    }
}
