package com.higgs.trust.evmcontract.facade;

import com.higgs.trust.evmcontract.core.Transaction;
import com.higgs.trust.evmcontract.facade.exception.ContractContextException;
import com.higgs.trust.evmcontract.util.ByteArraySet;
import com.higgs.trust.evmcontract.vm.DataWord;
import com.higgs.trust.evmcontract.vm.VM;
import com.higgs.trust.evmcontract.vm.program.Program;
import com.higgs.trust.evmcontract.vm.program.ProgramResult;
import com.higgs.trust.evmcontract.vm.program.invoke.ProgramInvoke;
import org.apache.commons.lang3.ArrayUtils;
import org.spongycastle.util.encoders.Hex;

import java.util.Objects;

/**
 * @author Chen Jiawei
 * @date 2018-11-15
 */
public class CustomerContractInvocationExecutor extends BaseContractExecutor {
    CustomerContractInvocationExecutor(ContractExecutionContext contractExecutionContext) {
        super(contractExecutionContext);
    }


    @Override
    protected void checkReceiverAccount() {
        checkReceiverAddress();

        if (Objects.isNull(contractRepository.getAccountState(receiverAddress))) {
            throw new ContractContextException(
                    "Account with receiver address does not exist, receiverAddress: " + Hex.toHexString(receiverAddress));
        }
    }

    @Override
    protected void checkCode() {
        checkReceiverAddress();

        byte[] code = transactionRepository.getCode(receiverAddress);
        if (ArrayUtils.isEmpty(code)) {
            throw new ContractContextException("Contract code is empty");
        }
    }


    @Override
    protected ContractExecutionResult executeContract() {
        transactionRepository.increaseNonce(senderAddress);


        ProgramResult programResult = new ProgramResult();
        ByteArraySet touchedAccountAddresses = new ByteArraySet();
        try {
            transferValue();
            touchedAccountAddresses.add(receiverAddress);

            byte[] codeHash = transactionRepository.getCodeHash(receiverAddress);
            byte[] code = transactionRepository.getCode(receiverAddress);
            Transaction transaction = new Transaction(nonce, gasPrice, gasLimit, receiverAddress, value, data);
            transaction.setHash(transactionHash);
            ProgramInvoke programInvoke = buildProgramInvoke();
            VM vm = new VM(systemProperties);
            Program program = new Program(codeHash, code, programInvoke, transaction, systemProperties);
            vm.play(program);
            programResult = program.getResult();

            processProgramResult(programResult, touchedAccountAddresses);
        } catch (Throwable e) {
            touchedAccountAddresses.remove(receiverAddress);
            contractRepository.rollback();
        }


        for (DataWord address : programResult.getDeleteAccounts()) {
            transactionRepository.delete(address.getLast20Bytes());
        }
        transactionRepository.commit();

        return buildContractExecutionResult(programResult, touchedAccountAddresses);
    }

    @Override
    protected byte[] formatMessageData() {
        return data;
    }
}
