package com.higgs.trust.slave.common.listener;

import com.higgs.trust.evmcontract.core.TransactionResultInfo;
import com.higgs.trust.slave.model.bo.BlockHeader;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author duhongming
 * @date 2018/12/17
 */
public class CompositeTrustListener implements TrustListener {

    private static abstract class RunnableInfo implements Runnable {
        private TrustListener listener;
        private String info;

        public RunnableInfo(TrustListener listener, String info) {
            this.listener = listener;
            this.info = info;
        }

        @Override
        public String toString() {
            return "RunnableInfo: " + info + " [listener: " + listener.getClass() + "]";
        }
    }

    @Autowired
    EventDispatchThread eventDispatchThread = EventDispatchThread.getDefault();

    protected List<TrustListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(TrustListener listener) {
        listeners.add(listener);
    }

    public void removeListener(TrustListener listener) {
        listeners.remove(listener);
    }


    @Override
    public void onBlock(BlockHeader header) {
        for (final TrustListener listener : listeners) {
            eventDispatchThread.invokeLater(new RunnableInfo(listener, "onBlock") {
                @Override
                public void run() {
                    listener.onBlock(header);
                }
            });
        }
    }

    @Override
    public void onTransactionExecuted(TransactionResultInfo resultInfo) {
        for (final TrustListener listener : listeners) {
            eventDispatchThread.invokeLater(new RunnableInfo(listener, "onBlock") {
                @Override
                public void run() {
                    listener.onTransactionExecuted(resultInfo);
                }
            });
        }
    }
}
