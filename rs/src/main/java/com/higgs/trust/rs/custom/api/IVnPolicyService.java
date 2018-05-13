package com.higgs.trust.rs.custom.api;


/**
 * Created by young001 on 2017/6/18.
 */
public interface IVnPolicyService {

    /**
     * 查找交易的policy
     *
     * @return the update cert policy id
     */
    public String getTransactionPolicyId();

    /**
     * 查找开户的policy
     *
     * @return the update cert policy id
     */
    public String getOpenAccountPolicyId();

    /**
     * 查找发行policy
     *
     * @return the update cert policy id
     */
    public String getIssueCoinPolicyId();

    /*
     * @desc 查找异步存证下发的的policy
     * @author WangQuanzhou
     * @date 2018/3/8 20:01
     */
    public String getAsyncSendIdentityPolicyId();

}
