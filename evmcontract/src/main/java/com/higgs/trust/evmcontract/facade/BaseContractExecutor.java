package com.higgs.trust.evmcontract.facade;

import com.higgs.trust.evmcontract.config.SystemProperties;
import com.higgs.trust.evmcontract.core.Repository;
import com.higgs.trust.evmcontract.db.BlockStore;
import com.higgs.trust.evmcontract.facade.constant.Constant;
import com.higgs.trust.evmcontract.facade.exception.ContractContextException;
import com.higgs.trust.evmcontract.facade.exception.ContractExecutionException;
import com.higgs.trust.evmcontract.facade.util.ContractUtil;
import com.higgs.trust.evmcontract.solidity.Abi;
import com.higgs.trust.evmcontract.util.ByteArraySet;
import com.higgs.trust.evmcontract.util.FastByteComparisons;
import com.higgs.trust.evmcontract.vm.program.ProgramResult;
import com.higgs.trust.evmcontract.vm.program.invoke.ProgramInvoke;
import com.higgs.trust.evmcontract.vm.program.invoke.ProgramInvokeImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Base executor encapsulating common operations or data for contract execution.
 *
 * @author Chen Jiawei
 * @date 2018-11-15
 */
@Slf4j
public abstract class BaseContractExecutor implements Executor<ContractExecutionResult> {
    private static final int ADDRESS_LENGTH = 20;
    private static final int BLOCK_HASH_LENGTH = 32;
    private static final String ERROR_SIGNATURE = "08c379a0";


    /**
     * Contract context.
     */
    private final ContractExecutionContext contractExecutionContext;

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
    protected byte[] receiverAddress;
    protected final byte[] gasPrice;
    private final byte[] parentHash;
    private final byte[] minerAddress;
    protected final long timestamp;
    protected final long number;
    private final byte[] difficulty;
    private final byte[] gasLimitBlock;
    private final BlockStore blockStore;
    protected final SystemProperties systemProperties;


