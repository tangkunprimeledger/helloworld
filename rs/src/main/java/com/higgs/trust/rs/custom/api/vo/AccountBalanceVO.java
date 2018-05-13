package com.higgs.trust.rs.custom.api.vo;

/*  
 * @desc 余额查询 Response对象
 * @author WangQuanzhou
 * @date 2018/2/9 15:56
 */  
public class AccountBalanceVO extends BaseVO{

    /**
     * 查询地址
     */
    private String addr;

    /**
     *  查询币种
     */
    private String currency;

    /**
     *  余额
     */
    private String amount;

    /**
     * 当前查询操作的时间
     */
    private String time;

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
