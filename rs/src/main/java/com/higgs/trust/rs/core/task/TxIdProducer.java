package com.higgs.trust.rs.core.task;

import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author liuyu
 */
@Component @Slf4j public class TxIdProducer implements InitializingBean {
    private ArrayBlockingQueue<TxIdBO> queueInit = null;
    private ArrayBlockingQueue<TxIdBO> queueWait = null;
    @Value("${rs.core.schedule.queueSize:10000}") private int maxSize;

    @Override public void afterPropertiesSet() throws Exception {
        queueInit = new ArrayBlockingQueue<>(maxSize);
        queueWait = new ArrayBlockingQueue<>(maxSize);
    }

    /**
     * put txId
     *
     * @param txId
     */
    public void put(TxIdBO txId) {
        try {
            if(txId.getStatusEnum() == CoreTxStatusEnum.INIT) {
                queueInit.put(txId);
            }else if(txId.getStatusEnum() == CoreTxStatusEnum.WAIT){
                queueWait.put(txId);
            }
        } catch (Exception e) {
            log.error("put txId has error", e);
        }
        log.info("queue - initSize:{},waitSize:{}", queueInit.size(),queueWait.size());
    }

    /**
     * take txId for status
     *
     * @return
     */
    public TxIdBO take(CoreTxStatusEnum statusEnum) {
        try {
            if(statusEnum == CoreTxStatusEnum.INIT) {
                return queueInit.take();
            }else if(statusEnum == CoreTxStatusEnum.WAIT){
                return queueWait.take();
            }
        } catch (Exception e) {
            log.error("take txId has error", e);
        }
        return null;
    }
}
