package com.higgs.trust.rs.custom.api.vo;

/*
 * @desc 通过地址进行交易查询 单个Response对象
 * @author WangQuanzhou
 * @date 2018/2/11 10:13
 */
public class TransAddrVO  extends BaseVO{


    /**
     * 交易随机数
     */
    private String randomNum;

    /**
     * 币种
     */
    private String currency;

    /**
     * 交易金额
     */
    private String amount;

    /**
     * 区块高度
     */
    private String blockHeight;

    /**
     * 交易更新时间
     */
    private String updatedTime;

    /**
     * 交易费用
     */
    private String fee;

    /**
     * 转出地址
     */
    private String fromAddr;

    /**
     * 备注信息
     */
    private String data;

    /**
     * 交易状态
     */
    private String status;

    /**
     * 转入地址
     */
    private String toAddr;

    /**
     * 交易id
     */
    private String txId;

    /**
     * 交易创建时间
     */
    private String txCreatedTime;

    public String getRandomNum() {
        return randomNum;
    }

    public void setRandomNum(String randomNum) {
        this.randomNum = randomNum;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(String blockHeight) {
        this.blockHeight = blockHeight;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getFromAddr() {
        return fromAddr;
    }

    public void setFromAddr(String fromAddr) {
        this.fromAddr = fromAddr;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getToAddr() {
        return toAddr;
    }

    public void setToAddr(String toAddr) {
        this.toAddr = toAddr;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getTxCreatedTime() {
        return txCreatedTime;
    }

    public void setTxCreatedTime(String txCreatedTime) {
        this.txCreatedTime = txCreatedTime;
    }
}
