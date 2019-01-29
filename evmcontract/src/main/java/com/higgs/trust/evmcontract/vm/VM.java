/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package com.higgs.trust.evmcontract.vm;

import ch.qos.logback.classic.Level;
import com.higgs.trust.evmcontract.config.BlockChainConfig;
import com.higgs.trust.evmcontract.config.SystemProperties;
import com.higgs.trust.evmcontract.db.ContractDetails;
import com.higgs.trust.evmcontract.vm.program.Program;
import com.higgs.trust.evmcontract.vm.program.Stack;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;

import static com.higgs.trust.evmcontract.crypto.HashUtil.sha3;
import static com.higgs.trust.evmcontract.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static com.higgs.trust.evmcontract.util.ByteUtil.toHexString;
import static com.higgs.trust.evmcontract.vm.OpCode.*;

/**
 * The Ethereum Virtual Machine (EVM) is responsible for initialization
 * and executing a transaction on a contract.
 * <p>
 * It is a quasi-Turing-complete machine; the quasi qualification
 * comes from the fact that the computation is intrinsically bounded
 * through a parameter, gas, which limits the total amount of computation done.
 * <p>
 * The EVM is a simple stack-based architecture. The word size of the machine
 * (and thus size of stack item) is 256-bit. This was chosen to facilitate
 * the SHA3-256 hash scheme and  elliptic-curve computations. The memory model
 * is a simple word-addressed byte array. The stack has an unlimited size.
 * The machine also has an independent storage model; this is similar in concept
 * to the memory but rather than a byte array, it is a word-addressable word array.
 * <p>
 * Unlike memory, which is volatile, storage is non volatile and is
 * maintained as part of the system state. All locations in both storage
 * and memory are well-defined initially as zero.
 * <p>
 * The machine does not follow the standard von Neumann architecture.
 * Rather than storing program code in generally-accessible memory or storage,
 * it is stored separately in a virtual ROM interactable only though
 * a specialised instruction.
 * <p>
 * The machine can have exceptional execution for several reasons,
 * including stack underflows and invalid instructions. These unambiguously
 * and validly result in immediate halting of the machine with all state changes
 * left intact. The one piece of exceptional execution that does not leave
 * state changes intact is the out-of-gas (OOG) exception.
 * <p>
 * Here, the machine halts immediately and reports the issue to
 * the execution agent (either the transaction processor or, recursively,
 * the spawning execution environment) and which will deal with it separately.
 *
 * @author Roman Mandeleil
 * @since 01.06.2014
 */
public class VM {


    private static final Logger LOGGER = LoggerFactory.getLogger("VM");

    private static final Logger DUMP_LOGGER = LoggerFactory.getLogger("dump");

    private static final String LOG_STRING = "{}    Op: [{}]  Gas: [{}] Deep: [{}]  Hint: [{}]";

    private static final Map<OpCode, Function<BlockChainConfig, Boolean>> OP_VALIDATORS = new HashMap<OpCode, Function<BlockChainConfig, Boolean>>() {{
        put(DELEGATECALL, (config) -> config.getConstants().hasDelegateCallOpcode());
        put(REVERT, BlockChainConfig::eip206);
        put(RETURNDATACOPY, BlockChainConfig::eip211);
        put(RETURNDATASIZE, BlockChainConfig::eip211);
        put(STATICCALL, BlockChainConfig::eip214);
    }};
    private static BigInteger MAX_SIZE_OF_DATA_WORD = BigInteger.valueOf(32);

    /**max mem size which couldn't be paid for ever
    *used to reduce expensive BigInt arithmetic
    */
    private static BigInteger MAX_MEM_SIZE = BigInteger.valueOf(Integer.MAX_VALUE);
    private static VMHook vmHook;
    private final SystemProperties config;
    /**
     * Keeps track of the number of steps performed in this VM
     */
    private int vmCounter = 0;
    private boolean vmTrace;
    private long dumpBlock;

    public VM(SystemProperties config) {
        ((ch.qos.logback.classic.Logger) LOGGER).setLevel(Level.INFO);
        this.config = config;
        vmTrace = config.vmTrace();
        dumpBlock = config.dumpBlock();
    }

    public static void setVmHook(VMHook vmHook) {
        VM.vmHook = vmHook;
    }

    /**
     * Utility to calculate new total memory size needed for an operation.
     * <br/> Basically just offset + size, unless size is 0, in which case the result is also 0.
     *
     * @param offset starting position of the memory
     * @param size   number of bytes needed
     * @return offset + size, unless size is 0. In that case memNeeded is also 0.
     */
    private static BigInteger memNeeded(DataWord offset, DataWord size) {
        return size.isZero() ? BigInteger.ZERO : offset.value().add(size.value());
    }

    private long calcMemGas(GasCost gasCosts, long oldMemSize, BigInteger newMemSize, long copySize) {
        long gasCost = 0;

        // Avoid overflows
        if (newMemSize.compareTo(MAX_MEM_SIZE) == 1) {
            throw Program.Exception.gasOverflow(newMemSize, MAX_MEM_SIZE);
        }

        // memory gas calc
        long memoryUsage = (newMemSize.longValue() + 31) / 32 * 32;
        if (memoryUsage > oldMemSize) {
            long memWords = (memoryUsage / 32);
            long memWordsOld = (oldMemSize / 32);
            //TODO #POC9 c_quadCoeffDiv = 512, this should be a constant, not magic number
            long memGas = (gasCosts.getMEMORY() * memWords + memWords * memWords / 512)
                    - (gasCosts.getMEMORY() * memWordsOld + memWordsOld * memWordsOld / 512);
            gasCost += memGas;
        }

        if (copySize > 0) {
            long copyGas = gasCosts.getCOPY_GAS() * ((copySize + 31) / 32);
            gasCost += copyGas;
        }
        return gasCost;
    }

