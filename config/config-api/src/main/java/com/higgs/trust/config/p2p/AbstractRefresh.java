/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.config.p2p;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author suimi
 * @date 2018/6/29
 */
public abstract class AbstractRefresh implements Refreshable {

    private final AtomicBoolean refresh = new AtomicBoolean(false);

    @Override public void setRefresh() {
        synchronized (refresh) {
            refresh.set(true);
        }
    }

    @Override public boolean needRefresh() {
        return refresh.get();
    }

    @Override public void refreshIfNeed() {
        if (this.refresh.get()) {
            synchronized (this.refresh) {
                refresh();
                this.refresh.compareAndSet(true, false);
            }
        }
    }

    public abstract void refresh();
}
