package com.higgs.trust.evmcontract.facade;

import com.higgs.trust.evmcontract.config.SystemProperties;
import com.higgs.trust.evmcontract.core.Repository;
import com.higgs.trust.evmcontract.db.BlockStore;
import com.higgs.trust.evmcontract.facade.constant.Constant;
import com.higgs.trust.evmcontract.facade.exception.ContractContextException;
import com.higgs.trust.evmcontract.facade.exception.ContractExecutionException;
import com.higgs.trust.evmcontract.facade.util.ContractUtil;
import com.higgs.trust.evmcontract.util.ByteArraySet;
import com.higgs.trust.evmcontract.vm.program.ProgramResult;
import com.higgs.trust.evmcontract.vm.program.invoke.ProgramInvoke;
import com.higgs.trust.evmcontract.vm.program.invoke.ProgramInvokeImpl;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Base executor encapsulating common operations or data for contract execution.
 *
 * @author Chen Jiawei
 * @date 2018-11-15
 */
public abstract class BaseContractExecutor implements Executor<ContractExecutionResult> {
    /**
     * Contract context.
     */
    protected final ContractExecutionContext contractExecutionContext;

    /**
     * Block-level snapshot.
     */
    protected final Repository blockRepository;

    /**
     * Transaction-level snapshot.
     */
    protected final Repository transactionRepository;

    /**
     * Contract-level snapshot.
     */
    protected final Repository contractRepository;


    /*** bellow fields come from context ***/
    protected final byte[] transactionHash;
    protected final byte[] data;
    protected final byte[] gasLimit;
    protected final byte[] senderAddress;
    protected final byte[] nonce;
    protected final byte[] value;
    protected final byte[] receiverAddress;
    protected final byte[] gasPrice;
    protected final byte[] parentHash;
    protected final byte[] minerAddress;
    protected final long timestamp;
    protected final long number;
    protected final byte[] difficulty;
    protected final byte[] gasLimitBlock;
    protected final BlockStore blockStore;
    protected final SystemProperties systemProperties;


    /**
     * Executor is created according to context.
     *
     * @param contractExecutionContext null is not allowed
     */
    BaseContractExecutor(ContractExecutionContext contractExecutionContext) {
        this.contractExecutionContext = contractExecutionContext;

        this.blockRepository = contractExecutionContext.getBlockRepository();
        checkBlockRepository();
        this.transactionRepository = blockRepository.startTracking();
        checkTransactionRepository();
        this.contractRepository = transactionRepository.startTracking();
        checkContractRepository();

        transactionHash = contractExecutionContext.getTransactionHash();
        data = contractExecutionContext.getData();
        gasLimit = contractExecutionContext.getGasLimit();
        senderAddress = contractExecutionContext.getSenderAddress();
        nonce = contractExecutionContext.getNonce();
        value = contractExecutionContext.getValue();
        receiverAddress = contractExecutionContext.getReceiverAddress();
        gasPrice = contractExecutionContext.getGasPrice();
        parentHash = contractExecutionContext.getParentHash();
        minerAddress = contractExecutionContext.getMinerAddress();
        timestamp = contractExecutionContext.getTimestamp();
        number = contractExecutionContext.getNumber();
        difficulty = contractExecutionContext.getDifficulty();
        gasLimitBlock = contractExecutionContext.getGasLimitBlock();
        blockStore = contractExecutionContext.getBlockStore();
        systemProperties = contractExecutionContext.getSystemProperties();
    }

    private void checkBlockRepository() {
        if (Objects.isNull(blockRepository)) {
            throw new ContractContextException("Block-level snapshot cannot be null");
        }
    }

    private void checkTransactionRepository() {
        if (Objects.isNull(transactionRepository)) {
            throw new ContractContextException("Transaction-level snapshot cannot be null");
        }
    }

    private void checkContractRepository() {
        if (Objects.isNull(contractRepository)) {
            throw new ContractContextException("Contract-level snapshot cannot be null");
        }
    }


    /**
     * For caller, once a {@link ContractContextException} instance is
     * captured, the transaction should be discarded from the block.
     */
    @Override
    public ContractExecutionResult execute() {
        check();

        long time = System.currentTimeMillis();
        ContractExecutionResult contractExecutionResult = executeContract();
        contractExecutionResult.setTimeCost(System.currentTimeMillis() - time);

        return contractExecutionResult;
    }


    /**
     * Check environment in which contract is executed. Throws exception
     * if any parameter is illegal or inappropriate.
     */
    protected void check() {
        checkBlockRepository();
        checkTransactionRepository();
        checkContractRepository();

        checkContext();
    }

    private void checkContext() {
        if (Objects.isNull(contractExecutionContext)) {
            throw new ContractContextException("Context is not provided");
        }

        checkData();
        checkGasLimit();
        checkSenderAddress();
        checkSenderAccount();
        checkNonce();
        checkValue();
        checkBalance();
        checkReceiverAddress();
        checkReceiverAccount();
        checkCode();
    }

    protected void checkData() {
        if (ArrayUtils.isEmpty(data)) {
            throw new ContractContextException("Payload for contract cannot be empty");
        }

        if (ArrayUtils.getLength(data) > Constant.TRANSACTION_DATA_SIZE_LIMIT) {
            throw new ContractContextException(
                    String.format("Payload size exceed the limitation: %d", Constant.TRANSACTION_DATA_SIZE_LIMIT));
        }
    }

