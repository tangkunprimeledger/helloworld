package com.higgs.trust.rs.core.task;

import com.higgs.trust.rs.core.api.CoreTransactionService;
import com.higgs.trust.rs.core.api.enums.CoreTxStatusEnum;
import com.higgs.trust.rs.core.bo.CoreTxBO;
import com.higgs.trust.rs.core.dao.po.CoreTransactionPO;
import com.higgs.trust.rs.core.repository.CoreTxRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
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
    @Value("${rs.core.schedule.taskInitSize:30}") private int taskInitSize;
    @Value("${rs.core.schedule.taskWaitSize:10}") private int taskWaitSize;
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
        executorInit = new ScheduledThreadPoolExecutor(taskInitSize);
        for (int i = 0; i < taskInitSize; i++) {
            executorInit.scheduleAtFixedRate(new RsTaskHandler(CoreTxStatusEnum.INIT), 0, interval, TimeUnit.MILLISECONDS);
        }
        //for wait
        executorWait = new ScheduledThreadPoolExecutor(taskWaitSize);
        for (int i = 0; i < taskWaitSize; i++) {
            executorWait.scheduleAtFixedRate(new RsTaskHandler(CoreTxStatusEnum.WAIT), 0, interval, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * task handler
     */
    class RsTaskHandler implements Runnable {
        private CoreTxStatusEnum statusEnum;
        private int maxSize;

        public RsTaskHandler(CoreTxStatusEnum statusEnum) {
            this.statusEnum = statusEnum;
            this.maxSize = statusEnum == CoreTxStatusEnum.INIT ? 50 : 200;
        }

        @Override public void run() {
            List<TxIdBO> list = new ArrayList<>(maxSize);
            int i = 0;
            while (i < maxSize){
                TxIdBO txIdBO = txIdProducer.take(this.statusEnum);
                if (txIdBO != null) {
                    list.add(txIdBO);
                }
                i++;
            }
            if(CollectionUtils.isEmpty(list)){
                return;
            }
            //process init
            if (statusEnum == CoreTxStatusEnum.INIT) {
                processInit(list);
                return;
            }
            //process wait
            if (statusEnum == CoreTxStatusEnum.WAIT) {
                processWait(list);
                return;
            }
        }

        /**
         * process init
         *
         * @param list
         */
        private void processInit(List<TxIdBO> list) {
            if(log.isDebugEnabled()) {
                log.debug("processInit.size:{}", list.size());
            }
            list.forEach(entry->{
                try {
                    coreTransactionService.processInitTx(entry.getTxId());
                } catch (Throwable e) {
                    log.error("task.processInit has error", e);
                }
            });
        }

        /**
         * process wait
         *
         * @param list
         */
        private void processWait(List<TxIdBO> list) {
            List<String> ids = new ArrayList<>(list.size());
            List<CoreTxBO> coreTxs = new ArrayList<>(list.size());
            list.forEach(entry->{
                ids.add(entry.getTxId());
            });
            List<CoreTransactionPO> pos = coreTxRepository.queryByTxIds(ids);
            pos.forEach(entry->{
                CoreTxBO coreTxBO = coreTxRepository.convertTxBO(entry);
                coreTxs.add(coreTxBO);
            });
            if(log.isDebugEnabled()){
                log.debug("submitToSlave.size:{}",coreTxs.size());
            }
            coreTransactionService.submitToSlave(coreTxs);
        }
    }
}
