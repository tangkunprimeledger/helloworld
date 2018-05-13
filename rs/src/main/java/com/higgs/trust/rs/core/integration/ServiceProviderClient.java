/*
 * Copyright (c) 2013-2017, suimi
 */
package com.higgs.trust.rs.core.integration;

import com.higgs.trust.common.feign.FeignRibbonConstants;
import com.higgs.trust.slave.api.vo.RespData;
import com.higgs.trust.slave.model.bo.Block;
import com.higgs.trust.slave.model.bo.BlockHeader;
import com.higgs.trust.slave.model.bo.CoreTransaction;
import com.higgs.trust.slave.model.bo.SignedTransaction;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("${higgs.trust.prefix}") public interface ServiceProviderClient {
    /**
     * sign transaction
     *
     * @param nodeName
     * @param coreTransaction
     * @return
     */
    @RequestMapping(value = "/signTx", method = RequestMethod.POST) RespData<String> signTx(
        @RequestHeader(FeignRibbonConstants.NODE_NAME) String nodeName, @RequestBody CoreTransaction coreTransaction);

}
