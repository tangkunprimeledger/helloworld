package com.higgs.trust.network;

import java.io.IOException;

/**
 * @author duhongming
 * @date 2018/8/23
 */
public class MessagingException extends IOException {

    public MessagingException(String message) {
        super(message);
    }

    public MessagingException(String message, Throwable t) {
        super(message, t);
    }

    public MessagingException(Throwable t) {
        super(t);
    }

    /**
     * Exception indicating no remote registered remote handler.
     */
    public static class NoRemoteHandler extends MessagingException {
        public NoRemoteHandler() {
            super("No remote message handler registered for this message");
        }
    }

    /**
     * Exception indicating handler failure.
     */
    public static class RemoteHandlerFailure extends MessagingException {
        public RemoteHandlerFailure() {
            super("Remote handler failed to handle message");
        }
    }

    /**
     * Exception indicating failure due to invalid message structure such as an incorrect preamble.
     */
    public static class ProtocolException extends MessagingException {
        public ProtocolException() {
            super("Failed to process message due to invalid message structure");
        }
    }
}
