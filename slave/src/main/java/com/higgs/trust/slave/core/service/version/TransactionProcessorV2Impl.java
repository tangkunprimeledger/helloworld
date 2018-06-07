package com.higgs.trust.slave.core.service.version;

import com.higgs.trust.slave.api.enums.TxProcessTypeEnum;
import com.higgs.trust.slave.api.enums.VersionEnum;
import com.higgs.trust.slave.model.bo.context.TransactionData;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author WangQuanzhou
 * @desc transaction processor V2
 * @date 2018/3/28 18:01
 */
@Component public class TransactionProcessorV2Impl implements TransactionProcessor, InitializingBean {

    @Autowired TxProcessorHolder txProcessorHolder;

    @Override public void afterPropertiesSet() throws Exception {
        txProcessorHolder.registVerisonProcessor(VersionEnum.V2, this);
    }

    @Override public void process(TransactionData transactionData) {
    }
}
