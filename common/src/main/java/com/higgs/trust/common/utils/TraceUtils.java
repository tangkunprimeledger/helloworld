package com.higgs.trust.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.Span;

import java.util.Random;

/**
 * @author cwy
 */
@Slf4j
public class TraceUtils {

    public static Span createSpan(){
        try{
            return PrimeTraceUtil.openNewTracer(new Random().nextLong());
        }catch (Throwable throwable){
            log.warn("create span error {}", throwable);
        }
        return null;
    }
    /**
     * create new span
     * @param traceId tranceId for new span
     * @return Span
     */
    public static Span createSpan(Long traceId){
        try{
            if(null != traceId && 0 != traceId){
                return PrimeTraceUtil.openNewTracer(traceId);
            }
        }catch (Exception e){
            log.warn("{}",e.getMessage());
        }
        return null;
    }

    /**
     * @return Long
     */
    public static Long getTraceId(){
        return PrimeTraceUtil.getTraceId();
    }

    /**
     * @param span span to close
     */
    public static void closeSpan(Span span){
        try{
            if(null != span){
                PrimeTraceUtil.closeSpan(span);
            }
        }catch (Exception e){
            log.warn("{}",e.getMessage());
        }
    }
}
