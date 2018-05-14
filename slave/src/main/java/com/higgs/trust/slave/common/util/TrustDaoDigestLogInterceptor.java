package com.higgs.trust.slave.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Repository;

/**
 * DAO层方法摘要日志
 * <p>
 * <pre>
 * <b>日志格式</b>
 * [(方法名,是否成功,耗时)(业务参数)]
 * <b>格式说明</b>：
 * 数据源名称：数据源的名称
 * 方法名：方法名称，方法所在的类名.方法名,如MerkleTreeDAO.add;
 * 是否成功：Y→成功，N→失败
 * 业务参数：DAO调用的入参(日志debug模式下打印)
 * </pre>
 *
 * @author pengdi
 * @date
 */
@Slf4j @Aspect @Repository public class TrustDaoDigestLogInterceptor {

    /**
     * 日志默认值
     */
    protected static final String LOG_DEFAULT = "-";

    /**
     * 日志前缀
     */
    protected static final String LOG_PREFIX = "[";

    /**
     * 日志后缀
     */
    protected static final String LOG_SUFFIX = "]";

    /**
     * 日志参数前缀
     */
    protected static final String LOG_PARAM_PREFIX = "(";

    /**
     * 日志参数后缀
     */
    protected static final String LOG_PARAM_SUFFIX = ")";

    /**
     * 日志分隔符(英文分号)
     */
    protected static final String LOG_SEP = ",";

    /**
     * 日志分隔符(英文点号)
     */
    protected static final String LOG_SEP_POINT = ".";

    /**
     * 成功
     */
    protected static final String YES = "Y";

    /**
     * 失败
     */
    protected static final String NO = "N";

    /**
     * 时间
     */
    protected static final String TIME_UNIT = "ms";

    /**
     * @param pj
     * @return
     * @throws Throwable
     */
    @Around("execution(* com.higgs.trust.slave.dao.*.*Dao.*(..)))") public Object invoke(ProceedingJoinPoint pj)
        throws Throwable {

        //日志开始时间
        long startTime = System.currentTimeMillis();
        //DAO方法名
        String methodName = pj.toShortString();

        //是否处理成功
        boolean isSuccess = true;

        try {
            return pj.proceed();
        } catch (Throwable t) {
            //调用失败
            isSuccess = false;

            throw t;
        } finally {
            //确保任何情况下业务都能正常进行
            try {
                //耗时
                long elapseTime = System.currentTimeMillis() - startTime;

                // 打印DAO摘要日志
//                log.info(constructLogString(methodName, isSuccess, elapseTime));
            } catch (Exception e) {

                log.error("记录income调用DAO摘要日志出错!", e);
            }
        }
    }

    /**
     * 构造记录日志的字符串
     * <pre>
     * 格式:[(数据源名称,方法名,是否成功,耗时)(业务参数)]
     * </pre>
     *
     * @param methodName
     * @param isSuccess
     * @param elapseTime
     * @return
     */
    private String constructLogString(String methodName, boolean isSuccess, long elapseTime) {
        StringBuffer sb = new StringBuffer();

        sb.append(LOG_PREFIX);
        sb.append(LOG_PARAM_PREFIX);
        sb.append(StringUtils.defaultIfBlank(methodName, LOG_DEFAULT));
        sb.append(LOG_SEP);
        sb.append(isSuccess ? YES : NO);
        sb.append(LOG_SEP);
        sb.append(elapseTime);
        sb.append(TIME_UNIT);
        sb.append(LOG_PARAM_SUFFIX);
        sb.append(LOG_SUFFIX);

        return sb.toString();
    }
}
