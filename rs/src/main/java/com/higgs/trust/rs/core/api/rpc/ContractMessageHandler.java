package com.higgs.trust.rs.core.api.rpc;

import com.higgs.trust.network.NetworkManage;
import com.higgs.trust.rs.common.enums.RsCoreErrorEnum;
import com.higgs.trust.rs.common.exception.RsCoreException;
import com.higgs.trust.rs.core.api.ContractService;
import com.higgs.trust.rs.core.bo.ContractQueryStateV2BO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: lingchao
 * @datetime:2019-01-06 19:16
 **/
@Slf4j
@Service
public class ContractMessageHandler implements InitializingBean {

    @Autowired
    private ContractService contractService;

    @Autowired
    private NetworkManage networkManage;

    List<?> queryState2(ContractQueryStateV2BO contractQueryStateV2BO) {
        if (StringUtils.isBlank(contractQueryStateV2BO.getAddress()) || StringUtils.isBlank(contractQueryStateV2BO.getMethod())) {
            log.error("address :{} or method :{} can not be null!", contractQueryStateV2BO.getAddress(), contractQueryStateV2BO.getMethod());
            throw new RsCoreException(RsCoreErrorEnum.RS_CORE_PARAM_ERROR);
        }
        return contractService.query2(contractQueryStateV2BO.getBlockHeight(), contractQueryStateV2BO.getAddress(), contractQueryStateV2BO.getMethod(), contractQueryStateV2BO.getParameters());
    }

    @Override
    public void afterPropertiesSet() {
        networkManage.registerHandler("contract/queryState2", this::queryState2);
    }
}
