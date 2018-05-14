package com.higgs.trust.rs.custom.model.enums.identity;

/*  
 * @desc
 * @author WangQuanzhou
 * @date 2018/3/8 14:25
 */
public enum IdentityEnum {
    PAUSE("PAUSE"),
    RUNNING("RUNNING");

    private String mngCode;

    private IdentityEnum(String mngCode) {
        this.mngCode = mngCode;
    }

    public static IdentityEnum getByMngCode(String mngCode) {
        IdentityEnum[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            IdentityEnum status = var1[var3];
            if (status.getMngCode().equals(mngCode)) {
                return status;
            }
        }

        return null;
    }

    public String getMngCode() {
        return this.mngCode;
    }
}
