package com.higgs.trust.common.utils;

import com.higgs.trust.common.constant.Constant;
import com.higgs.trust.common.constant.LoggerName;
import com.higgs.trust.common.enums.MonitorTargetEnum;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by turan on 2017/6/23.
 */
public class MonitorLogUtils {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LoggerName.PL_MONITOR_LOGGER);

    /**
     * 监控日志的输出
     *
     * @param module 监控的模块
     * @param target 监控的指标
     * @param type   指标的数据类型,可以是说一下类型 --text: 输出的是文本类型（json格式） --float : 输出的是浮点类型（数值类型不需要用json格式）
     *               --int : 输出的是整数类型（数值类型不需要用json格式）
     *               <p>
     *               example： 如需要统计单位时间内synchronizer生成的package的数量，那么在每次生成一个package后打印一条如下格式的日志:
     * @param info   输出的的具体内容
     * @Test public void test(){ LogUtils.logMonitor("synchronizer","pacakge_count","int",1); }
     */
    public static <T> void logMonitorInfo(String module, MonitorTargetEnum target, TargetInfoType type, T info) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = dateFormat.format(new Date());
        LOGGER.info(String.format("Monitor,%s,%s,%s,%s,%s", module, target.getMonitorTarget(), date, type, String.valueOf(info)));
    }

    public static <T> void logIntMonitorInfo(MonitorTargetEnum target, T info) {
        logMonitorInfo(Constant.APP_NAME, target, TargetInfoType.INT, info);
    }

    public static <T> void logFloatMonitorInfo(MonitorTargetEnum target, T info) {
        logMonitorInfo(Constant.APP_NAME, target, TargetInfoType.FLOAT, info);
    }

    public static <T> void logTextMonitorInfo(MonitorTargetEnum target, T info) {
        logMonitorInfo(Constant.APP_NAME, target, TargetInfoType.TEXT, info);
    }

    @Deprecated
    public static <T> void logMonitorInfo(String module, String target, TargetInfoType type, T info) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = dateFormat.format(new Date());
        LOGGER.info(String.format("Monitor,%s,%s,%s,%s,%s", module, target, date, type, String.valueOf(info)));
    }

    @Deprecated
    public static <T> void logIntMonitorInfo(String target, T info) {
        logMonitorInfo(Constant.APP_NAME, target, TargetInfoType.INT, info);
    }

    @Deprecated
    public static <T> void logFloatMonitorInfo(String target, T info) {
        logMonitorInfo(Constant.APP_NAME, target, TargetInfoType.FLOAT, info);
    }

    @Deprecated
    public static <T> void logTextMonitorInfo(String target, T info) {
        logMonitorInfo(Constant.APP_NAME, target, TargetInfoType.TEXT, info);
    }


    public enum TargetInfoType {
        TEXT("text"), FLOAT("float"), INT("int");

        String type;

        TargetInfoType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}

