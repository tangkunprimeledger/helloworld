package com.higgs.trust.network;

/**
 * @author duhongming
 * @date 2018/8/30
 */
public interface NetworkListener {
    public static enum Event {
        STARTED,
        JOIN,
        LEAVE,
        OFFLINE,
    }

    /**
     * handle
     * @param event
     * @param message
     */
    void handle(Event event, Object message);
}
