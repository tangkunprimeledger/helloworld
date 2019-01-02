package com.higgs.trust.evmcontract.facade;

import com.higgs.trust.evmcontract.core.Transaction;
import com.higgs.trust.evmcontract.facade.exception.ContractContextException;
import com.higgs.trust.evmcontract.util.ByteArraySet;
import com.higgs.trust.evmcontract.vm.VM;
import com.higgs.trust.evmcontract.vm.program.Program;
import com.higgs.trust.evmcontract.vm.program.ProgramResult;
import com.higgs.trust.evmcontract.vm.program.invoke.ProgramInvoke;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Objects;

/**
 * @author Chen Jiawei
 * @date 2018-11-30
 */
public class CustomerContractQueryingExecutor extends BaseContractExecutor {
    CustomerContractQueryingExecutor(ContractExecutionContext contractExecutionContext) {
        super(contractExecutionContext);
    }


    @Override
    protected void checkSenderAddress() {
    }

    @Override
    protected void checkNonce() {
    }

    @Override
    protected void checkBalance() {
    }

    @Override
    protected void checkReceiverAccount() {
        checkReceiverAddress();

        if (Objects.isNull(contractRepository.getAccountState(receiverAddress))) {
            throw new ContractContextException("Account with receiver address does not exist");
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
        ProgramResult programResult = new ProgramResult();
        ByteArraySet touchedAccountAddresses = new ByteArraySet();
        try {
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

        return buildContractExecutionResult(programResult, touchedAccountAddresses);
    }

    @Override
    protected byte[] formatMessageData() {
        return data;
    }
}