    private boolean isDeadAccount(Program program, byte[] addr) {
        return !program.getStorage().isExist(addr) || program.getStorage().getAccountState(addr).isEmpty();
    }

    /**
     * Validates whether operation is allowed
     * with current blockChain config
     *
     * @param op      VM operation
     * @param program Current program
     */
    private void validateOp(final  OpCode op, final Program program) {

        if (op == null) {
            throw Program.Exception.invalidOpCode(program.getCurrentOp());
        }

        if (!(OP_VALIDATORS.containsKey(op))) {
            return;
        }

        BlockChainConfig blockchainConfig = program.getBlockchainConfig();
        if (!OP_VALIDATORS.get(op).apply(blockchainConfig)) {
            throw Program.Exception.invalidOpCode(program.getCurrentOp());
        }
    }

    public void step(Program program) {

        if (vmTrace) {
            program.saveOpTrace();
        }

        try {
            BlockChainConfig blockchainConfig = program.getBlockchainConfig();

            /**
             * 1:check op
             * 2:verify stack size
             * 3:verify stack overflow
             */
            OpCode op = OpCode.code(program.getCurrentOp());
            validateOp(op, program);
            program.verifyStackSize(op.require());
            program.verifyStackOverflow(op.require(), op.ret());

            program.setLastOp(op.val());

            // parameters for logging
            long  memWords = 0;
            long gasBefore = program.getGasLong();

            // Calculate fees and spend gas
            CalculateFeeResult calculateResult = calculateFees(op, program, blockchainConfig);
            long gasCost = calculateResult.gasCost;
            DataWord adjustedCallGas = calculateResult.getAdjustedCallGas();
            program.spendGas(gasCost, op.name());

            // Log debugging line for VM
            if (program.getNumber().intValue() == dumpBlock) {
                this.dumpLine(op, gasBefore, gasCost , memWords, program);
            }
            if (vmHook != null) {
                vmHook.step(program, op);
            }

            //Execute operation
            String hint = stepInto(op, program,  blockchainConfig, adjustedCallGas);

            program.setPreviouslyExecutedOp(op.val());

            if (LOGGER.isDebugEnabled() && !op.isCall()) {
                LOGGER.info(LOG_STRING, String.format("%5s", "[" + program.getPC() + "]"),
                        String.format("%-12s",
                                op.name()), program.getGas().value(),
                        program.getCallDeep(), hint);
            }

            vmCounter++;
        } catch (RuntimeException e) {
            LOGGER.warn("VM halted: [{}]", e);
            program.spendAllGas();
            program.resetFutureRefund();
            program.stop();
            throw e;
        } finally {
            program.fullTrace();
        }
    }

