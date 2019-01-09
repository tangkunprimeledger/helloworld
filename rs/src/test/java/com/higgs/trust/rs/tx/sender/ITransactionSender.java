package com.higgs.trust.rs.tx.sender;

import com.higgs.trust.rs.core.bo.ContractQueryRequestV2;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * @author Chen Jiawei
 * @date 2019-01-03
 */
public interface ITransactionSender {
    @POST("/transaction/post")
    Call<RespData> post(@Body SignedTransaction signedTransaction);

    @POST("/contract/query2")
    Call<RespData> post(@Body ContractQueryRequestV2 contractQueryRequestV2);
}
