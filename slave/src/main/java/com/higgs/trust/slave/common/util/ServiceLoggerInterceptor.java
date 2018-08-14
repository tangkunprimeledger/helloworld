package com.higgs.trust.slave.common.util;

import com.higgs.trust.common.utils.MonitorLogUtils;
import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.common.constant.LoggerName;
import com.higgs.trust.slave.common.enums.SlaveErrorEnum;
import com.higgs.trust.slave.common.exception.SlaveException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

/**
 * @Description:服务拦截器
 * @author: pengdi
 **/
@Aspect @Component public class ServiceLoggerInterceptor {
    private static final Logger SERVICE_DIGEST_LOGGER = LoggerFactory.getLogger(LoggerName.SERVICE_DIGEST_LOGGER);

    @Around("execution(* com.higgs.trust.slave.core.service.pack.PackageService.*(..)))") public Object serviceLog(ProceedingJoinPoint pj)
        throws Throwable {
        //get the start time
        long startTime = System.currentTimeMillis();

        boolean success = true;

        Throwable exception = null;
        try {
            return pj.proceed();
        } catch (DuplicateKeyException e) {
            //idempotent is not Exception
            throw e;
        } catch (Throwable e) {
            success = false;
            exception = e;
            throw e;
        } finally {
            try {
                String serviceName = pj.getSignature().getDeclaringType().getSimpleName();
                String methodName = pj.getSignature().getName();
                digestLog(serviceName, methodName, success, startTime);

                if (!success) {
                    errorMonitorLog(exception);
                }
            } catch (Exception e) {
                SERVICE_DIGEST_LOGGER.error("digest log exception:", e);
            }
        }
    }

    /**
     * 打印流水日志
     */
    private void digestLog(String serviceName, String methodName, boolean success, long startTime) {
        long latency = System.currentTimeMillis() - startTime;

        //打印服务流水日志
        SERVICE_DIGEST_LOGGER
            .info("({}.{}, {}ms, {})", serviceName, methodName, latency, success ? Constant.SUCCESS : Constant.FAIL);

        //打印监控lantency日志
        String moduleLatency =
            new StringBuilder(serviceName).append(Constant.SPLIT_SLASH).append(methodName).append(Constant.SPLIT_SLASH)
                .append(Constant.LATENCY_SUFFIX).toString();
        MonitorLogUtils.logIntMonitorInfo(moduleLatency, latency);

        //打印TPS的日志
        String moduleTps =
            new StringBuilder(serviceName).append(Constant.SPLIT_SLASH).append(methodName).append(Constant.SPLIT_SLASH)
                .append(Constant.TPS_SUFFIX).toString();
        MonitorLogUtils.logIntMonitorInfo(moduleTps, 1);
    }

    /**
     * 打印异常monitor日志
     */
    private void errorMonitorLog(Throwable e) {
        if (e instanceof SlaveException) {
            SlaveException slaveException = (SlaveException)e;
            MonitorLogUtils.logTextMonitorInfo(Constant.MONITOR_TEXT, slaveException.getCode().toString());
        } else {
            MonitorLogUtils
                .logTextMonitorInfo(Constant.MONITOR_TEXT, SlaveErrorEnum.SLAVE_UNKNOWN_EXCEPTION.toString());
        }
    }
}