    /**
     * Execute operation
     * @param op current op
     * @param program context program
     * @param blockChainConfig config
     * @param adjustedCallGas adjusted call gas
     */
    public String stepInto(final OpCode op, final Program program,  final BlockChainConfig blockChainConfig,
                         final DataWord adjustedCallGas){

        Stack stack = program.getStack();
        String hint = "";
        switch (op) {
            /**
             * Stop and Arithmetic Operations
             */
            case STOP: {
                program.setHReturn(EMPTY_BYTE_ARRAY);
                program.stop();
            }
            break;
            case ADD: {
                DataWord word1 = program.stackPop();
                DataWord word2 = program.stackPop();

                if (LOGGER.isInfoEnabled()) {
                    hint = word1.value() + " + " + word2.value();
                }

                word1.add(word2);
                program.stackPush(word1);
                program.step();

            }
            break;
            case MUL: {
                DataWord word1 = program.stackPop();
                DataWord word2 = program.stackPop();

                if (LOGGER.isInfoEnabled()) {
                    hint = word1.value() + " * " + word2.value();
                }

                word1.mul(word2);
                program.stackPush(word1);
                program.step();
            }
            break;
            case SUB: {
                DataWord word1 = program.stackPop();
                DataWord word2 = program.stackPop();

                if ( LOGGER.isInfoEnabled()) {
                    hint = word1.value() + " - " + word2.value();
                }

                word1.sub(word2);
                program.stackPush(word1);
                program.step();
            }
            break;
            case DIV: {
                DataWord word1 = program.stackPop();
                DataWord word2 = program.stackPop();

                if ( LOGGER.isInfoEnabled()) {
                    hint = word1.value() + " / " + word2.value();
                }

                word1.div(word2);
                program.stackPush(word1);
                program.step();
            }
            break;
            case SDIV: {
                DataWord word1 = program.stackPop();
                DataWord word2 = program.stackPop();

                if ( LOGGER.isInfoEnabled()) {
                    hint = word1.sValue() + " / " + word2.sValue();
                }

                word1.sDiv(word2);
                program.stackPush(word1);
                program.step();
            }
            break;
            case MOD: {
                DataWord word1 = program.stackPop();
                DataWord word2 = program.stackPop();

                if ( LOGGER.isInfoEnabled()) {
                    hint = word1.value() + " % " + word2.value();
                }

                word1.mod(word2);
                program.stackPush(word1);
                program.step();
            }
            break;
            case SMOD: {
                DataWord word1 = program.stackPop();
                DataWord word2 = program.stackPop();

                if ( LOGGER.isInfoEnabled()) {
                    hint = word1.sValue() + " #% " + word2.sValue();
                }

                word1.sMod(word2);
                program.stackPush(word1);
                program.step();
            }
            break;
            case EXP: {
                DataWord word1 = program.stackPop();
                DataWord word2 = program.stackPop();

                if ( LOGGER.isInfoEnabled()) {
                    hint = word1.value() + " ** " + word2.value();
                }

                word1.exp(word2);
                program.stackPush(word1);
                program.step();
            }
            break;
            case SIGNEXTEND: {
                DataWord word1 = program.stackPop();
                BigInteger k = word1.value();

                if (k.compareTo(MAX_SIZE_OF_DATA_WORD) < 0) {
                    DataWord word2 = program.stackPop();
                    if ( LOGGER.isInfoEnabled()) {
                        hint = word1 + "  " + word2.value();
                    }
                    word2.signExtend(k.byteValue());
                    program.stackPush(word2);
                }
                program.step();
            }
            break;
            case NOT: {
                DataWord word1 = program.stackPop();
                word1.bnot();

                if (LOGGER.isInfoEnabled()) {
                    hint = "" + word1.value();
                }

                program.stackPush(word1);
                program.step();
            }
            break;
            case LT: {
                // TODO: can be improved by not using BigInteger
                DataWord word1 = program.stackPop();
                DataWord word2 = program.stackPop();

                if ( LOGGER.isInfoEnabled()) {
                    hint = word1.value() + " < " + word2.value();
                }

                if (word1.value().compareTo(word2.value()) == -1) {
                    word1.and(DataWord.ZERO);
                    word1.getData()[31] = 1;
                } else {
                    word1.and(DataWord.ZERO);
                }
                program.stackPush(word1);
                program.step();
            }
            break;
            case SLT: {
                // TODO: can be improved by not using BigInteger
                DataWord word1 = program.stackPop();
                DataWord word2 = program.stackPop();

                if ( LOGGER.isInfoEnabled()) {
                    hint = word1.sValue() + " < " + word2.sValue();
                }

                if (word1.sValue().compareTo(word2.sValue()) == -1) {
                    word1.and(DataWord.ZERO);
                    word1.getData()[31] = 1;
                } else {
                    word1.and(DataWord.ZERO);
                }
                program.stackPush(word1);
                program.step();
            }
            break;
            case SGT: {
                // TODO: can be improved by not using BigInteger
                DataWord word1 = program.stackPop();
                DataWord word2 = program.stackPop();

                if ( LOGGER.isInfoEnabled()) {
                    hint = word1.sValue() + " > " + word2.sValue();
                }

                if (word1.sValue().compareTo(word2.sValue()) == 1) {
                    word1.and(DataWord.ZERO);
                    word1.getData()[31] = 1;
                } else {
                    word1.and(DataWord.ZERO);
                }
                program.stackPush(word1);
                program.step();
            }
            break;
            case GT: {
                // TODO: can be improved by not using BigInteger
                DataWord word1 = program.stackPop();
                DataWord word2 = program.stackPop();

                if ( LOGGER.isInfoEnabled()) {
                    hint = word1.value() + " > " + word2.value();
                }

                if (word1.value().compareTo(word2.value()) == 1) {
                    word1.and(DataWord.ZERO);
                    word1.getData()[31] = 1;
                } else {
                    word1.and(DataWord.ZERO);
                }
                program.stackPush(word1);
                program.step();
            }
            break;
            case EQ: {
                DataWord word1 = program.stackPop();
                DataWord word2 = program.stackPop();

                if ( LOGGER.isInfoEnabled()) {
                    hint = word1.value() + " == " + word2.value();
                }

                if (word1.xor(word2).isZero()) {
                    word1.and(DataWord.ZERO);
                    word1.getData()[31] = 1;
                } else {
                    word1.and(DataWord.ZERO);
                }
                program.stackPush(word1);
                program.step();
            }
            break;
            case ISZERO: {
                DataWord word1 = program.stackPop();
                if (word1.isZero()) {
                    word1.getData()[31] = 1;
                } else {
                    word1.and(DataWord.ZERO);
                }

                if ( LOGGER.isInfoEnabled()) {
                    hint = "" + word1.value();
                }

                program.stackPush(word1);
                program.step();
            }
            break;

            /**
             * Bitwise Logic Operations
             */
            case AND: {
                DataWord word1 = program.stackPop();
                DataWord word2 = program.stackPop();

                if ( LOGGER.isInfoEnabled()) {
                    hint = word1.value() + " && " + word2.value();
                }
                //确保栈比特位被正确设置，相同为真且为真
                word1.and(word2);
                program.stackPush(word1);
                program.step();
            }
            break;
            case OR: {
                DataWord word1 = program.stackPop();
                DataWord word2 = program.stackPop();

                if ( LOGGER.isInfoEnabled()) {
                    hint = word1.value() + " || " + word2.value();
                }

                word1.or(word2);
                program.stackPush(word1);
                program.step();
            }
            break;
            case XOR: {
                DataWord word1 = program.stackPop();
                DataWord word2 = program.stackPop();

                if ( LOGGER.isInfoEnabled()) {
                    hint = word1.value() + " ^ " + word2.value();
                }

                word1.xor(word2);
                program.stackPush(word1);
                program.step();
            }
            break;
            case BYTE: {
                DataWord word1 = program.stackPop();
                DataWord word2 = program.stackPop();
                final DataWord result;
                if (word1.value().compareTo(MAX_SIZE_OF_DATA_WORD) == -1) {
                    byte tmp = word2.getData()[word1.intValue()];
                    word2.and(DataWord.ZERO);
                    word2.getData()[31] = tmp;
                    result = word2;
                } else {
                    result = new DataWord();
                }

                if ( LOGGER.isInfoEnabled()) {
                    hint = "" + result.value();
                }

                program.stackPush(result);
                program.step();
            }
            break;
            case ADDMOD: {
                DataWord word1 = program.stackPop();
                DataWord word2 = program.stackPop();
                DataWord word3 = program.stackPop();
                word1.addmod(word2, word3);
                program.stackPush(word1);
                program.step();
            }
            break;
            case MULMOD: {
                DataWord word1 = program.stackPop();
                DataWord word2 = program.stackPop();
                DataWord word3 = program.stackPop();
                word1.mulmod(word2, word3);
                program.stackPush(word1);
                program.step();
            }
            break;

            /**签名方法
             * SHA3
             */
            case SHA3: {
                DataWord memOffsetData = program.stackPop();
                DataWord lengthData = program.stackPop();
                byte[] buffer = program.memoryChunk(memOffsetData.intValueSafe(), lengthData.intValueSafe());

                byte[] encoded = sha3(buffer);
                DataWord word = new DataWord(encoded);

                if ( LOGGER.isInfoEnabled()) {
                    hint = word.toString();
                }

                program.stackPush(word);
                program.step();
            }
            break;

            /**获取owner地址
             * Environmental Information
             */
            case ADDRESS: {
                DataWord address = program.getOwnerAddress();

                if ( LOGGER.isInfoEnabled()) {
                    hint = "address: " + toHexString(address.getLast20Bytes());
                }

                program.stackPush(address);
                program.step();
            }
            break;
            //获取账户余额
            case BALANCE: {
                DataWord address = program.stackPop();
                DataWord balance = program.getBalance(address);

                if ( LOGGER.isInfoEnabled()) {
                    hint = "address: "
                            + toHexString(address.getLast20Bytes())
                            + " balance: " + balance.toString();
                }

                program.stackPush(balance);
                program.step();
            }
            break;
            //获取原始地址
            case ORIGIN: {
                DataWord originAddress = program.getOriginAddress();

                if ( LOGGER.isInfoEnabled()) {
                    hint = "address: " + toHexString(originAddress.getLast20Bytes());
                }

                program.stackPush(originAddress);
                program.step();
            }
            break;
            //获取执行地址
            case CALLER: {

                DataWord callerAddress = program.getCallerAddress();

                if ( LOGGER.isInfoEnabled()) {
                    hint = "address: " + toHexString(callerAddress.getLast20Bytes());
                }

                program.stackPush(callerAddress);
                program.step();
            }
            break;
            case CALLVALUE: {
                DataWord callValue = program.getCallValue();

                if ( LOGGER.isInfoEnabled()) {
                    hint = "value: " + callValue;
                }

                program.stackPush(callValue);
                program.step();
            }
            break;
            case CALLDATALOAD: {
                DataWord dataOffs = program.stackPop();
                DataWord value = program.getDataValue(dataOffs);

                if ( LOGGER.isInfoEnabled()) {
                    hint = "data: " + value;
                }

                program.stackPush(value);
                program.step();
            }
            break;
            case CALLDATASIZE: {
                DataWord dataSize = program.getDataSize();

                if ( LOGGER.isInfoEnabled()) {
                    hint = "size: " + dataSize.value();
                }

                program.stackPush(dataSize);
                program.step();
            }
            break;
            case CALLDATACOPY: {
                DataWord memOffsetData = program.stackPop();
                DataWord dataOffsetData = program.stackPop();
                DataWord lengthData = program.stackPop();

                byte[] msgData = program.getDataCopy(dataOffsetData, lengthData);

                if ( LOGGER.isInfoEnabled()) {
                    hint = "data: " + toHexString(msgData);
                }

                program.memorySave(memOffsetData.intValueSafe(), msgData);
                program.step();
            }
            break;
            case RETURNDATASIZE: {
                DataWord dataSize = program.getReturnDataBufferSize();

                if ( LOGGER.isInfoEnabled()) {
                    hint = "size: " + dataSize.value();
                }

                program.stackPush(dataSize);
                program.step();
            }
            break;
            case RETURNDATACOPY: {
                DataWord memOffsetData = program.stackPop();
                DataWord dataOffsetData = program.stackPop();
                DataWord lengthData = program.stackPop();

                byte[] msgData = program.getReturnDataBufferData(dataOffsetData, lengthData);

                if (msgData == null) {
                    throw new Program.ReturnDataCopyIllegalBoundsException(dataOffsetData, lengthData, program.getReturnDataBufferSize().longValueSafe());
                }

                if ( LOGGER.isInfoEnabled()) {
                    hint = "data: " + toHexString(msgData);
                }

                program.memorySave(memOffsetData.intValueSafe(), msgData);
                program.step();
            }
            break;
            case CODESIZE:
            case EXTCODESIZE: {

                int length;
                if (op == OpCode.CODESIZE) {
                    length = program.getCode().length;
                } else {
                    DataWord address = program.stackPop();
                    length = program.getCodeAt(address).length;
                }
                DataWord codeLength = new DataWord(length);

                if ( LOGGER.isInfoEnabled()) {
                    hint = "size: " + length;
                }

                program.stackPush(codeLength);
                program.step();
            }
            break;
            case CODECOPY:
            case EXTCODECOPY: {

                byte[] fullCode = EMPTY_BYTE_ARRAY;
                if (op == OpCode.CODECOPY) {
                    fullCode = program.getCode();
                }

                if (op == OpCode.EXTCODECOPY) {
                    DataWord address = program.stackPop();
                    fullCode = program.getCodeAt(address);
                }
                //00
                int memOffset = program.stackPop().intValueSafe();
                //00fd转十进制为253，表示合约代码的开始位置为253
                int codeOffset = program.stackPop().intValueSafe();
                //01ef转十进制为495，表示合约代码的长度为495，
                // 运行合约代码的code需要从整个code中拷贝出来
                int lengthData = program.stackPop().intValueSafe();

                int sizeToBeCopied =
                        (long) codeOffset + lengthData > fullCode.length ?
                                (fullCode.length < codeOffset ? 0 : fullCode.length - codeOffset)
                                : lengthData;

                byte[] codeCopy = new byte[lengthData];

                if (codeOffset < fullCode.length) {
                    System.arraycopy(fullCode, codeOffset, codeCopy, 0, sizeToBeCopied);
                }

                if ( LOGGER.isInfoEnabled()) {
                    hint = "code: " + toHexString(codeCopy);
                }
                //将拷贝出来的合约代码存放到memory中，合约运行结束会将codeCopy存储到stroage中
                program.memorySave(memOffset, codeCopy);
                program.step();
            }
            break;
            case GASPRICE: {
                DataWord gasPrice = program.getGasPrice();

                if ( LOGGER.isInfoEnabled()) {
                    hint = "price: " + gasPrice.toString();
                }

                program.stackPush(gasPrice);
                program.step();
            }
            break;

            /**
             * Block Information
             */
            case BLOCKHASH: {

                int blockIndex = program.stackPop().intValueSafe();

                DataWord blockHash = program.getBlockHash(blockIndex);

                if ( LOGGER.isInfoEnabled()) {
                    hint = "blockHash: " + blockHash;
                }

                program.stackPush(blockHash);
                program.step();
            }
            break;
            case COINBASE: {
                DataWord coinbase = program.getCoinbase();

                if ( LOGGER.isInfoEnabled()) {
                    hint = "coinbase: " + toHexString(coinbase.getLast20Bytes());
                }

                program.stackPush(coinbase);
                program.step();
            }
            break;
            case TIMESTAMP: {
                DataWord timestamp = program.getTimestamp();

                if ( LOGGER.isInfoEnabled()) {
                    hint = "timestamp: " + timestamp.value();
                }

                program.stackPush(timestamp);
                program.step();
            }
            break;
            case NUMBER: {
                DataWord number = program.getNumber();

                if ( LOGGER.isInfoEnabled()) {
                    hint = "number: " + number.value();
                }

                program.stackPush(number);
                program.step();
            }
            break;
            case DIFFICULTY: {
                DataWord difficulty = program.getDifficulty();

                if ( LOGGER.isInfoEnabled()) {
                    hint = "difficulty: " + difficulty;
                }

                program.stackPush(difficulty);
                program.step();
            }
            break;
            case GASLIMIT: {
                DataWord gaslimit = program.getGasLimit();

                if ( LOGGER.isInfoEnabled()) {
                    hint = "gaslimit: " + gaslimit;
                }

                program.stackPush(gaslimit);
                program.step();
            }
            break;
            case POP: {
                program.stackPop();
                program.step();
            }
            break;
            case DUP1:
            case DUP2:
            case DUP3:
            case DUP4:
            case DUP5:
            case DUP6:
            case DUP7:
            case DUP8:
            case DUP9:
            case DUP10:
            case DUP11:
            case DUP12:
            case DUP13:
            case DUP14:
            case DUP15:
            case DUP16: {
                //复制栈中数据到栈顶
                int n = op.val() - OpCode.DUP1.val() + 1;
                DataWord word_1 = stack.get(stack.size() - n);
                program.stackPush(word_1.clone());
                program.step();

            }
            break;
            case SWAP1:
            case SWAP2:
            case SWAP3:
            case SWAP4:
            case SWAP5:
            case SWAP6:
            case SWAP7:
            case SWAP8:
            case SWAP9:
            case SWAP10:
            case SWAP11:
            case SWAP12:
            case SWAP13:
            case SWAP14:
            case SWAP15:
            case SWAP16: {
                //与栈顶交换
                int n = op.val() - OpCode.SWAP1.val() + 2;
                stack.swap(stack.size() - 1, stack.size() - n);
                program.step();
            }
            break;
            case LOG0:
            case LOG1:
            case LOG2:
            case LOG3:
            case LOG4: {

                if (program.isStaticCall()) {
                    throw new Program.StaticCallModificationException();
                }
                DataWord address = program.getOwnerAddress();

                DataWord memStart = stack.pop();
                DataWord memOffset = stack.pop();

                int nTopics = op.val() - OpCode.LOG0.val();

                List<DataWord> topics = new ArrayList<>();
                for (int i = 0; i < nTopics; ++i) {
                    DataWord topic = stack.pop();
                    topics.add(topic);
                }

                byte[] data = program.memoryChunk(memStart.intValueSafe(), memOffset.intValueSafe());

                LogInfo logInfo =
                        new LogInfo(address.getLast20Bytes(), topics, data);

                if ( LOGGER.isInfoEnabled()) {
                    hint = logInfo.toString();
                }

                program.getResult().addLogInfo(logInfo);
                program.step();
            }
            break;
            case MLOAD: {
                DataWord addr = program.stackPop();
                DataWord data = program.memoryLoad(addr);

                if ( LOGGER.isInfoEnabled()) {
                    hint = "data: " + data;
                }

                program.stackPush(data);
                program.step();
            }
            break;
            case MSTORE: {
                DataWord addr = program.stackPop();
                DataWord value = program.stackPop();

                if ( LOGGER.isInfoEnabled()) {
                    hint = "addr: " + addr + " value: " + value;
                }

                program.memorySave(addr, value);
                program.step();
            }
            break;
            case MSTORE8: {
                DataWord addr = program.stackPop();
                DataWord value = program.stackPop();
                byte[] byteVal = {value.getData()[31]};
                program.memorySave(addr.intValueSafe(), byteVal);
                program.step();
            }
            break;
            case SLOAD: {
                DataWord key = program.stackPop();
                DataWord val = program.storageLoad(key);

                if ( LOGGER.isInfoEnabled()) {
                    hint = "key: " + key + " value: " + val;
                }

                if (val == null) {
                    val = key.and(DataWord.ZERO);
                }

                program.stackPush(val);
                program.step();
            }
            break;
            case SSTORE: {
                if (program.isStaticCall()) {
                    throw new Program.StaticCallModificationException();
                }

                DataWord addr = program.stackPop();
                DataWord value = program.stackPop();

                if ( LOGGER.isInfoEnabled()) {
                    hint = "[" + program.getOwnerAddress().toPrefixString() + "] key: " + addr + " value: " + value;
                }

                program.storageSave(addr, value);
                program.step();
            }
            break;
            case JUMP: {
                DataWord pos = program.stackPop();
                int nextPC = program.verifyJumpDest(pos);

                if ( LOGGER.isInfoEnabled()) {
                    hint = "~> " + nextPC;
                }

                program.setPC(nextPC);

            }
            break;
            case JUMPI: {
                DataWord pos = program.stackPop();
                DataWord cond = program.stackPop();

                if (!cond.isZero()) {
                    int nextPC = program.verifyJumpDest(pos);

                    if ( LOGGER.isInfoEnabled()) {
                        hint = "~> " + nextPC;
                    }

                    program.setPC(nextPC);
                } else {
                    program.step();
                }

            }
            break;
            case PC: {
                int pc = program.getPC();
                DataWord pcWord = new DataWord(pc);

                if ( LOGGER.isInfoEnabled()) {
                    hint = pcWord.toString();
                }

                program.stackPush(pcWord);
                program.step();
            }
            break;
            case MSIZE: {
                int memSize = program.getMemSize();
                DataWord wordMemSize = new DataWord(memSize);

                if ( LOGGER.isInfoEnabled()) {
                    hint = "" + memSize;
                }

                program.stackPush(wordMemSize);
                program.step();
            }
            break;
            case GAS: {
                DataWord gas = program.getGas();

                if ( LOGGER.isInfoEnabled()) {
                    hint = "" + gas;
                }

                program.stackPush(gas);
                program.step();
            }
            break;

            case PUSH1:
            case PUSH2:
            case PUSH3:
            case PUSH4:
            case PUSH5:
            case PUSH6:
            case PUSH7:
            case PUSH8:
            case PUSH9:
            case PUSH10:
            case PUSH11:
                //  case PUSH12:
            case PUSH13:
            case PUSH14:
            case PUSH15:
            case PUSH16:
            case PUSH17:
            case PUSH18:
            case PUSH19:
            case PUSH20:
            case PUSH21:
            case PUSH22:
            case PUSH23:
            case PUSH24:
            case PUSH25:
            case PUSH26:
            case PUSH27:
            case PUSH28:
            case PUSH29:
            case PUSH30:
            case PUSH31:
            case PUSH32: {
                program.step();
                int nPush = op.val() - PUSH1.val() + 1;

                byte[] data = program.sweep(nPush);

                if ( LOGGER.isInfoEnabled()) {
                    hint = "" + toHexString(data);
                }

                program.stackPush(data);
            }
            break;
            case JUMPDEST: {
                program.step();
            }
            break;
            case CREATE: {
                if (program.isStaticCall()) {
                    throw new Program.StaticCallModificationException();
                }

                DataWord value = program.stackPop();
                DataWord inOffset = program.stackPop();
                DataWord inSize = program.stackPop();

                if ( LOGGER.isInfoEnabled()) {
                    LOGGER.info(LOG_STRING, String.format("%5s", "[" + program.getPC() + "]"),
                            String.format("%-12s", op.name()),
                            program.getGas().value(),
                            program.getCallDeep(), hint);
                }

                program.createContract(value, inOffset, inSize);

                program.step();
            }
            break;
            case CALL:
            case CALLCODE:
            case DELEGATECALL:
            case STATICCALL: {
                program.stackPop(); // use adjustedCallGas instead of requested
                DataWord codeAddress = program.stackPop();
                DataWord value = op.callHasValue() ?
                        program.stackPop() : DataWord.ZERO;

                if (program.isStaticCall() && op == CALL && !value.isZero()) {
                    throw new Program.StaticCallModificationException();
                }

                if (!value.isZero()) {
                    adjustedCallGas.add(new DataWord(blockChainConfig.getGasCost().getSTIPEND_CALL()));
                }

                DataWord inDataOffs = program.stackPop();
                DataWord inDataSize = program.stackPop();

                DataWord outDataOffs = program.stackPop();
                DataWord outDataSize = program.stackPop();

                if ( LOGGER.isInfoEnabled()) {
                    hint = "addr: " + toHexString(codeAddress.getLast20Bytes())
                            + " gas: " + adjustedCallGas.shortHex()
                            + " inOff: " + inDataOffs.shortHex()
                            + " inSize: " + inDataSize.shortHex();
                    LOGGER.info(LOG_STRING, String.format("%5s", "[" + program.getPC() + "]"),
                            String.format("%-12s", op.name()),
                            program.getGas().value(),
                            program.getCallDeep(), hint);
                }

                program.memoryExpand(outDataOffs, outDataSize);

                MessageCall msg = new MessageCall(
                        op, adjustedCallGas, codeAddress, value, inDataOffs, inDataSize,
                        outDataOffs, outDataSize);

                PrecompiledContracts.PrecompiledContract contract =
                        PrecompiledContracts.getContractForAddress(codeAddress, blockChainConfig);

                if (!op.callIsStateless()) {
                    program.getResult().addTouchAccount(codeAddress.getLast20Bytes());
                }

                if (contract != null) {
                    program.callToPrecompiledAddress(msg, contract);
                } else {
                    program.callToAddress(msg);
                }

                program.step();
            }
            break;
            case RETURN:
            case REVERT: {
                DataWord offset = program.stackPop();
                DataWord size = program.stackPop();

                byte[] hReturn = program.memoryChunk(offset.intValueSafe(), size.intValueSafe());
                program.setHReturn(hReturn);

                if ( LOGGER.isInfoEnabled()) {
                    hint = "data: " + toHexString(hReturn)
                            + " offset: " + offset.value()
                            + " size: " + size.value();
                }


                program.stop();

                if (op == REVERT) {
                    program.getResult().setRevert();
                }
            }
            break;
            case SUICIDE: {
                if (program.isStaticCall()) {
                    throw new Program.StaticCallModificationException();
                }

                DataWord address = program.stackPop();
                program.suicide(address);
                program.getResult().addTouchAccount(address.getLast20Bytes());

                if ( LOGGER.isInfoEnabled()) {
                    hint = "address: " + toHexString(program.getOwnerAddress().getLast20Bytes());
                }

                program.stop();
            }
            break;
            default:
                break;
        }

        return hint;
    }

