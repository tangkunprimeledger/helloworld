package com.higgs.trust.rs.custom.dao.po.identity;

import com.higgs.trust.rs.custom.dao.po.BasePO;

import java.util.Date;

/*
 * @desc 存证数据对象
 * @author WangQuanzhou
 * @date 2018/3/5 12:02
 */  
public class IdentityPO extends BasePO {

    private static final long serialVersionUID = -1062285205103686367L;
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
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 存证状态
     */
    private String status;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
