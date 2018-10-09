package com.higgs.trust.network.message;

/**
 * @author duhongming
 * @date 2018/9/3
 */
public abstract class NetworkMessage {
    public enum Type {
        REQUEST(1),
        RESPONSE(2);

        private final int id;

        Type(int id) {
            this.id = id;
        }

        /**
         * Returns the unique message type ID.
         *
         * @return the unique message type ID.
         */
        public int id() {
            return id;
        }

        /**
         * Returns the message type enum associated with the given ID.
         *
         * @param id the type ID.
         * @return the type enum for the given ID.
         */
        public static Type forId(int id) {
            switch (id) {
                case 1:
                    return REQUEST;
                case 2:
                    return RESPONSE;
                default:
                    throw new IllegalArgumentException("Unknown status ID " + id);
            }
        }
    }

    private long id;
    private byte[] payload;

    public NetworkMessage(long id, byte[] payload) {
        this.id = id;
        this.payload = payload;
    }

    public long id() {
        return this.id;
    }

    public byte[] payload() {
        return payload;
    }

    /**
     * Returns the message type
     *
     * @return
     */
    public abstract Type type();

    public boolean isRequest() {
        return type() == Type.REQUEST;
    }
}
