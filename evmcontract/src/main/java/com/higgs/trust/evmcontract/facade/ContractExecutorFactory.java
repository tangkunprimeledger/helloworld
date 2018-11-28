package com.higgs.trust.evmcontract.facade;

import com.higgs.trust.evmcontract.facade.exception.ContractContextException;

import java.util.Objects;

/**
 * A utility class producing executor for contract.
 *
 * @author Chen Jiawei
 * @date 2018-11-15
 */
public final class ContractExecutorFactory
        implements ExecutorFactory<ContractExecutionContext, ContractExecutionResult> {
    @Override
    public BaseContractExecutor createExecutor(ContractExecutionContext contractExecutionContext) {
        if (Objects.isNull(contractExecutionContext)) {
            throw new ContractContextException("An context is necessary for executor creation");
        }

        ContractTypeEnum contractType = contractExecutionContext.getContractType();
        if (Objects.isNull(contractType)) {
            throw new ContractContextException("Contract type shall be given for executor creation");
        }

        switch (contractType) {
            case CONTRACT_CREATION:
                return new ContractCreationExecutor(contractExecutionContext);
            case CUSTOMER_CONTRACT_INVOCATION:
                return new CustomerContractInvocationExecutor(contractExecutionContext);
            case PRECOMPILED_CONTRACT_INVOCATION:
                return new PrecompiledContractInvocationExecutor(contractExecutionContext);
            case ASSET_TRANSFER:
                return new AssetTransferExecutor(contractExecutionContext);
            default:
                throw new ContractContextException("Unknown contract type");
        }
    }
}