    /**
     * calculate fees by op
     * @param op current op
     * @param program
     * @param blockChainConfig
     * @return CalculateFeeResult
     */
    public CalculateFeeResult calculateFees(OpCode op,Program program,BlockChainConfig blockChainConfig){

        CalculateFeeResult result = new CalculateFeeResult();
        long gasCost = op.getTier().asInt();
        GasCost gasCosts = blockChainConfig.getGasCost();
        Stack stack = program.getStack();
        long oldMemSize = program.getMemSize();
        switch (op) {
            case STOP:
                gasCost = gasCosts.getSTOP();
                break;
            case SUICIDE:
                gasCost = gasCosts.getSUICIDE();
                DataWord suicideAddressWord = stack.get(stack.size() - 1);
                if (blockChainConfig.eip161()) {
                    if (isDeadAccount(program, suicideAddressWord.getLast20Bytes()) &&
                            !program.getBalance(program.getOwnerAddress()).isZero()) {
                        gasCost += gasCosts.getNEW_ACCT_SUICIDE();
                    }
                } else {
                    if (!program.getStorage().isExist(suicideAddressWord.getLast20Bytes())) {
                        gasCost += gasCosts.getNEW_ACCT_SUICIDE();
                    }
                }
                break;
            case SSTORE:
                DataWord newValue = stack.get(stack.size() - 2);
                DataWord oldValue = program.storageLoad(stack.peek());
                if (oldValue == null && !newValue.isZero()) {
                    gasCost = gasCosts.getSET_SSTORE();
                } else if (oldValue != null && newValue.isZero()) {
                    // refund step cost policy.
                    program.futureRefundGas(gasCosts.getREFUND_SSTORE());
                    gasCost = gasCosts.getCLEAR_SSTORE();
                } else {
                    gasCost = gasCosts.getRESET_SSTORE();
                }
                break;
            case SLOAD:
                gasCost = gasCosts.getSLOAD();
                break;
            case BALANCE:
                gasCost = gasCosts.getBALANCE();
                break;

            // These all operate on memory and therefore potentially expand it:
            case MSTORE:
                gasCost += calcMemGas(gasCosts, oldMemSize, memNeeded(stack.peek(), DataWord.of(32)), 0);
                break;
            case MSTORE8:
                gasCost += calcMemGas(gasCosts, oldMemSize, memNeeded(stack.peek(), DataWord.ONE), 0);
                break;
            case MLOAD:
                gasCost += calcMemGas(gasCosts, oldMemSize, memNeeded(stack.peek(), DataWord.of(32)), 0);
                break;
            case RETURN:
            case REVERT:
                gasCost = gasCosts.getSTOP() + calcMemGas(gasCosts, oldMemSize,
                        memNeeded(stack.peek(), stack.get(stack.size() - 2)), 0);
                break;
            case SHA3:
                gasCost = gasCosts.getSHA3() + calcMemGas(gasCosts, oldMemSize, memNeeded(stack.peek(), stack.get(stack.size() - 2)), 0);
                DataWord size = stack.get(stack.size() - 2);
                long chunkUsed = VMUtils.getSizeInWords(size.longValueSafe());
                gasCost += chunkUsed * gasCosts.getSHA3_WORD();
                break;
            case CALLDATACOPY:
            case RETURNDATACOPY:
                gasCost += calcMemGas(gasCosts, oldMemSize,
                        memNeeded(stack.peek(), stack.get(stack.size() - 3)),
                        stack.get(stack.size() - 3).longValueSafe());
                break;
            case CODECOPY:
                gasCost += calcMemGas(gasCosts, oldMemSize,
                        memNeeded(stack.peek(), stack.get(stack.size() - 3)),
                        stack.get(stack.size() - 3).longValueSafe());
                break;
            case EXTCODESIZE:
                gasCost = gasCosts.getEXT_CODE_SIZE();
                break;
            case EXTCODECOPY:
                gasCost = gasCosts.getEXT_CODE_COPY() + calcMemGas(gasCosts, oldMemSize,
                        memNeeded(stack.get(stack.size() - 2), stack.get(stack.size() - 4)),
                        stack.get(stack.size() - 4).longValueSafe());
                break;
            case CALL:
            case CALLCODE:
            case DELEGATECALL:
            case STATICCALL:

                gasCost = gasCosts.getCALL();
                DataWord callGasWord = stack.get(stack.size() - 1);

                DataWord callAddressWord = stack.get(stack.size() - 2);

                DataWord value = op.callHasValue() ?
                        stack.get(stack.size() - 3) : DataWord.ZERO;

                //check to see if account does not exist and is not a preCompiled contract
                if (op == CALL) {
                    if (blockChainConfig.eip161()) {
                        if (isDeadAccount(program, callAddressWord.getLast20Bytes()) && !value.isZero()) {
                            gasCost += gasCosts.getNEW_ACCT_CALL();
                        }
                    } else {
                        if (!program.getStorage().isExist(callAddressWord.getLast20Bytes())) {
                            gasCost += gasCosts.getNEW_ACCT_CALL();
                        }
                    }
                }

                //TODO #POC9 Make sure this is converted to BigInteger (256num support)
                if (!value.isZero()) {
                    gasCost += gasCosts.getVT_CALL();
                }

                int opOff = op.callHasValue() ? 4 : 3;
                // in offset+size
                BigInteger in = memNeeded(stack.get(stack.size() - opOff), stack.get(stack.size() - opOff - 1));
                // out offset+size
                BigInteger out = memNeeded(stack.get(stack.size() - opOff - 2), stack.get(stack.size() - opOff - 3));
                gasCost += calcMemGas(gasCosts, oldMemSize, in.max(out), 0);

                if (gasCost > program.getGas().longValueSafe()) {
                    throw Program.Exception.notEnoughOpGas(op, callGasWord, program.getGas());
                }

                DataWord gasLeft = program.getGas().clone();
                gasLeft.sub(new DataWord(gasCost));
                DataWord adjustedCallGas = callGasWord.clone();
                result.setAdjustedCallGas(adjustedCallGas);
                gasCost +=  Math.min(gasLeft.intValueSafe(), adjustedCallGas.longValueSafe());
                break;
            case CREATE:
                gasCost = gasCosts.getCREATE() + calcMemGas(gasCosts, oldMemSize,
                        memNeeded(stack.get(stack.size() - 2), stack.get(stack.size() - 3)), 0);
                break;
            case LOG0:
            case LOG1:
            case LOG2:
            case LOG3:
            case LOG4:

                int nTopics = op.val() - OpCode.LOG0.val();

                BigInteger dataSize = stack.get(stack.size() - 2).value();
                BigInteger dataCost = dataSize.multiply(BigInteger.valueOf(gasCosts.getLOG_DATA_GAS()));
                if (program.getGas().value().compareTo(dataCost) < 0) {
                    throw Program.Exception.notEnoughOpGas(op, dataCost, program.getGas().value());
                }

                gasCost = gasCosts.getLOG_GAS() +
                        gasCosts.getLOG_TOPIC_GAS() * nTopics +
                        gasCosts.getLOG_DATA_GAS() * stack.get(stack.size() - 2).longValue() +
                        calcMemGas(gasCosts, oldMemSize, memNeeded(stack.peek(), stack.get(stack.size() - 2)), 0);
                break;
            case EXP:

                DataWord exp = stack.get(stack.size() - 2);
                int bytesOccupied = exp.bytesOccupied();
                gasCost = gasCosts.getEXP_GAS() + gasCosts.getEXP_BYTE_GAS() * bytesOccupied;
                break;
            default:
                break;
        }
        result.setGasCost(gasCost);
        return result;
    }

