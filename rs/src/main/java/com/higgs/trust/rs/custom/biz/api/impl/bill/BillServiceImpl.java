package com.higgs.trust.rs.custom.biz.api.impl.bill;

import com.higgs.trust.rs.custom.api.bill.BillService;
import com.higgs.trust.rs.custom.model.RespData;
import com.higgs.trust.rs.custom.vo.BillCreateVO;
import com.higgs.trust.rs.custom.vo.BillTransferVO;
import org.springframework.stereotype.Service;

/**
 * bill service impl
 *
 * @author lingchao
 * @create 2018年05月13日15:24
 */
@Service
public class BillServiceImpl implements BillService{
    /**
     * 创建票据方法
     *
     * @param billCreateVO
     * @return
     */
    @Override
    public RespData<?> create(BillCreateVO billCreateVO){
        //初步幂等校验

        //请求入库

        //identity 是否存在

        //组装UTXO,CoreTransaction,签名，下发
        return new RespData<>();
    }


    /**
     * 票据转移
     *
     * @param billTransferVO
     * @return
     */

    @Override
    public RespData<?> transfer(BillTransferVO billTransferVO){
        return new RespData<>();
    }
}
