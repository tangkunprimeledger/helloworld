package com.higgs.trust.rs.custom.model;


import javax.validation.constraints.NotNull;

/**
 *
 *
 * @author yangjiyun
 * @create 2017 -06-21 15:32
 */
public class MaintanenceModeBO extends BaseBO {
    @NotNull
    private Boolean maintanenceMode;

    public Boolean getMaintanenceMode() {
        return maintanenceMode;
    }

    public void setMaintanenceMode(Boolean maintanenceMode) {
        this.maintanenceMode = maintanenceMode;
    }
}
