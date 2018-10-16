package com.higgs.trust.consensus.config;

public enum NodeStateEnum {
    Starting("启动中"), Initialize("初始化"), StartingConsensus("启动协议层"), SelfChecking("自检"), AutoSync("自动同步"), ArtificialSync("人工同步"), Standby("备用"), Running("运行中"), Offline("下线");
    /**
     * 描述说明
     */
    private final String description;

    NodeStateEnum(String description) {
        this.description = description;
    }

}
