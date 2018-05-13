package com.higgs.trust.rs.custom.biz.api.impl.bill;

import com.higgs.trust.rs.custom.model.RespData;
import org.springframework.stereotype.Service;

/**
 * BillService helper
 *
 * @author lingchao
 * @create 2018年05月13日16:11
 */
@Service
public class BillServiceHelper {

/*
    public RespData<?> requestIdempotent(String request_id) {
    */
/*    RespData<?> respData = null;
        Transaction transactionPO = transactionDao.queryByTxId(txId);
        if (null != transactionPO) {
            if (StringUtils.equals(transactionPO.getStatus(), TransferStatusEnum.INIT.getCode())) {
                LOGGER.info("txId：{} 的交易请求幂等，交易已接收", txId);
                respData = new RespData(RespCodeEnum.TRANSFER_ACCEPTED);
            } else {
                LOGGER.info("txId：{} 的交易请求幂等，交易发送失败， 交易状态为:{}", txId, transactionPO.getStatus());
                respData = new RespData(RespCodeEnum.TRANSFER_SEND_FAIL);
            }
            TransactionVO transactionVO = CoinChainBOBuilder.buildTransactionVO(txId);
            respData.setData(transactionVO);
        }
        return respData;*//*

    }
*/

}
