package com.higgs.trust.common.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

/**
 * @author duhongming
 * @date 2018/10/12
 */
public class LogLevelChanger {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LogLevelChanger.class);

    /**
     * change log level
     * @param logName
     * @param level OFF, ERROR, WARN, INFO, DEBUG, TRACE, ALL
     */
    public static void change(String logName, String level) {
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            if (logName.endsWith("$")) {
                String finalName = logName.substring(0, logName.length() - 1);
                loggerContext.getLoggerList().forEach(x -> {
                    if (x.getName().endsWith(finalName)) {
                        x.setLevel(Level.valueOf(level));
                    }
                });
            } else {
                loggerContext.getLogger(logName).setLevel(Level.valueOf(level));
            }
        } catch (Exception e) {
            LOGGER.error("change log level error: ", e);
        }
    }
}
