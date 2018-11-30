package com.higgs.trust.slave.core.service.contract;

import com.higgs.trust.common.utils.Profiler;
import com.higgs.trust.evmcontract.facade.compile.ContractInvocation;
import org.spongycastle.util.encoders.Hex;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Chen Jiawei
 * @date 2018-11-29
 */
@Service
public class ContractQuery {
    public List<?> executeQuery(byte[] contractAddress, String methodSignature, Object... args) {
        try {
            Profiler.enter(String.format("query contract at %s", Hex.toHexString(contractAddress)));

            ContractInvocation contractInvocation = new ContractInvocation();
            byte[] data = contractInvocation.getBytecodeForInvokeContract(methodSignature, args);

            //TODO: 添加执行过程

            byte[] result = null;
            return contractInvocation.decodeResult(result);
        } finally {
            Profiler.release();
        }
    }
}
