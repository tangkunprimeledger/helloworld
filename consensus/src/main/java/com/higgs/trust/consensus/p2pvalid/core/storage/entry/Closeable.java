package com.higgs.trust.consensus.p2pvalid.core.storage.entry;

import java.io.Serializable;

/**
 * @author cwy
 */
public class Closeable implements Serializable {
    private static final long serialVersionUID = -1L;
    private boolean closed;

    public boolean isClosed() {
        return closed;
    }

    public void close() {
        closed = true;
    }
}
