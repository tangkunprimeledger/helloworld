package com.higgs.trust.network.message;

/**
 * @author duhongming
 * @date 2018/8/21
 */
public class NetworkResponse extends NetworkMessage {

    private final Status status;

    public NetworkResponse(long id, byte[] payload) {
        this(id, Status.OK, payload);
    }

    public NetworkResponse(long id, Status status, byte[] payload) {
        super(id, payload);
        this.status = status;
    }

    @Override
    public Type type() {
        return Type.RESPONSE;
    }

    public Status status() {
        return status;
    }

    public enum Status {

        /**
         * Unauthorized
         */
        UNAUTHORIZED(0),

        /**
         * All ok.
         */
        OK(1),

        /**
         * Response status signifying no registered handler.
         */
        ERROR_NO_HANDLER(1),

        /**
         * Response status signifying an exception handling the message.
         */
        ERROR_HANDLER_EXCEPTION(2),

        /**
         * Response status signifying invalid message structure.
         */
        PROTOCOL_EXCEPTION(3);

        private final int id;

        Status(int id) {
            this.id = id;
        }

        /**
         * Returns the unique status ID.
         *
         * @return the unique status ID.
         */
        public int id() {
            return id;
        }

        /**
         * Returns the status enum associated with the given ID.
         *
         * @param id the status ID.
         * @return the status enum for the given ID.
         */
        public static Status forId(int id) {
            switch (id) {
                case 0:
                    return OK;
                case 1:
                    return ERROR_NO_HANDLER;
                case 2:
                    return ERROR_HANDLER_EXCEPTION;
                case 3:
                    return PROTOCOL_EXCEPTION;
                default:
                    throw new IllegalArgumentException("Unknown status ID " + id);
            }
        }
    }
}
