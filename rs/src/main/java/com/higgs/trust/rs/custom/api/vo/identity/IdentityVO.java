package com.higgs.trust.rs.custom.api.vo.identity;

import com.higgs.trust.rs.custom.api.vo.BaseVO;
import org.hibernate.validator.constraints.NotBlank;

/*
 * @desc 存证实体类
 * @author WangQuanzhou
 * @date 2018/3/2 18:03
 */  
public class IdentityVO extends BaseVO {

    private static final long serialVersionUID = 539354061605972439L;
    /**
     * 存证的key
     */
    @NotBlank
    private String key;


    /**
     * 存证value
     */
    @NotBlank
    private String value;


    /**
     * 请求唯一id，幂等判断的依据
     */
    @NotBlank
    private String reqNo;

    /**
     * 存证状态
     */
    private String status;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getReqNo() {
        return reqNo;
    }

    public void setReqNo(String reqNo) {
        this.reqNo = reqNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