    private void checkGasLimit() {
        if (ArrayUtils.isEmpty(gasLimit)) {
            throw new ContractContextException("Gas limit for contract cannot be empty");
        }

        if (ContractUtil.moreThan(gasLimit, Constant.TRANSACTION_GAS_LIMIT)) {
            throw new ContractContextException(String.format("Gas limit for contract exceed the limitation: %d",
                    ContractUtil.toBigInteger(Constant.TRANSACTION_GAS_LIMIT)));
        }
    }

    private void checkSenderAddress() {
        if (ArrayUtils.isEmpty(senderAddress)) {
            throw new ContractContextException("Sender address cannot be empty");
        }
    }

    protected void checkSenderAccount() {
        checkSenderAddress();

        if (Objects.isNull(contractRepository.getAccountState(senderAddress))) {
            throw new ContractContextException("Sender account does not exist");
        }
    }

    protected void checkNonce() {
        if (ArrayUtils.isEmpty(nonce)) {
            throw new ContractContextException("Nonce cannot be empty");
        }
        checkSenderAddress();

        BigInteger currentNonce = ContractUtil.toBigInteger(nonce);
        BigInteger nonceInRepository = transactionRepository.getNonce(senderAddress);
        if (ContractUtil.notEqual(currentNonce, nonceInRepository)) {
            throw new ContractContextException("Nonce is incorrect");
        }
    }

    protected void checkValue() {
        if (Objects.isNull(value)) {
            throw new ContractContextException("Value cannot be null");
        }
    }

    protected void checkBalance() {
        checkValue();
        checkSenderAddress();

        BigInteger currentValue = ContractUtil.toBigInteger(value);
        BigInteger balanceInRepository = transactionRepository.getBalance(senderAddress);
        if (ContractUtil.moreThan(currentValue, balanceInRepository)) {
            throw new ContractContextException("Balance of sender in repository is not enough to pay for value");
        }
    }

    protected void checkReceiverAddress() {
        if (ArrayUtils.isEmpty(receiverAddress)) {
            throw new ContractContextException("Receiver address cannot be empty");
        }
    }

    protected void checkReceiverAccount() {
        checkReceiverAddress();

        if (Objects.nonNull(contractRepository.getAccountState(receiverAddress))) {
            throw new ContractContextException("Account with new address exists");
        }
    }

    protected void checkCode() {
        checkReceiverAddress();

        byte[] code = transactionRepository.getCode(receiverAddress);
        if (ArrayUtils.isNotEmpty(code)) {
            throw new ContractContextException("Contract code is not empty");
        }
    }


    /**
     * Starts to execute contract.
     *
     * @return result of contract execution
     */
    protected abstract ContractExecutionResult executeContract();

    protected void transferValue() {
        BigInteger transferValue = ContractUtil.toBigInteger(value);
        contractRepository.addBalance(senderAddress, transferValue.negate());
        contractRepository.addBalance(receiverAddress, transferValue);
    }

    protected ProgramInvoke buildProgramInvoke() {
        // This is the sender of original transaction, never a contract.
        byte[] initialCaller = senderAddress;
        // This is the address of the account that is directly responsible for this execution.
        byte[] directCaller = senderAddress;
        byte[] receiverBalance = contractRepository.getBalance(receiverAddress).toByteArray();
        byte[] messageData = formatMessageData();

        return new ProgramInvokeImpl(receiverAddress, initialCaller, directCaller, receiverBalance, gasPrice, gasLimit,
                value, messageData, parentHash, minerAddress, timestamp, number, difficulty, gasLimitBlock,
                contractRepository, blockStore);
    }

    protected byte[] formatMessageData() {
        return new byte[0];
    }

    protected void processProgramResult(final ProgramResult programResult,
                                        final ByteArraySet touchedAccountAddresses) {
        if (programResult.getException() != null || programResult.isRevert()) {
            programResult.getDeleteAccounts().clear();
            programResult.getLogInfoList().clear();
            touchedAccountAddresses.remove(receiverAddress);
            contractRepository.rollback();
        } else {
            touchedAccountAddresses.addAll(programResult.getTouchedAccounts());
            contractRepository.commit();
        }
    }

    protected ContractExecutionResult buildContractExecutionResult(final ProgramResult programResult,
                                                                   final ByteArraySet touchedAccountAddresses) {
        ContractExecutionResult contractExecutionResult = new ContractExecutionResult();

        if (programResult.getException() != null) {
            contractExecutionResult.setErrorMessage(programResult.getException().getMessage());
            contractExecutionResult.setException(
                    new ContractExecutionException("Exception happens when contract is executed", programResult.getException()));
        }

        if (programResult.isRevert()) {
            contractExecutionResult.setRevert(true);
            contractExecutionResult.setErrorMessage("REVERT opcode executed");
        }

        contractExecutionResult.getTouchedAccountAddresses().addAll(touchedAccountAddresses);
        contractExecutionResult.setTransactionHash(transactionHash);
        contractExecutionResult.setValue(value);
        contractExecutionResult.setLogInfoList(programResult.getLogInfoList());
        contractExecutionResult.setResult(programResult.getHReturn());
        contractExecutionResult.setDeleteAccounts(programResult.getDeleteAccounts());
        contractExecutionResult.setInternalTransactions(programResult.getInternalTransactions());
        contractExecutionResult.setStateRoot(blockRepository.getRoot());

        return contractExecutionResult;
    }
}
