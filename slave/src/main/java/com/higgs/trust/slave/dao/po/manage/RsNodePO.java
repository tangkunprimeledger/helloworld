package com.higgs.trust.slave.dao.po.manage;import com.higgs.trust.slave.dao.po.BaseEntity;import java.util.Date;public class RsNodePO extends BaseEntity<RsNodePO> {    private static final long serialVersionUID = 1L;    /**     * rs ID     */    private String rsId;    /**     * description about rs     */    private String desc;    /**     * status     */    private String status;    /**     * create time     */    private Date createTime;    public String getRsId() {        return this.rsId;    }    public void setRsId(String rsId) {        this.rsId = rsId;    }    public String getStatus() {        return this.status;    }    public void setStatus(String status) {        this.status = status;    }    public String getDesc() {        return this.desc;    }    public void setDesc(String desc) {        this.desc = desc;    }    public Date getCreateTime() {        return this.createTime;    }    public void setCreateTime(Date createTime) {        this.createTime = createTime;    }    @Override public String toString() {        StringBuilder builder = new StringBuilder();        builder.append("RsNodePO [");        builder.append("rsId=");        builder.append(rsId);        builder.append(", status=");        builder.append(status);        builder.append(", desc=");        builder.append(desc);        builder.append(", createTime=");        builder.append(createTime);        builder.append("]");        return builder.toString();    }}