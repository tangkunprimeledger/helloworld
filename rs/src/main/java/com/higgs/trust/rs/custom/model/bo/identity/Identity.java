package com.higgs.trust.rs.custom.model.bo.identity;

import com.higgs.trust.rs.custom.model.BaseBO;

/*
 * @desc 存证实体类
 * @author WangQuanzhou
 * @date 2018/3/2 18:03
 */  
public class Identity extends BaseBO {

    private static final long serialVersionUID = 1436723757568942247L;
    /**
     * 存证的key
     */
    private String key;


    /**
     * 存证value
     */
    private String value;


    /**
     * 请求唯一id，幂等判断的依据
     */
    private String reqNo;

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
}
