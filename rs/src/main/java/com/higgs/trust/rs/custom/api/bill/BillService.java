package com.higgs.trust.rs.custom.api.bill;

import com.higgs.trust.rs.custom.model.RespData;
import com.higgs.trust.rs.custom.vo.BillCreateVO;
import com.higgs.trust.rs.custom.vo.BillTransferVO;

/**
 * @author lingchao
 * @create 2018年05月13日15:24
 */
public interface BillService {
    /**
     * 创建票据方法
     *
     * @param billCreateVO
     * @return
     */
    RespData<?> create(BillCreateVO billCreateVO);


    /**
     * 票据转移
     *
     * @param billTransferVO
     * @return
     */
    RespData<?> transfer(BillTransferVO billTransferVO);
}
