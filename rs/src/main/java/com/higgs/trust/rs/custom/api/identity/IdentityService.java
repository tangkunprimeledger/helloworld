package com.higgs.trust.rs.custom.api.identity;

import com.higgs.trust.rs.custom.model.RespData;
import com.higgs.trust.rs.custom.model.bo.BankChainRequest;
import com.higgs.trust.rs.custom.model.bo.identity.IdentityRequest;

/*
 * @desc 存证相关service
 * @author WangQuanzhou
 * @date 2018/3/2 19:47
 */
public interface IdentityService {

    /*
     * @desc 接收存证请求
     * @param  IdentityRequest
     * @return
     */
    public RespData acceptRequest(IdentityRequest identityRequest);

    /*
     * @desc 根据key查询对应的value
     * @param String
     * @return   IdentityPO
     */
    public RespData queryIdentityByKey(String key);

    /*
     * @desc 根据reqNo查询对应的value
     * @param String
     * @return   IdentityPO
     */
    public RespData queryIdentityByReqNo(String reqNo);

    /*
     * @desc 异步下发存证业务数据
     * @param   BankChainRequest
     * @return
     */
    public void asyncSendIdentity(BankChainRequest bankChainRequest);
}
