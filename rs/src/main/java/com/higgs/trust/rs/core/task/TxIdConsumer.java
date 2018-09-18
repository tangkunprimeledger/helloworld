package com.higgs.trust.rs.core.task;

import com.google.common.collect.Lists;
import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.bo.CoreTxBO;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import com.higgs.trust.rs.core.repository.CoreTxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author liuyu
 * @description
 * @date 2018-07-04
 */
@Component @Slf4j public class TxIdConsumer implements InitializingBean {
    @Autowired private TxIdProducer txIdProducer;
    @Autowired private CoreTransactionService coreTransactionService;
    @Autowired private CoreTxRepository coreTxRepository;
    @Value("${rs.core.schedule.taskSize:8}") private int size;
    @Value("${rs.core.schedule.interval:1}") private Long interval;

    private ScheduledExecutorService executorInit;
    private ScheduledExecutorService executorWait;

    @Override public void afterPropertiesSet() throws Exception {
        startConsume();
    }

    /**
     * start
     */
    public void startConsume() {
        //for init
        if (executorInit == null || executorInit.isShutdown() || executorInit.isTerminated()) {
            executorInit = new ScheduledThreadPoolExecutor(size);
            for (int i = 1; i < (size + 1); i++) {
                executorInit.scheduleAtFixedRate(new RsTaskHandler(CoreTxStatusEnum.INIT), 0, interval, TimeUnit.MILLISECONDS);
            }
        }
        //for wait
        if (executorWait == null || executorWait.isShutdown() || executorWait.isTerminated()) {
            executorWait = new ScheduledThreadPoolExecutor(size);
            for (int i = 1; i < (size + 1); i++) {
                executorWait.scheduleAtFixedRate(new RsTaskHandler(CoreTxStatusEnum.INIT), 0, interval, TimeUnit.MILLISECONDS);
            }
        }
    }

    /**
     * task handler
     */
    class RsTaskHandler implements Runnable {
        private CoreTxStatusEnum statusEnum;

        public RsTaskHandler(CoreTxStatusEnum statusEnum) {
            this.statusEnum = statusEnum;
        }

        @Override public void run() {
            TxIdBO txIdBO = txIdProducer.take(this.statusEnum);
            if (txIdBO == null) {
                return;
            }
            CoreTxStatusEnum statusEnum = txIdBO.getStatusEnum();
            //process init
            if (statusEnum == CoreTxStatusEnum.INIT) {
                processInit(txIdBO);
                return;
            }
            //process wait
            if (statusEnum == CoreTxStatusEnum.WAIT) {
                processWait(txIdBO);
                return;
            }
        }

        /**
         * process init
         *
         * @param txIdBO
         */
        private void processInit(TxIdBO txIdBO) {
            try {
                coreTransactionService.processInitTx(txIdBO.getTxId());
            } catch (Throwable e) {
                log.error("task.processInit has error", e);
            }
        }

        /**
         * process wait
         *
         * @param txIdBO
         */
        private void processWait(TxIdBO txIdBO) {
            try {
                CoreTransactionPO po = coreTxRepository.queryByTxId(txIdBO.getTxId(), false);
                CoreTxBO coreTxBO = coreTxRepository.convertTxBO(po);
                coreTransactionService.submitToSlave(Lists.newArrayList(coreTxBO));
            }catch (Throwable e){
                log.error("task.processWait has error", e);
            }
        }
    }
}
