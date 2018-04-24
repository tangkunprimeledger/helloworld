package com.higgs.trust.slave.core.service.datahandler.manage;

import com.higgs.trust.slave.model.bo.manage.Policy;
import com.higgs.trust.slave.model.bo.manage.RegisterPolicy;

/**
 * @author tangfashuang
 * @date 2018/04/17 19:26
 * @desc policy handler interface
 */
public interface PolicyHandler {

    /**
     * get policy by id
     * @param policyId
     * @return
     */
    Policy getPolicy(String policyId);

    /**
     * register policy
     * @param registerPolicy
     */
    void registerPolicy(RegisterPolicy registerPolicy);
}
