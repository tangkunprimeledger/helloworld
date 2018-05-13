package com.higgs.trust.rs.custom.api.enums;

/**  
 * @desc TODO  
 * @author WangQuanzhou
 * @date 2018/5/12 17:54    
 */  
public enum UpperRequestRunningStatus {

    /**
     * 暂停
     */
    PAUSE("PAUSE"),

    /**
     * 运行中
     */
    RUNNING("RUNNING"),;

    private String mngCode;

    UpperRequestRunningStatus(String mngCode) {
        this.mngCode = mngCode;
    }

    public static UpperRequestRunningStatus getByMngCode(String mngCode) {
        for (UpperRequestRunningStatus status : UpperRequestRunningStatus.values()) {
            if (status.getMngCode().equals(mngCode)) {
                return status;
            }
        }
        return null;
    }

    public String getMngCode() {
        return mngCode;
    }
}
