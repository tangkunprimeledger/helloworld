package com.higgs.trust.rs.custom.model.bo.identity;

import com.higgs.trust.rs.custom.model.BaseBO;

import java.util.Date;

/*
 * @desc 暂存 存证请求数据的相关数据对象
 * @author WangQuanzhou
 * @date 2018/3/5 12:02
 */  
public class IdentityRequest extends BaseBO {

    private static final long serialVersionUID = -6405043635467806518L;
    /**
     * 主键id  自增
     */
    private Long id;


    /**
     * 请求唯一标识
     */
    private String reqNo;

    /**
     * 存证的key
     */
    private String key;


    /**
     * 存证value
     */
    private String value;


    /**
     * 存证更新的标志
     *  000表示覆盖更新
     *  999表示不做更新
     */
    private String flag;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReqNo() {
        return reqNo;
    }

    public void setReqNo(String reqNo) {
        this.reqNo = reqNo;
    }

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

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
