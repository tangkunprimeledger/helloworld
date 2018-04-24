package com.higgs.trust.slave.core.service.version;

import com.higgs.trust.slave.api.enums.VersionEnum;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author WangQuanzhou
 * @desc this class maintain a map which store the relationship between version and exact processor
 * @date 2018/3/28 17:59
 */
@Component public class TxProcessorHolder {
    private Map<VersionEnum, TransactionProcessor> versionProcessorMap = new HashMap<>();

    // register processor
    public void registVerisonProcessor(VersionEnum version, TransactionProcessor processor) {
        versionProcessorMap.put(version, processor);
    }

    // get processor
    public TransactionProcessor getProcessor(VersionEnum version) {
        return versionProcessorMap.get(version);
    }
}
