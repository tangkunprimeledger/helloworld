package com.higgs.trust.consensus.p2pvalid.dao.po;import java.util.Date;public class SendCommandPO extends BaseEntity<SendCommandPO> {    private static final long serialVersionUID = 1L;            private Long id;//   id    private String messageDigest;//   message digest    private String validCommand;//   valid command    private String nodeName;//   node name    private String commandSign;//   command sign    private String commandClass;//   command class    private Integer ackNodeNum;//   num of  ack node    private Integer gcThreshold;//   threshold to gc    private Integer status;//   0-add to send queue，1-add to gc queue    private Integer retrySendNum;//   count of retry    private Date createTime;//   create time    private Date updateTime;//   update time    public Long getId() {        return this.id;    }    public void setId(Long id) {        this.id = id;    }    public String getMessageDigest() {        return this.messageDigest;    }    public void setMessageDigest(String messageDigest) {        this.messageDigest = messageDigest;    }    public String getValidCommand() {        return this.validCommand;    }    public void setValidCommand(String validCommand) {        this.validCommand = validCommand;    }    public String getNodeName() {        return this.nodeName;    }    public void setNodeName(String nodeName) {        this.nodeName = nodeName;    }    public String getCommandSign() {        return this.commandSign;    }    public void setCommandSign(String commandSign) {        this.commandSign = commandSign;    }    public String getCommandClass() {        return this.commandClass;    }    public void setCommandClass(String commandClass) {        this.commandClass = commandClass;    }    public Integer getAckNodeNum() {        return this.ackNodeNum;    }    public void setAckNodeNum(Integer ackNodeNum) {        this.ackNodeNum = ackNodeNum;    }    public Integer getGcThreshold() {        return this.gcThreshold;    }    public void setGcThreshold(Integer gcThreshold) {        this.gcThreshold = gcThreshold;    }    public Integer getStatus() {        return this.status;    }    public void setStatus(Integer status) {        this.status = status;    }    public Integer getRetrySendNum() {        return this.retrySendNum;    }    public void setRetrySendNum(Integer retrySendNum) {        this.retrySendNum = retrySendNum;    }    public Date getCreateTime() {        return this.createTime;    }    public void setCreateTime(Date createTime) {        this.createTime = createTime;    }    public Date getUpdateTime() {        return this.updateTime;    }    public void setUpdateTime(Date updateTime) {        this.updateTime = updateTime;    }    @Override    public String toString() {        StringBuilder builder = new StringBuilder();        builder.append("SendCommandPO [");           builder.append("id=");        builder.append(id);        builder.append(", messageDigest=");        builder.append(messageDigest);        builder.append(", validCommand=");        builder.append(validCommand);        builder.append(", nodeName=");        builder.append(nodeName);        builder.append(", commandSign=");        builder.append(commandSign);        builder.append(", commandClass=");        builder.append(commandClass);        builder.append(", ackNodeNum=");        builder.append(ackNodeNum);        builder.append(", gcThreshold=");        builder.append(gcThreshold);        builder.append(", status=");        builder.append(status);        builder.append(", retrySendNum=");        builder.append(retrySendNum);        builder.append(", createTime=");        builder.append(createTime);        builder.append(", updateTime=");        builder.append(updateTime);        builder.append("]");        return builder.toString();    }}