    /**
     * calculate fee result vo
     */
    @Setter
    @Getter
    class CalculateFeeResult{
        long gasCost;
        DataWord adjustedCallGas;

    }
    public void play(Program program) {
        try {
            if (vmHook != null) {
                vmHook.startPlay(program);
            }

            if (program.byTestingSuite()) {
                return;
            }

            while (!program.isStopped()) {
                this.step(program);
            }

        } catch (RuntimeException e) {
            e.printStackTrace();
            program.setRuntimeFailure(e);
        } catch (StackOverflowError soe) {
            LOGGER.error("\n !!! StackOverflowError: update your java executeContract command with -Xss2M !!!\n", soe);
            System.exit(-1);
        } finally {
            if (vmHook != null) {
                vmHook.stopPlay(program);
            }
        }
    }

    /*
     * Dumping the VM state at the current operation in various styles
     *  - standard  Not Yet Implemented
     *  - standard+ (owner address, program counter, operation, gas left)
     *  - pretty (stack, memory, storage, level, contract,
     *              vmCounter, internalSteps, operation
                    gasBefore, gasCost, memWords)
     */
    private void dumpLine(OpCode op, long gasBefore, long gasCost, long memWords, Program program) {
        if ("standard+".equals(config.dumpStyle())) {
            switch (op) {
                case STOP:
                case RETURN:
                case SUICIDE:

                    ContractDetails details = program.getStorage()
                            .getContractDetails(program.getOwnerAddress().getLast20Bytes());
                    List<DataWord> storageKeys = new ArrayList<>(details.getStorage().keySet());
                    Collections.sort(storageKeys);

                    for (DataWord key : storageKeys) {
                        DUMP_LOGGER.trace("{} {}",
                                toHexString(key.getNoLeadZeroesData()),
                                toHexString(details.getStorage().get(key).getNoLeadZeroesData()));
                    }
                default:
                    break;
            }
            String addressString = toHexString(program.getOwnerAddress().getLast20Bytes());
            String pcString = toHexString(new DataWord(program.getPC()).getNoLeadZeroesData());
            String opString = toHexString(new byte[]{op.val()});
            String gasString = toHexString(program.getGas().getNoLeadZeroesData());

            DUMP_LOGGER.trace("{} {} {} {}", addressString, pcString, opString, gasString);
        } else if ("pretty".equals(config.dumpStyle())) {
            DUMP_LOGGER.trace("    STACK");
            for (DataWord item : program.getStack()) {
                DUMP_LOGGER.trace("{}", item);
            }
            DUMP_LOGGER.trace("    MEMORY");
            String memoryString = program.memoryToString();
            if (!"".equals(memoryString)) {
                DUMP_LOGGER.trace("{}", memoryString);
            }

            DUMP_LOGGER.trace("    STORAGE");
            ContractDetails details = program.getStorage()
                    .getContractDetails(program.getOwnerAddress().getLast20Bytes());
            List<DataWord> storageKeys = new ArrayList<>(details.getStorage().keySet());
            Collections.sort(storageKeys);

            for (DataWord key : storageKeys) {
                DUMP_LOGGER.trace("{}: {}",
                        key.shortHex(),
                        details.getStorage().get(key).shortHex());
            }

            int level = program.getCallDeep();
            String contract = toHexString(program.getOwnerAddress().getLast20Bytes());
            String internalSteps = String.format("%4s", Integer.toHexString(program.getPC())).replace(' ', '0').toUpperCase();
            DUMP_LOGGER.trace("{} | {} | #{} | {} : {} | {} | -{} | {}x32",
                    level, contract, vmCounter, internalSteps, op,
                    gasBefore, gasCost, memWords);
        }
    }
}
