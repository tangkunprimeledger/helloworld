package com.higgs.trust.slave.common.enums;

public enum NodeStateEnum {
    Starting("Starting"), SelfChecking("SelfChecking"), AutoSync("AutoSync"), ArtificialSync("ArtificialSync"), Running(
        "Running"), Offline("Offline");
    /**
     * 描述说明
     */
    private final String description;

    NodeStateEnum(String description) {
        this.description = description;
    }

}
