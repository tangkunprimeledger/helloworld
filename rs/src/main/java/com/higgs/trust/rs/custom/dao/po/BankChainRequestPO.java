package com.higgs.trust.rs.custom.dao.po;

import java.util.Date;

public class BankChainRequestPO extends BasePO {

    private static final long serialVersionUID = -6584282315308460388L;

    private Long id;//  主键id  自增
    private String reqNo;//   请求唯一标识
    private String bizType;//   请求业务类型  目前只有  STORAGE-存证
    private String status;//   请求状态：INIT-初始态  PROCESSING-处理中   SUCCESS-处理成功
    private String respCode;//   响应码：对应 RespCodeEnum
    private String respMsg;//   响应信息
    private Date createTime;//   创建时间
    private Date updateTime;//   更新时间

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReqNo() {
        return this.reqNo;
    }

    public void setReqNo(String reqNo) {
        this.reqNo = reqNo;
    }

    public String getBizType() {
        return this.bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRespCode() {
        return this.respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getRespMsg() {
        return this.respMsg;
    }

    public void setRespMsg(String respMsg) {
        this.respMsg = respMsg;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CoinchainRequest [");
        builder.append("id=");
        builder.append(id);
        builder.append(", reqNo=");
        builder.append(reqNo);
        builder.append(", bizType=");
        builder.append(bizType);
        builder.append(", status=");
        builder.append(status);
        builder.append(", respCode=");
        builder.append(respCode);
        builder.append(", respMsg=");
        builder.append(respMsg);
        builder.append(", createTime=");
        builder.append(createTime);
        builder.append(", updateTime=");
        builder.append(updateTime);
        builder.append("]");
        return builder.toString();
    }
}


