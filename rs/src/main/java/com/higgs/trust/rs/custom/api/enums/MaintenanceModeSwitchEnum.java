package com.higgs.trust.rs.custom.api.enums;

/**
 * @author lingchao
 */
public enum MaintenanceModeSwitchEnum {
    /**
     * 维护模式工作中
     */
    ON("ON",true),

    /**
     * 维护模式未工作
     */
    OFF("OFF",false),
    ;

    private String mngCode;
    private boolean on;

    MaintenanceModeSwitchEnum(String mngCode, boolean on) {
        this.mngCode = mngCode;
        this.on = on;
    }

    public String getMngCode() {
        return mngCode;
    }

	public boolean isOn() {
		return on;
	}
    
    
}
