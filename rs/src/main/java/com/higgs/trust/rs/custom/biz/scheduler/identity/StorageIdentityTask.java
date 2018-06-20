package com.higgs.trust.rs.custom.biz.scheduler.identity;

import com.higgs.trust.rs.custom.api.identity.IdentityService;
import com.higgs.trust.rs.custom.dao.BankChainRequestDAO;
import com.higgs.trust.rs.custom.dao.po.BankChainRequestPO;
import com.higgs.trust.rs.custom.model.convertor.identity.POToBOConvertor;
import com.higgs.trust.slave.api.enums.manage.InitPolicyEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.higgs.trust.Application.COMMON_THREAD_POOL;
import static com.higgs.trust.Application.INITIAL_DELAY;

/*
 * @desc 异步下发存证数据定时任务
 * @author WangQuanzhou
 * @date 2018/3/7 16:59
 */
@Service @Slf4j public class StorageIdentityTask implements InitializingBean {
    /**
     * 间隔时间
     */
    public static final long PERIOD_TX = 5;
    @Autowired private BankChainRequestDAO bankChainRequestDAO;
    @Autowired private IdentityService identityService;

    @Override public void afterPropertiesSet() throws Exception {
        //ScheduleWithFixedDelay 取决于每次任务执行的时间长短，是基于不固定时间间隔进行任务调度
        //只有非管控模式才运行异常推进
        COMMON_THREAD_POOL.scheduleWithFixedDelay(new Runnable() {
            @Override public void run() {
                    process();
            }
        }, INITIAL_DELAY, PERIOD_TX, TimeUnit.SECONDS);
    }

    public void process() {
        // 定时下发存证数据
        log.info("[process]: start handle");
        List<BankChainRequestPO> list = bankChainRequestDAO.queryRequest();
        log.info("[process]: start process,the list size={}", list.size());
        if (CollectionUtils.isEmpty(list)) {
//            log.info("没有需要下发的存证数据");
            return;
        }

        for (BankChainRequestPO bankChainRequestPO : list) {

            // 判断是否是存证类业务
            if (!InitPolicyEnum.STORAGE.getType().equals(bankChainRequestPO.getBizType())) {
                return;
            }

            try {
                log.info("定时任务处理存证下发业务  reqNo :{}，开始处理", bankChainRequestPO.getReqNo());
                identityService.asyncSendIdentity(POToBOConvertor.convertBankChainRequestPOToBO(bankChainRequestPO));
                log.info("定时任务处理存证下发业务  reqNo :{} 处理结束", bankChainRequestPO.getReqNo());
            } catch (Throwable e) {
                log.error("[StorageIdentityTask] process failed,reqNo={}", bankChainRequestPO.getReqNo(), e);
            }

        }
        log.info("[process]: end handle,the list size={}", list.size());
    }

}

