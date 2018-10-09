package com.higgs.trust.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PrimeTraceUtil {

    private static Tracer tracer;
    private static String NEW_SPAN_NAME = "newSpan";

    /**
     * 获取当前线程的trace span
     *
     * @return
     */
    public static Span getSpan() {
        if (tracer == null){
            return null;
        }
        return tracer.getCurrentSpan();
    }

    /**
     * 获取当前线程的traceid
     *
     * @return
     */
    public static long getTraceId() {
        Span span = getSpan();
        if (span != null) {
            return span.getTraceId();
        }
        return 0;
    }

    /**
     * 同时关闭当前线程的traceid(如果存在)，然后才打开指定的traceid，并返回span对象，以便使用后手动close
     *
     * @param traceId
     * @return
     */
    public static Span openNewTracer(long traceId) {
        Span oldSpan = getSpan();
        closeSpan(oldSpan);

        Span newSpan = Span.builder().name(NEW_SPAN_NAME)
                .traceIdHigh(0L)
                .traceId(traceId)
                .spanId(traceId).build();
        return tracer.createSpan(NEW_SPAN_NAME, newSpan);
    }

    /**
     * 每次使用后需要手动关闭span
     *
     * @param span
     */
    public static void closeSpan(Span span) {
        if (tracer != null && span != null) {
            tracer.close(span);
        }
    }

    @Autowired
    public void setBeanFactory(BeanFactory beanFactory) throws Exception {
        PrimeTraceUtil.tracer = beanFactory.getBean(Tracer.class);
    }
}