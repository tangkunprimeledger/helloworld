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
package com.higgs.trust.evmcontract.vm.program.invoke;

import com.higgs.trust.evmcontract.core.Block;
import com.higgs.trust.evmcontract.core.Repository;
import com.higgs.trust.evmcontract.core.Transaction;
import com.higgs.trust.evmcontract.db.BlockStore;
import com.higgs.trust.evmcontract.util.ByteUtil;
import com.higgs.trust.evmcontract.vm.DataWord;
import com.higgs.trust.evmcontract.vm.VM;
import com.higgs.trust.evmcontract.vm.program.Program;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

import static org.apache.commons.lang3.ArrayUtils.nullToEmpty;

/**
 * @author Roman Mandeleil
 * @since 08.06.2014
 */
@Component("ProgramInvokeFactory")
public class ProgramInvokeFactoryImpl implements ProgramInvokeFactory {

    private static final Logger logger = LoggerFactory.getLogger("VM");

    // Invocation by the wire tx
    @Override
    public ProgramInvoke createProgramInvoke(Transaction tx, Block block, Repository repository,
                                             BlockStore blockStore) {

        /***         ADDRESS op       ***/
        // YP: Get address of currently executing account.
        byte[] address = tx.isContractCreation() ? tx.getContractAddress() : tx.getReceiveAddress();

        /***         ORIGIN op       ***/
        // YP: This is the sender of original transaction; it is never a contract.
        byte[] origin = tx.getSender();

        /***         CALLER op       ***/
        // YP: This is the address of the account that is directly responsible for this execution.
        byte[] caller = tx.getSender();

        /***         BALANCE op       ***/
        byte[] balance = repository.getBalance(address).toByteArray();

        /***         GASPRICE op       ***/
        byte[] gasPrice = tx.getGasPrice();

        /*** GAS op ***/
        byte[] gas = tx.getGasLimit();

        /***        CALLVALUE op      ***/
        byte[] callValue = nullToEmpty(tx.getValue());

        /***     CALLDATALOAD  op   ***/
        /***     CALLDATACOPY  op   ***/
        /***     CALLDATASIZE  op   ***/
        byte[] data = tx.isContractCreation() ? ByteUtil.EMPTY_BYTE_ARRAY : nullToEmpty(tx.getData());

        /***    PREVHASH  op  ***/
        byte[] lastHash = null;//block.getParentHash();

        /***   COINBASE  op ***/
        byte[] coinbase = null;//block.getCoinbase();

        /*** TIMESTAMP  op  ***/
        long timestamp = 0;//block.getTimestamp();

        /*** NUMBER  op  ***/
        long number = 0;//block.getNumber();

        /*** DIFFICULTY  op  ***/
        byte[] difficulty = null;// block.getDifficulty();

        /*** GASLIMIT op ***/
        byte[] gaslimit = null;//block.getGasLimit();

        if ( logger.isInfoEnabled()) {
            logger.info("Top level call: \n" +
                            "tx.hash={}\n" +
                            "address={}\n" +
                            "origin={}\n" +
                            "caller={}\n" +
                            "balance={}\n" +
                            "gasPrice={}\n" +
                            "gas={}\n" +
                            "callValue={}\n" +
                            "data={}\n" +
                            "lastHash={}\n" +
                            "coinbase={}\n" +
                            "timestamp={}\n" +
                            "blockNumber={}\n" +
                            "difficulty={}\n" +
                            "gaslimit={}\n",

                    ByteUtil.toHexString(tx.getHash()),
                    ByteUtil.toHexString(address),
                    ByteUtil.toHexString(origin),
                    ByteUtil.toHexString(caller),
                    ByteUtil.bytesToBigInteger(balance),
                    ByteUtil.bytesToBigInteger(gasPrice),
                    ByteUtil.bytesToBigInteger(gas),
                    ByteUtil.bytesToBigInteger(callValue),
                    ByteUtil.toHexString(data),
                    ByteUtil.toHexString(lastHash),
                    ByteUtil.toHexString(coinbase),
                    timestamp,
                    number,
                    ByteUtil.toHexString(difficulty),
                    gaslimit);
        }

        return new ProgramInvokeImpl(address, origin, caller, balance, gasPrice, gas, callValue, data,
                lastHash, coinbase, timestamp, number, difficulty, gaslimit,
                repository, blockStore);
    }

    /**
     * This invocation created for contract call contract
     */
    @Override
    public ProgramInvoke createProgramInvoke(Program program, DataWord toAddress, DataWord callerAddress,
                                             DataWord inValue, DataWord inGas,
                                             BigInteger balanceInt, byte[] dataIn,
                                             Repository repository, BlockStore blockStore,
                                             boolean isStaticCall, boolean byTestingSuite) {

        DataWord address = toAddress;
        DataWord origin = program.getOriginAddress();
        DataWord caller = callerAddress;

        DataWord balance = new DataWord(balanceInt.toByteArray());
        DataWord gasPrice = program.getGasPrice();
        DataWord gas = inGas;
        DataWord callValue = inValue;

        byte[] data = dataIn;
        DataWord lastHash = program.getPrevHash();
        DataWord coinbase = program.getCoinbase();
        DataWord timestamp = program.getTimestamp();
        DataWord number = program.getNumber();
        DataWord difficulty = program.getDifficulty();
        DataWord gasLimit = program.getGasLimit();

        if ( logger.isInfoEnabled()) {
            logger.info("Internal call: \n" +
                            "address={}\n" +
                            "origin={}\n" +
                            "caller={}\n" +
                            "balance={}\n" +
                            "gasPrice={}\n" +
                            "gas={}\n" +
                            "callValue={}\n" +
                            "data={}\n" +
                            "lastHash={}\n" +
                            "coinbase={}\n" +
                            "timestamp={}\n" +
                            "blockNumber={}\n" +
                            "difficulty={}\n" +
                            "gaslimit={}\n",
                    ByteUtil.toHexString(address.getLast20Bytes()),
                    ByteUtil.toHexString(origin.getLast20Bytes()),
                    ByteUtil.toHexString(caller.getLast20Bytes()),
                    balance.toString(),
                    gasPrice.longValue(),
                    gas.longValue(),
                    ByteUtil.toHexString(callValue.getNoLeadZeroesData()),
                    ByteUtil.toHexString(data),
                    ByteUtil.toHexString(lastHash.getData()),
                    ByteUtil.toHexString(coinbase.getLast20Bytes()),
                    timestamp.longValue(),
                    number.longValue(),
                    ByteUtil.toHexString(difficulty.getNoLeadZeroesData()),
                    gasLimit.bigIntValue());
        }

        return new ProgramInvokeImpl(address, origin, caller, balance, gasPrice, gas, callValue,
                data, lastHash, coinbase, timestamp, number, difficulty, gasLimit,
                repository, program.getCallDeep() + 1, blockStore, isStaticCall, byTestingSuite);
    }
}
