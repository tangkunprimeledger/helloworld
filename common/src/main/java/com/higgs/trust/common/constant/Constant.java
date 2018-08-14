package com.higgs.trust.common.constant;

public class Constant {
    public static final String APP_NAME = "slave";

    /**
     * 成功(Y)/失败(N)
     */
    public static final String SUCCESS = "Y";

    public static final String FAIL = "N";

    public static final String SPLIT_SLASH = "_";

    /**
     * 监控后缀
     */
    public static final String LATENCY_SUFFIX = "latency";

    public static final String TPS_SUFFIX = "tps";

    public static final String MONITOR_TEXT = "monitor_text";

    public static final Long GENESIS_HEIGHT = 1L;

    public static final int MAX_BLOCKING_QUEUE_SIZE = 1000;

    public static final int MAX_PENDING_TX_QUEUE_SIZE = 20000;

    public static final int MAX_EXIST_MAP_SIZE = 50000;

    public static final int PERF_LOG_THRESHOLD = 30000;

}
