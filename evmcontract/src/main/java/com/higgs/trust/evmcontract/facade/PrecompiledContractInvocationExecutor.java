package com.higgs.trust.evmcontract.facade;

import com.higgs.trust.evmcontract.facade.exception.ContractContextException;
import com.higgs.trust.evmcontract.facade.exception.ContractExecutionException;
import com.higgs.trust.evmcontract.util.ByteArraySet;
import com.higgs.trust.evmcontract.vm.DataWord;
import com.higgs.trust.evmcontract.vm.PrecompiledContracts;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Objects;

/**
 * @author Chen Jiawei
 * @date 2018-11-15
 */
public class PrecompiledContractInvocationExecutor extends BaseContractExecutor {
    PrecompiledContractInvocationExecutor(ContractExecutionContext contractExecutionContext) {
        super(contractExecutionContext);
    }


    @Override
    protected void checkValue() {
        if (Objects.nonNull(value)) {
            throw new ContractContextException("Value for precompiled contract must be null");
        }
    }

    @Override
    protected void checkBalance() {

    }

    @Override
    protected void checkReceiverAddress() {
        super.checkReceiverAddress();

        PrecompiledContracts.PrecompiledContract precompiledContract = PrecompiledContracts.getContractForAddress(
                new DataWord(receiverAddress), systemProperties.getBlockchainConfig());
        if (Objects.isNull(precompiledContract)) {
            throw new ContractContextException("Contract is not a precompiled one");
        }
    }

    @Override
    protected void checkReceiverAccount() {

    }

    @Override
    protected void checkCode() {

    }


    @Override
    protected ContractExecutionResult executeContract() {
        transactionRepository.increaseNonce(senderAddress);


        RuntimeException exception = null;
        byte[] result = null;
        ByteArraySet touchedAccountAddresses = new ByteArraySet();
        try {
            touchedAccountAddresses.add(receiverAddress);

            PrecompiledContracts.PrecompiledContract precompiledContract = PrecompiledContracts.getContractForAddress(
                    new DataWord(receiverAddress), systemProperties.getBlockchainConfig());
            Pair<Boolean, byte[]> out = precompiledContract.execute(data);
            if (!out.getLeft()) {
                exception = new RuntimeException("Precompiled contract fails");
            } else {
                result = out.getRight();
            }

            contractRepository.commit();
        } catch (Throwable e) {
            exception = new RuntimeException(e.getMessage(), e);
            touchedAccountAddresses.remove(receiverAddress);
            contractRepository.rollback();
        }


        transactionRepository.commit();


        ContractExecutionResult contractExecutionResult = new ContractExecutionResult();
        if (exception != null) {
            contractExecutionResult.setErrorMessage(exception.getMessage());
            contractExecutionResult.setException(
                    new ContractExecutionException("Exception happens when transferring asset", exception));
        }
        contractExecutionResult.getTouchedAccountAddresses().addAll(touchedAccountAddresses);
        contractExecutionResult.setResult(result);
        contractExecutionResult.setTransactionHash(transactionHash);
        contractExecutionResult.setValue(value);
        contractExecutionResult.setStateRoot(blockRepository.getRoot());

        return contractExecutionResult;
    }
}
