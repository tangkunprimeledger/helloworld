package com.higgs.trust.rs.custom.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * @Description:
 * @author: pengdi
 **/
@Getter
@Setter
public class BillTransferVO {
    /**
     * 请求编号 64
     */
    private String requestId;

    /**
     * 业务存证模型 8192
     */
    private String bizModel;

    /**
     * 应收票据编号 64
     */
    private String billId;

    /**
     * 受让持票人 64
     */
    private String nextHolder;
}
