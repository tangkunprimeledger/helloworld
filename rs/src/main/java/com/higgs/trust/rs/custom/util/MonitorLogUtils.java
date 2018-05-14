package com.higgs.trust.rs.custom.util;

import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by turan on 2017/6/23.
 */
public class MonitorLogUtils {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("PL-Monitor-info");
    private static final org.slf4j.Logger LOGGER_ERROR = LoggerFactory.getLogger("PL-Monitor-error");

    /**
     * 监控日志的输出
     *
     * @param module 监控的模块
     * @param target 监控的指标
     * @param type   指标的数据类型,可以是说一下类型
     *               --text: 输出的是文本类型（json格式）
     *               --float : 输出的是浮点类型（数值类型不需要用json格式）
     *               --int : 输出的是整数类型（数值类型不需要用json格式）
     *               <p>
     *               example：
     *               如需要统计单位时间内synchronizer生成的package的数量，那么在每次生成一个package后打印一条如下格式的日志:
     * @param info   输出的的具体内容
     * @param <T>
     * @Test public void test(){
     * LogUtils.logMonitor("synchronizer","pacakge_count","int",1);
     * }
     */
    public static <T> void logMonitorInfo(String module, String target, TargetInfoType type, T info) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = dateFormat.format(new Date());
        LOGGER.info(String.format("Monitor,%s,%s,%s,%s,%s", module, target, date, type, String.valueOf(info)));
    }

    public static <T> void logBankChainIntMonitorInfo(String target, T info) {
        logMonitorInfo("bankchain", target, TargetInfoType.INT, info);
    }

    public static <T> void logBankChainFloatMonitorInfo(String target, T info) {
        logMonitorInfo("bankchain", target, TargetInfoType.FLOAT, info);
    }

    public static <T> void logBankChainTextMonitorInfo(String target, T info) {
        logMonitorInfo("bankchain", target, TargetInfoType.TEXT, info);
    }

    /**
     * 输出异常和错误信息
     *
     * @param module           模块名
     * @param keyWord          关键字
     * @param format,argumnets 参数传入同普通日志打印
     */

    public static void logMonitorError(String module, String keyWord, String format, Object... arguments) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = dateFormat.format(new Date());
        LOGGER_ERROR.error(String.format("Monitor,%s,%s,%s", module, date, keyWord));
        LOGGER.error(format, arguments);
    }

    /**
     * 对于log输出，如果需要添加一个监控点，这个是关键字的监控，加上一个keyWord即可
     * example
     * logUcfCoinErrorMonitor("test", "error {}", "test name", e);
     *
     * @param keyWord
     * @param format
     * @param arguments
     */
    public static void logBankChainErrorMonitor(String keyWord, String format, Object... arguments) {
        logMonitorError("bankchain", keyWord, format, arguments);
    }

    public enum TargetInfoType {
        TEXT("text"),
        FLOAT("float"),
        INT("int");

        String type;

        TargetInfoType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}