    /**
     * Creates an executor according to the specified context.
     *
     * @param contractExecutionContext null is not allowed, and its fields must be prepared completely by invoker
     */
    BaseContractExecutor(ContractExecutionContext contractExecutionContext) {
        this.contractExecutionContext = contractExecutionContext;
        checkContractExecutionContext();
        log.info("Starts to create executor: " + contractExecutionContext.toString());

        // The block-level snapshot will be used to generate global state
        // root after the contract is executed. This provide support for
        // querying global state at the transaction level.
        this.blockRepository = contractExecutionContext.getBlockRepository();
        checkBlockRepository();
        this.transactionRepository = blockRepository.startTracking();
        checkTransactionRepository();
        this.contractRepository = transactionRepository.startTracking();
        checkContractRepository();

        transactionHash = contractExecutionContext.getTransactionHash();
        data = contractExecutionContext.getData();
        gasLimit = contractExecutionContext.getGasLimit();
        // In this project, sender is not required to be in repository,
        // because accounts exist in the solidity code, not on this
        // platform.
        senderAddress = contractExecutionContext.getSenderAddress();
        // Nonce is used for avoiding replay attack before. In this
        // project, it is responsibility of trust business logic layer.
        // But nonce is reserved for simple processing and possible
        // rollback in future.
        nonce = calculateNonce();
        // For this project, value is always be zero.
        value = contractExecutionContext.getValue();
        // For this project, receiver address is always prepared outside,
        // including contract creation.
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

    private void checkContractExecutionContext() {
        if (Objects.isNull(contractExecutionContext)) {
            throw new ContractContextException("Context cannot be null");
        }
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

    protected byte[] calculateNonce() {
        checkSenderAddress();
        return transactionRepository.getNonce(senderAddress).toByteArray();
    }

    protected void checkSenderAddress() {
        if (ArrayUtils.isEmpty(senderAddress)) {
            throw new ContractContextException("Sender address cannot be empty");
        }

        if (senderAddress.length != ADDRESS_LENGTH) {
            throw new ContractContextException(
                    String.format("Sender address %s is not of %d bytes",
                            Hex.toHexString(senderAddress), ADDRESS_LENGTH));
        }
    }


    /**
     * For caller, once a {@link ContractContextException} instance is
     * captured, the transaction should be discarded from the block.
     */
    @Override
    public ContractExecutionResult execute() {
        check();

        log.info("Starts to execute contract: " + contractExecutionContext.toString());
        long startTime = System.currentTimeMillis();
        ContractExecutionResult contractExecutionResult = executeContract();
        contractExecutionResult.setTimeCost(System.currentTimeMillis() - startTime);

        return contractExecutionResult;
    }

    /**
     * Check environment in which contract is executed. Throws exception
     * if any parameter is illegal or inappropriate.
     */
    private void check() {
        checkContractExecutionContext();

        checkBlockRepository();
        checkTransactionRepository();
        checkContractRepository();

        checkContext();
    }

    private void checkContext() {
        checkData();
        checkGasLimit();
        checkSenderAddress();
        checkNonce();
        checkValue();
        checkBalance();
        checkReceiverAddress();
        checkReceiverAccount();
        checkCode();
        checkMinerAddress();
        checkParentHash();
    }

    protected void checkData() {
        if (ArrayUtils.isEmpty(data)) {
            throw new ContractContextException("Payload for contract cannot be empty");
        }

        if (ArrayUtils.getLength(data) > Constant.TRANSACTION_DATA_SIZE_LIMIT) {
            throw new ContractContextException(
                    String.format("Payload size exceed the limitation: %d bytes",
                            Constant.TRANSACTION_DATA_SIZE_LIMIT));
        }
    }

    private void checkGasLimit() {
        if (ArrayUtils.isEmpty(gasLimit)) {
            throw new ContractContextException("Gas limit for contract cannot be empty");
        }

        if (ContractUtil.moreThan(gasLimit, Constant.TRANSACTION_GAS_LIMIT)) {
            throw new ContractContextException(
                    String.format("Gas limit for contract exceed the limitation: %d",
                            ContractUtil.toBigInteger(Constant.TRANSACTION_GAS_LIMIT)));
        }
    }

    protected void checkNonce() {
        if (ArrayUtils.isEmpty(nonce)) {
            throw new ContractContextException("Nonce cannot be empty");
        }

        checkSenderAddress();
        byte[] nonceInRepository = transactionRepository.getNonce(senderAddress).toByteArray();
        if (!FastByteComparisons.equal(nonce, nonceInRepository)) {
            throw new ContractContextException(
                    String.format("Nonce %s is not same as %s in repository",
                            Hex.toHexString(nonce), Hex.toHexString(nonceInRepository)));
        }
    }

    protected void checkValue() {
        if (Objects.isNull(value)) {
            throw new ContractContextException("Value cannot be null");
        }
    }

    protected void checkBalance() {
        checkValue();
        BigInteger currentValue = ContractUtil.toBigInteger(value);

        checkSenderAddress();
        BigInteger balanceInRepository = transactionRepository.getBalance(senderAddress);

        if (ContractUtil.moreThan(currentValue, balanceInRepository)) {
            throw new ContractContextException("Balance of sender in repository is not enough to pay for value");
        }
    }

    protected void checkReceiverAddress() {
        if (ArrayUtils.isEmpty(receiverAddress)) {
            throw new ContractContextException("Receiver address cannot be empty");
        }

        if (receiverAddress.length != ADDRESS_LENGTH) {
            throw new ContractContextException(
                    String.format("Receiver address %s is not of %d bytes",
                            Hex.toHexString(receiverAddress), ADDRESS_LENGTH));
        }
    }

    protected void checkReceiverAccount() {
        checkReceiverAddress();

        if (Objects.nonNull(contractRepository.getAccountState(receiverAddress))) {
            throw new ContractContextException(
                    String.format("Account with new address %s exists", Hex.toHexString(receiverAddress)));
        }
    }

    protected void checkCode() {
        checkReceiverAddress();

        byte[] code = transactionRepository.getCode(receiverAddress);
        if (ArrayUtils.isNotEmpty(code)) {
            throw new ContractContextException(
                    String.format("Contract code is not empty, receiverAddress=%s, code=%s",
                            Hex.toHexString(receiverAddress), Hex.toHexString(code)));
        }
    }

    private void checkMinerAddress() {
        if (minerAddress.length != ADDRESS_LENGTH) {
            throw new ContractContextException(
                    String.format("Miner address %s is not of %d bytes", Hex.toHexString(minerAddress), ADDRESS_LENGTH));
        }
    }

    private void checkParentHash() {
        if (parentHash.length != BLOCK_HASH_LENGTH) {
            throw new ContractContextException(
                    String.format("Parent hash %s is not of %d bytes", Hex.toHexString(parentHash), BLOCK_HASH_LENGTH));
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

            String errorMessage;
            try {
                errorMessage = parseRevertInformation(programResult.getHReturn());
                contractExecutionResult.setErrorMessage(errorMessage);
            } catch (ContractExecutionException e) {
                contractExecutionResult.setException(e);
                contractExecutionResult.setErrorMessage("REVERT opcode executed, but: " + e.getMessage());
            }
        }

        contractExecutionResult.getTouchedAccountAddresses().addAll(touchedAccountAddresses);
        contractExecutionResult.setTransactionHash(transactionHash);
        contractExecutionResult.setValue(value);
        contractExecutionResult.setLogInfoList(programResult.getLogInfoList());
        contractExecutionResult.setResult(programResult.getHReturn());
        contractExecutionResult.setDeleteAccounts(programResult.getDeleteAccounts());
        contractExecutionResult.setInternalTransactions(programResult.getInternalTransactions());
        contractExecutionResult.setReceiverAddress(receiverAddress);
        contractExecutionResult.setStateRoot(blockRepository.getRoot());

        return contractExecutionResult;
    }

    private String parseRevertInformation(byte[] hReturn) {
        // In solidity code, revert has no message.
        if (ArrayUtils.isEmpty(hReturn)) {
            return "";
        }

        // According to solidity design, revert message must start
        // with "08c379a0", which is the abi coding for method
        // signature "(string) error(string)".
        if (!Hex.toHexString(hReturn).startsWith(ERROR_SIGNATURE)) {
            throw new ContractExecutionException("return is not a standard revert message");
        }

        byte[] abiData = Arrays.copyOfRange(hReturn, ERROR_SIGNATURE.length() / 2, hReturn.length);
        Abi.Function function = Abi.Function.of("(string) error(string)");
        List<?> list = function.decodeResult(abiData, false);

        if (list.size() != 1 || !(list.get(0) instanceof String)) {
            throw new ContractExecutionException("parsing revert message fail");
        }

        return (String) list.get(0);
    }
}
