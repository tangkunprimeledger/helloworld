package com.higgs.trust.evmcontract.facade;

import com.higgs.trust.evmcontract.core.Repository;
import com.higgs.trust.evmcontract.crypto.HashUtil;
import com.higgs.trust.evmcontract.datasource.inmem.HashMapDB;
import com.higgs.trust.evmcontract.db.BlockStore;
import com.higgs.trust.evmcontract.db.RepositoryRoot;
import com.higgs.trust.evmcontract.facade.exception.ContractContextException;
import com.higgs.trust.evmcontract.facade.util.ContractUtil;
import com.higgs.trust.evmcontract.solidity.Abi;
import com.higgs.trust.evmcontract.solidity.CallTransaction;
import com.higgs.trust.evmcontract.solidity.compiler.CompilationResult;
import com.higgs.trust.evmcontract.solidity.compiler.SolidityCompiler;
import com.higgs.trust.evmcontract.vm.DataWord;
import org.junit.*;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static com.higgs.trust.evmcontract.solidity.compiler.SolidityCompiler.Options.*;
import static org.junit.Assert.*;

/**
 * @author Chen Jiawei
 * @date 2018-11-21
 */
public class ExecutorTest {
    private static ExecutorFactory<ContractExecutionContext, ContractExecutionResult> factory;
    private static BlockStore blockStore;
    private Repository blockRepository;

    @BeforeClass
    public static void setUp() {
        factory = new ContractExecutorFactory();
        blockStore = buildBlockStore();
    }

    @AfterClass
    public static void tearDown() {
        blockStore = null;
        factory = null;
    }

    @Before
    public void init() {
        blockRepository = buildBlockRepository();
    }

    @After
    public void finish() {
        blockRepository = null;
    }

    private static BlockStore buildBlockStore() {
        return new BlockStoreAdapter() {
            @Override
            public byte[] getBlockHashByNumber(long blockNumber, byte[] branchBlockHash) {
                return Hex.decode("0e4e78a67d45f061a5b68847534b428a1277652677b6467f2d1f33819274e792");
            }
        };
    }

    private static Repository buildBlockRepository() {
        HashMapDB<byte[]> dataSource = new HashMapDB<>();
        return new RepositoryRoot(dataSource, null);
    }

    private ContractExecutionContext buildContractExecutionContext(
            ContractTypeEnum contractType, byte[] transactionHash, byte[] nonce, byte[] senderAddress,
            byte[] receiverAddress, byte[] value, byte[] data, byte[] parentHash, byte[] minerAddress,
            long timestamp, long number) {
        return new ContractExecutionContext(contractType, transactionHash, nonce, senderAddress, receiverAddress,
                value, data, parentHash, minerAddress, timestamp, number, blockStore, blockRepository);
    }


    //    pragma solidity ^0.4.12;
    //
    //    contract DataStorage {
    //        uint256 data;
    //
    //        function set(uint256 x) public {
    //            data = x;
    //        }
    //
    //        function get() public constant returns (uint256 retVal) {
    //            return data;
    //        }
    //    }
    /**
     * 部署成功。
     */
    @Test(timeout = 1000L)
    public void testExecute_ContractCreation_001() {
        ContractTypeEnum contractType = ContractTypeEnum.CONTRACT_CREATION;
        byte[] transactionHash = Hex.decode("03e22f204d45f061a5b68847534b428a1277652677b6467f2d1f3381bbc4115c");
        byte[] nonce = new DataWord(0).getData();
        byte[] senderAddress = Hex.decode("05792f204d45f061a5b68847534b428a127ae583");
        byte[] receiverAddress = Hex.decode("00a615668486da40f31fd050854fb137b317e056");
        byte[] value = new DataWord(0).getData();
        byte[] data = Hex.decode("608060405234801561001057600080fd5b5060df8061001f6000396000f30060806040526004361060" +
                "49576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806360fe47b11" +
                "4604e5780636d4ce63c146078575b600080fd5b348015605957600080fd5b50607660048036038101908080359060200190" +
                "92919050505060a0565b005b348015608357600080fd5b50608a60aa565b604051808281526020019150506040518091039" +
                "0f35b8060008190555050565b600080549050905600a165627a7a72305820879b17325bbf18ab328711a82852468ddfbb5e" +
                "f91bb8ab27ff16546b16217d380029");
        byte[] parentHash = Hex.decode("098765467d45f061a5b68847534b428a1277652677b6467f2d1f3381ae683910");
        byte[] minerAddress = Hex.decode("5f061a5b68847534b428a1277652677b6467f2d1");
        long timestamp = 3785872384L;
        long number = 12;

        blockRepository.createAccount(senderAddress);
        blockRepository.addBalance(senderAddress, ContractUtil.toBigInteger(value));
        blockRepository.commit();
        assertEquals(Hex.toHexString(receiverAddress), Hex.toHexString(HashUtil.calcNewAddr(senderAddress, nonce)));


        ContractExecutionContext context = buildContractExecutionContext(contractType, transactionHash,
                nonce, senderAddress, receiverAddress, value, data, parentHash, minerAddress, timestamp, number);
        Executor<ContractExecutionResult> executor = factory.createExecutor(context);
        ContractExecutionResult result = executor.execute();


        assertNotNull(result);
        assertEquals(1, result.getTouchedAccountAddresses().size());
        assertTrue(result.getTouchedAccountAddresses().contains(receiverAddress));
        assertEquals(Hex.toHexString(transactionHash), Hex.toHexString(result.getTransactionHash()));
        assertEquals(Hex.toHexString(value), Hex.toHexString(result.getValue()));
        assertEquals(0, result.getLogInfoList().size());
        assertEquals("6080604052600436106049576000357c01000000000000000000000000000000000000000000000000000000009004" +
                "63ffffffff16806360fe47b114604e5780636d4ce63c146078575b600080fd5b348015605957600080fd5b5060766004803" +
                "603810190808035906020019092919050505060a0565b005b348015608357600080fd5b50608a60aa565b60405180828152" +
                "60200191505060405180910390f35b8060008190555050565b600080549050905600a165627a7a72305820879b17325bbf1" +
                "8ab328711a82852468ddfbb5ef91bb8ab27ff16546b16217d380029", Hex.toHexString(result.getResult()));
        assertEquals(0, result.getDeleteAccounts().size());
        assertEquals(0, result.getInternalTransactions().size());
        assertNull(result.getException());
        assertNotNull(result.getStateRoot());
        assertFalse(result.getRevert());
        assertNull(result.getErrorMessage());

        assertEquals(ContractUtil.toBigInteger(new DataWord(1).getData()), blockRepository.getNonce(senderAddress));
        assertNotNull(blockRepository.getAccountState(receiverAddress));
        assertArrayEquals(result.getResult(), blockRepository.getCode(receiverAddress));

        blockRepository.commit();
        Repository repository = blockRepository.getSnapshotTo(result.getStateRoot());
        assertEquals(ContractUtil.toBigInteger(new DataWord(1).getData()), repository.getNonce(senderAddress));
        assertNotNull(repository.getAccountState(receiverAddress));
        assertArrayEquals(result.getResult(), repository.getCode(receiverAddress));
    }

    //    pragma solidity ^0.4.12;
    //
    //    contract DataStorage {
    //        uint256 data;
    //
    //        function set(uint256 x) public {
    //            data = x;
    //        }
    //
    //        function get() public constant returns (uint256 retVal) {
    //            return data;
    //        }
    //    }
    /**
     * 部署失败：value不为0。
     */
    @Test(timeout = 1000L)
    public void testExecute_ContractCreation_002() {
        ContractTypeEnum contractType = ContractTypeEnum.CONTRACT_CREATION;
        byte[] transactionHash = Hex.decode("03e22f204d45f061a5b68847534b428a1277652677b6467f2d1f3381bbc4115c");
        byte[] nonce = new DataWord(0).getData();
        byte[] senderAddress = Hex.decode("05792f204d45f061a5b68847534b428a127ae583");
        byte[] receiverAddress = Hex.decode("00a615668486da40f31fd050854fb137b317e056");
        byte[] value = new DataWord(10000).getData();
        byte[] data = Hex.decode("608060405234801561001057600080fd5b5060df8061001f6000396000f30060806040526004361060" +
                "49576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806360fe47b11" +
                "4604e5780636d4ce63c146078575b600080fd5b348015605957600080fd5b50607660048036038101908080359060200190" +
                "92919050505060a0565b005b348015608357600080fd5b50608a60aa565b604051808281526020019150506040518091039" +
                "0f35b8060008190555050565b600080549050905600a165627a7a72305820879b17325bbf18ab328711a82852468ddfbb5e" +
                "f91bb8ab27ff16546b16217d380029");
        byte[] parentHash = Hex.decode("098765467d45f061a5b68847534b428a1277652677b6467f2d1f3381ae683910");
        byte[] minerAddress = Hex.decode("5f061a5b68847534b428a1277652677b6467f2d1");
        long timestamp = 3785872384L;
        long number = 12;

        blockRepository.createAccount(senderAddress);
        blockRepository.addBalance(senderAddress, ContractUtil.toBigInteger(value));
        blockRepository.commit();
        byte[] root = blockRepository.getRoot();
        assertEquals(Hex.toHexString(receiverAddress), Hex.toHexString(HashUtil.calcNewAddr(senderAddress, nonce)));


        ContractExecutionContext context = buildContractExecutionContext(contractType, transactionHash,
                nonce, senderAddress, receiverAddress, value, data, parentHash, minerAddress, timestamp, number);
        Executor<ContractExecutionResult> executor = factory.createExecutor(context);
        ContractExecutionResult result = executor.execute();


        assertNotNull(result);
        assertEquals(0, result.getTouchedAccountAddresses().size());
        assertEquals(Hex.toHexString(transactionHash), Hex.toHexString(result.getTransactionHash()));
        assertEquals(Hex.toHexString(value), Hex.toHexString(result.getValue()));
        assertEquals(0, result.getLogInfoList().size());
        assertEquals("", Hex.toHexString(result.getResult()));
        assertEquals(0, result.getDeleteAccounts().size());
        assertEquals(0, result.getInternalTransactions().size());
        assertNull(result.getException());
        assertNotEquals(Hex.toHexString(root), Hex.toHexString(result.getStateRoot()));
        assertTrue(result.getRevert());
        assertEquals("REVERT opcode executed", result.getErrorMessage());

        assertEquals(ContractUtil.toBigInteger(new DataWord(1).getData()), blockRepository.getNonce(senderAddress));
        assertNull(blockRepository.getAccountState(receiverAddress));
        assertEquals(0, blockRepository.getCode(receiverAddress).length);

        blockRepository.commit();
        Repository repository = blockRepository.getSnapshotTo(result.getStateRoot());
        assertEquals(ContractUtil.toBigInteger(new DataWord(1).getData()), repository.getNonce(senderAddress));
        assertNull(repository.getAccountState(receiverAddress));
        assertEquals(0, repository.getCode(receiverAddress).length);
    }

    /**
     * 部署成功: 字节码乱写。这是可以接受的，因为Solidity应用的逻辑是VM控制不了的。
     */
    @Test(timeout = 1000L)
    public void testExecute_ContractCreation_003() {
        ContractTypeEnum contractType = ContractTypeEnum.CONTRACT_CREATION;
        byte[] transactionHash = Hex.decode("03e22f204d45f061a5b68847534b428a1277652677b6467f2d1f3381bbc4115c");
        byte[] nonce = new DataWord(0).getData();
        byte[] senderAddress = Hex.decode("05792f204d45f061a5b68847534b428a127ae583");
        byte[] receiverAddress = Hex.decode("00a615668486da40f31fd050854fb137b317e056");
        byte[] value = new DataWord(0).getData();
        byte[] data = Hex.decode("6080");
        byte[] parentHash = Hex.decode("098765467d45f061a5b68847534b428a1277652677b6467f2d1f3381ae683910");
        byte[] minerAddress = Hex.decode("5f061a5b68847534b428a1277652677b6467f2d1");
        long timestamp = 3785872384L;
        long number = 12;

        blockRepository.createAccount(senderAddress);
        blockRepository.addBalance(senderAddress, ContractUtil.toBigInteger(value));
        blockRepository.commit();
        assertEquals(Hex.toHexString(receiverAddress), Hex.toHexString(HashUtil.calcNewAddr(senderAddress, nonce)));


        ContractExecutionContext context = buildContractExecutionContext(contractType, transactionHash,
                nonce, senderAddress, receiverAddress, value, data, parentHash, minerAddress, timestamp, number);
        Executor<ContractExecutionResult> executor = factory.createExecutor(context);
        ContractExecutionResult result = executor.execute();


        assertNotNull(result);
        assertEquals(1, result.getTouchedAccountAddresses().size());
        assertTrue(result.getTouchedAccountAddresses().contains(receiverAddress));
        assertEquals(Hex.toHexString(transactionHash), Hex.toHexString(result.getTransactionHash()));
        assertEquals(Hex.toHexString(value), Hex.toHexString(result.getValue()));
        assertEquals(0, result.getLogInfoList().size());
        assertEquals("", Hex.toHexString(result.getResult()));
        assertEquals(0, result.getDeleteAccounts().size());
        assertEquals(0, result.getInternalTransactions().size());
        assertNull(result.getException());
        assertNotNull(result.getStateRoot());
        assertFalse(result.getRevert());
        assertNull(result.getErrorMessage());

        assertEquals(ContractUtil.toBigInteger(new DataWord(1).getData()), blockRepository.getNonce(senderAddress));
        assertNotNull(blockRepository.getAccountState(receiverAddress));
        assertArrayEquals(result.getResult(), blockRepository.getCode(receiverAddress));

        blockRepository.commit();
        Repository repository = blockRepository.getSnapshotTo(result.getStateRoot());
        assertEquals(ContractUtil.toBigInteger(new DataWord(1).getData()), repository.getNonce(senderAddress));
        assertNotNull(repository.getAccountState(receiverAddress));
        assertArrayEquals(result.getResult(), repository.getCode(receiverAddress));
    }

    /**
     * 部署异常: 空字节码是不许的。
     */
    @Test
    public void testExecute_ContractCreation_004() {
        ContractTypeEnum contractType = ContractTypeEnum.CONTRACT_CREATION;
        byte[] transactionHash = Hex.decode("03e22f204d45f061a5b68847534b428a1277652677b6467f2d1f3381bbc4115c");
        byte[] nonce = new DataWord(0).getData();
        byte[] senderAddress = Hex.decode("05792f204d45f061a5b68847534b428a127ae583");
        byte[] receiverAddress = Hex.decode("00a615668486da40f31fd050854fb137b317e056");
        byte[] value = new DataWord(0).getData();
        byte[] data = Hex.decode("");
        byte[] parentHash = Hex.decode("098765467d45f061a5b68847534b428a1277652677b6467f2d1f3381ae683910");
        byte[] minerAddress = Hex.decode("5f061a5b68847534b428a1277652677b6467f2d1");
        long timestamp = 3785872384L;
        long number = 12;

        blockRepository.createAccount(senderAddress);
        blockRepository.addBalance(senderAddress, ContractUtil.toBigInteger(value));
        blockRepository.commit();
        assertEquals(Hex.toHexString(receiverAddress), Hex.toHexString(HashUtil.calcNewAddr(senderAddress, nonce)));


        ContractExecutionContext context = buildContractExecutionContext(contractType, transactionHash,
                nonce, senderAddress, receiverAddress, value, data, parentHash, minerAddress, timestamp, number);
        Executor<ContractExecutionResult> executor = factory.createExecutor(context);
        try {
            executor.execute();
            fail("should throw ContractContextException");
        } catch (Exception e) {
            assertEquals(ContractContextException.class, e.getClass());
            assertTrue(e.getMessage().contains("Payload for contract cannot be empty"));
        }
    }

    //    pragma solidity ^0.4.12;
    //
    //    contract DataStorage {
    //        uint256 data;
    //
    //        constructor(uint256 x) public {
    //            data = x;
    //        }
    //
    //        function set(uint256 x) public {
    //            data = x;
    //        }
    //
    //        function get() public constant returns (uint256 retVal) {
    //            return data;
    //        }
    //    }
    /**
     * 部署成功：含非默认构造方法，传参18。
     */
    @Test(timeout = 1000L)
    public void testExecute_ContractCreation_005() {
        ContractTypeEnum contractType = ContractTypeEnum.CONTRACT_CREATION;
        byte[] transactionHash = Hex.decode("03e22f204d45f061a5b68847534b428a1277652677b6467f2d1f3381bbc4115c");
        byte[] nonce = new DataWord(0).getData();
        byte[] senderAddress = Hex.decode("05792f204d45f061a5b68847534b428a127ae583");
        byte[] receiverAddress = Hex.decode("00a615668486da40f31fd050854fb137b317e056");
        byte[] value = new DataWord(0).getData();
        byte[] data = Hex.decode("608060405234801561001057600080fd5b506040516020806101288339810180604052810190808051" +
                "9060200190929190505050806000819055505060df806100496000396000f3006080604052600436106049576000357c010" +
                "0000000000000000000000000000000000000000000000000000000900463ffffffff16806360fe47b114604e5780636d4c" +
                "e63c146078575b600080fd5b348015605957600080fd5b5060766004803603810190808035906020019092919050505060a" +
                "0565b005b348015608357600080fd5b50608a60aa565b6040518082815260200191505060405180910390f35b8060008190" +
                "555050565b600080549050905600a165627a7a72305820ae881b096783230f034d45fa950fdeab31f10a90e88ae75ae37c5" +
                "c54a5ab89520029" + "0000000000000000000000000000000000000000000000000000000000000012");
        byte[] parentHash = Hex.decode("098765467d45f061a5b68847534b428a1277652677b6467f2d1f3381ae683910");
        byte[] minerAddress = Hex.decode("5f061a5b68847534b428a1277652677b6467f2d1");
        long timestamp = 3785872384L;
        long number = 12;

        blockRepository.createAccount(senderAddress);
        blockRepository.addBalance(senderAddress, ContractUtil.toBigInteger(value));
        blockRepository.commit();
        assertEquals(Hex.toHexString(receiverAddress), Hex.toHexString(HashUtil.calcNewAddr(senderAddress, nonce)));


        ContractExecutionContext context = buildContractExecutionContext(contractType, transactionHash,
                nonce, senderAddress, receiverAddress, value, data, parentHash, minerAddress, timestamp, number);
        Executor<ContractExecutionResult> executor = factory.createExecutor(context);
        ContractExecutionResult result = executor.execute();


        assertNotNull(result);
        assertEquals(1, result.getTouchedAccountAddresses().size());
        assertTrue(result.getTouchedAccountAddresses().contains(receiverAddress));
        assertEquals(Hex.toHexString(transactionHash), Hex.toHexString(result.getTransactionHash()));
        assertEquals(Hex.toHexString(value), Hex.toHexString(result.getValue()));
        assertEquals(0, result.getLogInfoList().size());
        assertEquals("6080604052600436106049576000357c01000000000000000000000000000000000000000000000000000000009004" +
                "63ffffffff16806360fe47b114604e5780636d4ce63c146078575b600080fd5b348015605957600080fd5b5060766004803" +
                "603810190808035906020019092919050505060a0565b005b348015608357600080fd5b50608a60aa565b60405180828152" +
                "60200191505060405180910390f35b8060008190555050565b600080549050905600a165627a7a72305820ae881b0967832" +
                "30f034d45fa950fdeab31f10a90e88ae75ae37c5c54a5ab89520029", Hex.toHexString(result.getResult()));
        assertEquals(0, result.getDeleteAccounts().size());
        assertEquals(0, result.getInternalTransactions().size());
        assertNull(result.getException());
        assertNotNull(result.getStateRoot());
        assertFalse(result.getRevert());
        assertNull(result.getErrorMessage());

        assertEquals(ContractUtil.toBigInteger(new DataWord(1).getData()), blockRepository.getNonce(senderAddress));
        assertNotNull(blockRepository.getAccountState(receiverAddress));
        assertArrayEquals(result.getResult(), blockRepository.getCode(receiverAddress));
        assertEquals(18, blockRepository.getStorageValue(receiverAddress, new DataWord(0)).intValueSafe());

        blockRepository.commit();
        Repository repository = blockRepository.getSnapshotTo(result.getStateRoot());
        assertEquals(ContractUtil.toBigInteger(new DataWord(1).getData()), repository.getNonce(senderAddress));
        assertNotNull(repository.getAccountState(receiverAddress));
        assertArrayEquals(result.getResult(), repository.getCode(receiverAddress));
        assertEquals(18, repository.getStorageValue(receiverAddress, new DataWord(0)).intValueSafe());
    }

    //    pragma solidity ^0.4.12;
    //
    //    contract DataStorage {
    //        uint256 data;
    //
    //        constructor(uint256 x) public {
    //            data = x;
    //        }
    //
    //        function set(uint256 x) public {
    //            data = x;
    //        }
    //
    //        function get() public constant returns (uint256 retVal) {
    //            return data;
    //        }
    //    }
    /**
     * 合约调用：调用get方法，返回字段18。
     */
    @Test(timeout = 1000L)
    public void testExecute_CustomerContractInvocation_006() {
        testExecute_ContractCreation_005();

        ContractTypeEnum contractType = ContractTypeEnum.CUSTOMER_CONTRACT_INVOCATION;
        byte[] transactionHash = Hex.decode("03e22f204d45f061a5b68847534b428a1277652677b6467f2d1f3381bbc4115c");
        byte[] nonce = new DataWord(1).getData();
        byte[] senderAddress = Hex.decode("05792f204d45f061a5b68847534b428a127ae583");
        byte[] receiverAddress = Hex.decode("00a615668486da40f31fd050854fb137b317e056");
        byte[] value = new DataWord(0).getData();
        byte[] data = Hex.decode("6d4ce63c");
        byte[] parentHash = Hex.decode("098765467d45f061a5b68847534b428a1277652677b6467f2d1f3381ae683910");
        byte[] minerAddress = Hex.decode("5f061a5b68847534b428a1277652677b6467f2d1");
        long timestamp = 3785872384L;
        long number = 12;

        blockRepository.addBalance(senderAddress, ContractUtil.toBigInteger(value));
        blockRepository.commit();


        ContractExecutionContext context = buildContractExecutionContext(contractType, transactionHash,
                nonce, senderAddress, receiverAddress, value, data, parentHash, minerAddress, timestamp, number);
        Executor<ContractExecutionResult> executor = factory.createExecutor(context);
        ContractExecutionResult result = executor.execute();


        assertNotNull(result);
        assertEquals(1, result.getTouchedAccountAddresses().size());
        assertTrue(result.getTouchedAccountAddresses().contains(receiverAddress));
        assertEquals(Hex.toHexString(transactionHash), Hex.toHexString(result.getTransactionHash()));
        assertEquals(Hex.toHexString(value), Hex.toHexString(result.getValue()));
        assertEquals(0, result.getLogInfoList().size());
        assertEquals("0000000000000000000000000000000000000000000000000000000000000012", Hex.toHexString(result.getResult()));
        assertEquals(0, result.getDeleteAccounts().size());
        assertEquals(0, result.getInternalTransactions().size());
        assertNull(result.getException());
        assertNotNull(result.getStateRoot());
        assertFalse(result.getRevert());
        assertNull(result.getErrorMessage());

        assertEquals(ContractUtil.toBigInteger(new DataWord(2).getData()), blockRepository.getNonce(senderAddress));
        assertNotNull(blockRepository.getAccountState(receiverAddress));
        assertNotEquals(result.getResult(), blockRepository.getCode(receiverAddress));
        assertEquals(18, blockRepository.getStorageValue(receiverAddress, new DataWord(0)).intValueSafe());

        blockRepository.commit();
        Repository repository = blockRepository.getSnapshotTo(result.getStateRoot());
        assertEquals(ContractUtil.toBigInteger(new DataWord(2).getData()), repository.getNonce(senderAddress));
        assertNotNull(repository.getAccountState(receiverAddress));
        assertNotEquals(result.getResult(), repository.getCode(receiverAddress));
        assertEquals(18, repository.getStorageValue(receiverAddress, new DataWord(0)).intValueSafe());

        Abi.Function func = Abi.Function.of("(uint256) get()");
        List<?> parsedResult = func.decodeResult(result.getResult());
        assertEquals(18, ((BigInteger) parsedResult.get(0)).intValue());
    }

    /**
     * 预编译合约调用。
     */
    @Test(timeout = 1000L)
    public void testExecute_PrecompiledContractInvocation_007() {
        ContractTypeEnum contractType = ContractTypeEnum.PRECOMPILED_CONTRACT_INVOCATION;
        byte[] transactionHash = Hex.decode("03e22f204d45f061a5b68847534b428a1277652677b6467f2d1f3381bbc4115c");
        byte[] nonce = new DataWord(0).getData();
        byte[] senderAddress = Hex.decode("05792f204d45f061a5b68847534b428a127ae583");
        byte[] receiverAddress = Hex.decode("0000000000000000000000000000000000000000000000000000000000000002");
        byte[] value = null;
        byte[] data = Hex.decode("405234801561001057600080fd00396000f30060806040526004361069763520");
        byte[] parentHash = Hex.decode("098765467d45f061a5b68847534b428a1277652677b6467f2d1f3381ae683910");
        byte[] minerAddress = Hex.decode("5f061a5b68847534b428a1277652677b6467f2d1");
        long timestamp = 3785872384L;
        long number = 12;

        blockRepository.createAccount(senderAddress);
        blockRepository.commit();


        ContractExecutionContext context = buildContractExecutionContext(contractType, transactionHash,
                nonce, senderAddress, receiverAddress, value, data, parentHash, minerAddress, timestamp, number);
        Executor<ContractExecutionResult> executor = factory.createExecutor(context);
        ContractExecutionResult result = executor.execute();


        assertNotNull(result);
        assertEquals(1, result.getTouchedAccountAddresses().size());
        assertTrue(result.getTouchedAccountAddresses().contains(receiverAddress));
        assertEquals(Hex.toHexString(transactionHash), Hex.toHexString(result.getTransactionHash()));
        assertNull(value);
        assertEquals(Hex.toHexString(HashUtil.sha256(data)), Hex.toHexString(result.getResult()));
        assertNull(result.getException());
        assertNotNull(result.getStateRoot());
        assertFalse(result.getRevert());
        assertNull(result.getErrorMessage());

        assertEquals(ContractUtil.toBigInteger(new DataWord(1).getData()), blockRepository.getNonce(senderAddress));
        assertNull(blockRepository.getAccountState(receiverAddress));

        blockRepository.commit();
        Repository repository = blockRepository.getSnapshotTo(result.getStateRoot());
        assertEquals(ContractUtil.toBigInteger(new DataWord(1).getData()), repository.getNonce(senderAddress));
        assertNull(repository.getAccountState(receiverAddress));
    }

    /**
     * 转账。
     */
    @Test(timeout = 1000L)
    public void testExecute_AssetTransfer_008() {
        ContractTypeEnum contractType = ContractTypeEnum.ASSET_TRANSFER;
        byte[] transactionHash = Hex.decode("03e22f204d45f061a5b68847534b428a1277652677b6467f2d1f3381bbc4115c");
        byte[] nonce = new DataWord(0).getData();
        byte[] senderAddress = Hex.decode("05792f204d45f061a5b68847534b428a127ae583");
        byte[] receiverAddress = Hex.decode("10a645668486da40f31fd050854fb137b317e056");
        byte[] value = new DataWord(46739).getData();
        byte[] data = null;
        byte[] parentHash = Hex.decode("098765467d45f061a5b68847534b428a1277652677b6467f2d1f3381ae683910");
        byte[] minerAddress = Hex.decode("5f061a5b68847534b428a1277652677b6467f2d1");
        long timestamp = 3785872384L;
        long number = 12;

        blockRepository.createAccount(senderAddress);
        blockRepository.createAccount(receiverAddress);
        blockRepository.addBalance(senderAddress, ContractUtil.toBigInteger(value));
        blockRepository.commit();


        ContractExecutionContext context = buildContractExecutionContext(contractType, transactionHash,
                nonce, senderAddress, receiverAddress, value, data, parentHash, minerAddress, timestamp, number);
        Executor<ContractExecutionResult> executor = factory.createExecutor(context);
        ContractExecutionResult result = executor.execute();


        assertNotNull(result);
        assertEquals(1, result.getTouchedAccountAddresses().size());
        assertTrue(result.getTouchedAccountAddresses().contains(receiverAddress));
        assertEquals(Hex.toHexString(transactionHash), Hex.toHexString(result.getTransactionHash()));
        assertEquals(Hex.toHexString(value), Hex.toHexString(result.getValue()));
        assertNull(result.getException());
        assertNotNull(result.getStateRoot());
        assertFalse(result.getRevert());
        assertNull(result.getErrorMessage());

        assertEquals(ContractUtil.toBigInteger(new DataWord(1).getData()), blockRepository.getNonce(senderAddress));
        assertNotNull(blockRepository.getAccountState(receiverAddress));
        assertEquals(BigInteger.valueOf(0L), blockRepository.getBalance(senderAddress));
        assertEquals(BigInteger.valueOf(46739L), blockRepository.getBalance(receiverAddress));

        blockRepository.commit();
        Repository repository = blockRepository.getSnapshotTo(result.getStateRoot());
        assertEquals(ContractUtil.toBigInteger(new DataWord(1).getData()), repository.getNonce(senderAddress));
        assertNotNull(repository.getAccountState(receiverAddress));
        assertEquals(BigInteger.valueOf(0L), repository.getBalance(senderAddress));
        assertEquals(BigInteger.valueOf(46739L), repository.getBalance(receiverAddress));
    }

    private byte[] compile(String fileName,
                           String contractName,
                           String constructorSignature,
                           Object... args) throws IOException {
        Path source = Paths.get("src","test/resources/contract/" + fileName);

        SolidityCompiler.Option allowPathsOption =
                new SolidityCompiler.Options.AllowPaths(Collections.singletonList(source.getParent().getParent()));
        SolidityCompiler.Result res =
                SolidityCompiler.compile(source.toFile(), true, ABI, BIN, INTERFACE, METADATA, allowPathsOption);
        CompilationResult result = CompilationResult.parse(res.output);
        CompilationResult.ContractMetadata metadata = result.getContract(contractName);

        return Abi.Constructor.of(constructorSignature, Hex.decode(metadata.bin), args);
    }

    private CompilationResult.ContractMetadata getContractMetadata(String fileName,
                                                                   String contractName) throws IOException {
        Path source = Paths.get("src", "test/resources/contract/" + fileName);

        SolidityCompiler.Option allowPathsOption =
                new SolidityCompiler.Options.AllowPaths(Collections.singletonList(source.getParent().getParent()));
        SolidityCompiler.Result res =
                SolidityCompiler.compile(source.toFile(), true, ABI, BIN, INTERFACE, METADATA, allowPathsOption);
        CompilationResult result = CompilationResult.parse(res.output);

        return result.getContract(contractName);
    }

    //    pragma solidity ^0.4.12;
    //
    //    contract ExecutorTest009 {
    //        event Sent(uint indexed value, address from, uint amount);
    //
    //        constructor () public {
    //            emit Sent(25, msg.sender, 56);
    //        }
    //    }
    /**
     * 事件日志。
     */
    @Test(timeout = 1000L)
    public void testExecute_Logs_009() throws IOException {
        ContractTypeEnum contractType = ContractTypeEnum.CONTRACT_CREATION;
        byte[] transactionHash = Hex.decode("03e22f204d45f061a5b68847534b428a1277652677b6467f2d1f3381bbc4115c");
        byte[] nonce = new DataWord(0).getData();
        byte[] senderAddress = Hex.decode("05792f204d45f061a5b68847534b428a127ae583");
        byte[] receiverAddress = Hex.decode("00a615668486da40f31fd050854fb137b317e056");
        byte[] value = new DataWord(0).getData();
        // 6080604052348015600f57600080fd5b5060197f2e0c9b7721d4bcc1b5781e2248e010b07b94a614f855a3406b43d03aad9ad4d233
        // 6038604051808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152
        // 6020018281526020019250505060405180910390a2603580608b6000396000f3006080604052600080fd00a165627a7a723058209a
        // df98f1a08289b12e493333601cbc0318b7294e44b47b59ccba15ba0ffc0e140029
        byte[] data = compile("ExecutorTest009.sol", "ExecutorTest009", "ExecutorTest009()");
        byte[] parentHash = Hex.decode("098765467d45f061a5b68847534b428a1277652677b6467f2d1f3381ae683910");
        byte[] minerAddress = Hex.decode("5f061a5b68847534b428a1277652677b6467f2d1");
        long timestamp = 3785872384L;
        long number = 12;

        blockRepository.createAccount(senderAddress);
        blockRepository.addBalance(senderAddress, ContractUtil.toBigInteger(value));
        blockRepository.commit();
        assertEquals(Hex.toHexString(receiverAddress), Hex.toHexString(HashUtil.calcNewAddr(senderAddress, nonce)));


        ContractExecutionContext context = buildContractExecutionContext(contractType, transactionHash,
                nonce, senderAddress, receiverAddress, value, data, parentHash, minerAddress, timestamp, number);
        Executor<ContractExecutionResult> executor = factory.createExecutor(context);
        ContractExecutionResult result = executor.execute();


        assertNotNull(result);
        assertEquals(1, result.getTouchedAccountAddresses().size());
        assertTrue(result.getTouchedAccountAddresses().contains(receiverAddress));
        assertEquals(Hex.toHexString(transactionHash), Hex.toHexString(result.getTransactionHash()));
        assertEquals(Hex.toHexString(value), Hex.toHexString(result.getValue()));
        assertEquals(1, result.getLogInfoList().size());
        assertEquals("00a615668486da40f31fd050854fb137b317e056",
                Hex.toHexString(result.getLogInfoList().get(0).getAddress()));
        assertEquals("00000000000000000000000005792f204d45f061a5b68847534b428a127ae583" +
                "0000000000000000000000000000000000000000000000000000000000000038",
                Hex.toHexString(result.getLogInfoList().get(0).getData()));
        assertEquals("0000000000000000000000000000000000000000000000000000000000000019",
                Hex.toHexString(result.getLogInfoList().get(0).getTopics().get(1).getData()));

        CompilationResult.ContractMetadata metadata = getContractMetadata("ExecutorTest009.sol", "ExecutorTest009");
        CallTransaction.Contract contract = new CallTransaction.Contract(metadata.abi);
        CallTransaction.Function event = contract.getByName("Sent");
        byte[] eventSignatrue = event.encodeSignatureLong();
        //"2e0c9b7721d4bcc1b5781e2248e010b07b94a614f855a3406b43d03aad9ad4d2"
        assertEquals(Hex.toHexString(eventSignatrue),
                Hex.toHexString(result.getLogInfoList().get(0).getTopics().get(0).getData()));

        CallTransaction.Invocation invocation = contract.parseEvent(result.getLogInfoList().get(0));
        assertEquals(25, ((BigInteger) invocation.args[0]).intValue());
        assertEquals("05792f204d45f061a5b68847534b428a127ae583", Hex.toHexString((byte[]) invocation.args[1]));
        assertEquals(56, ((BigInteger) invocation.args[2]).intValue());

        assertEquals("6080604052600080fd00a165627a7a723058207ed1cf4ff17e07eb0fdbf21f0531f91a15a818de46ff9bd268058826" +
                "1c02ed7d0029", Hex.toHexString(result.getResult()));
        assertEquals(0, result.getDeleteAccounts().size());
        assertEquals(0, result.getInternalTransactions().size());
        assertNull(result.getException());
        assertNotNull(result.getStateRoot());
        assertFalse(result.getRevert());
        assertNull(result.getErrorMessage());

        assertEquals(ContractUtil.toBigInteger(new DataWord(1).getData()), blockRepository.getNonce(senderAddress));
        assertNotNull(blockRepository.getAccountState(receiverAddress));
        assertArrayEquals(result.getResult(), blockRepository.getCode(receiverAddress));

        blockRepository.commit();
        Repository repository = blockRepository.getSnapshotTo(result.getStateRoot());
        assertEquals(ContractUtil.toBigInteger(new DataWord(1).getData()), repository.getNonce(senderAddress));
        assertNotNull(repository.getAccountState(receiverAddress));
        assertArrayEquals(result.getResult(), repository.getCode(receiverAddress));
    }

    //    pragma solidity ^0.4.12;
    //
    //    contract DataStore {
    //        uint256 data;
    //
    //        function set(uint256 x) public {
    //            data = x;
    //        }
    //
    //        function get() public view returns (uint256) {
    //            return data;
    //        }
    //    }
    private void deployTargetContract() throws IOException {
        ContractTypeEnum contractType = ContractTypeEnum.CONTRACT_CREATION;
        byte[] transactionHash = Hex.decode("03e22f204d45f061a5b68847534b428a1277652677b6467f2d1f3381bbc4115c");
        byte[] nonce = new DataWord(0).getData();
        byte[] senderAddress = Hex.decode("05792f204d45f061a5b68847534b428a127ae583");
        byte[] receiverAddress = Hex.decode("00a615668486da40f31fd050854fb137b317e056");
        byte[] value = new DataWord(0).getData();
        byte[] data = compile("DataStore010.sol", "DataStore", "DataStore()");
        byte[] parentHash = Hex.decode("098765467d45f061a5b68847534b428a1277652677b6467f2d1f3381ae683910");
        byte[] minerAddress = Hex.decode("5f061a5b68847534b428a1277652677b6467f2d1");
        long timestamp = 3785872384L;
        long number = 12;

        blockRepository.createAccount(senderAddress);
        blockRepository.addBalance(senderAddress, ContractUtil.toBigInteger(value));
        blockRepository.commit();
        assertEquals(Hex.toHexString(receiverAddress), Hex.toHexString(HashUtil.calcNewAddr(senderAddress, nonce)));


        ContractExecutionContext context = buildContractExecutionContext(contractType, transactionHash,
                nonce, senderAddress, receiverAddress, value, data, parentHash, minerAddress, timestamp, number);
        Executor<ContractExecutionResult> executor = factory.createExecutor(context);
        executor.execute();
    }

    //    pragma solidity ^0.4.12;
    //
    //    contract DataStore {
    //        function set(uint256 x) public;
    //    }
    //
    //    contract Caller {
    //        function call() public {
    //            address addr = 0x00a615668486da40f31fd050854fb137b317e056;
    //            DataStore dataStore = DataStore(addr);
    //            dataStore.set(6);
    //        }
    //    }
    private void deployCallerContract() throws IOException {
        ContractTypeEnum contractType = ContractTypeEnum.CONTRACT_CREATION;
        byte[] transactionHash = Hex.decode("13e22f204d45f061a5b68847534b428a1277652677b6467f2d1f3381bbc4115c");
        byte[] nonce = new DataWord(1).getData();
        byte[] senderAddress = Hex.decode("05792f204d45f061a5b68847534b428a127ae583");
        byte[] receiverAddress = Hex.decode("71e0ecb8ccaf0e56ef87df74dcdd9a9510bf0a3a");
        byte[] value = new DataWord(0).getData();
        byte[] data = compile("Caller010.sol", "Caller", "Caller()");
        byte[] parentHash = Hex.decode("198765467d45f061a5b68847534b428a1277652677b6467f2d1f3381ae683910");
        byte[] minerAddress = Hex.decode("2f061a5b68847534b428a1277652677b6467f2d1");
        long timestamp = 3785872384L;
        long number = 12;

        blockRepository.addBalance(senderAddress, ContractUtil.toBigInteger(value));
        blockRepository.commit();
        assertEquals(Hex.toHexString(receiverAddress), Hex.toHexString(HashUtil.calcNewAddr(senderAddress, nonce)));


        ContractExecutionContext context = buildContractExecutionContext(contractType, transactionHash,
                nonce, senderAddress, receiverAddress, value, data, parentHash, minerAddress, timestamp, number);
        Executor<ContractExecutionResult> executor = factory.createExecutor(context);
        executor.execute();
    }

    /**
     * 合约内调用合约。
     */
    @Test(timeout = 1000L)
    public void testExecute_InvokeContract_010() throws IOException {
        deployTargetContract();
        deployCallerContract();

        ContractTypeEnum contractType = ContractTypeEnum.CUSTOMER_CONTRACT_INVOCATION;
        byte[] transactionHash = Hex.decode("03e22f204d45f061a5b68847534b428a1277652677b6467f2d1f3381bbc4115c");
        byte[] nonce = new DataWord(2).getData();
        byte[] senderAddress = Hex.decode("05792f204d45f061a5b68847534b428a127ae583");
        byte[] receiverAddress = Hex.decode("71e0ecb8ccaf0e56ef87df74dcdd9a9510bf0a3a");
        byte[] value = new DataWord(0).getData();
        byte[] data = Hex.decode("28b5e32b");
        byte[] parentHash = Hex.decode("098765467d45f061a5b68847534b428a1277652677b6467f2d1f3381ae683910");
        byte[] minerAddress = Hex.decode("5f061a5b68847534b428a1277652677b6467f2d1");
        long timestamp = 3785872384L;
        long number = 12;

        blockRepository.addBalance(senderAddress, ContractUtil.toBigInteger(value));
        blockRepository.commit();


        ContractExecutionContext context = buildContractExecutionContext(contractType, transactionHash,
                nonce, senderAddress, receiverAddress, value, data, parentHash, minerAddress, timestamp, number);
        Executor<ContractExecutionResult> executor = factory.createExecutor(context);
        ContractExecutionResult result = executor.execute();


        assertNotNull(result);
        assertEquals(2, result.getTouchedAccountAddresses().size());
        assertTrue(result.getTouchedAccountAddresses().contains(receiverAddress));
        assertTrue(result.getTouchedAccountAddresses().contains(Hex.decode("71e0ecb8ccaf0e56ef87df74dcdd9a9510bf0a3a")));
        assertEquals(Hex.toHexString(transactionHash), Hex.toHexString(result.getTransactionHash()));
        assertEquals(Hex.toHexString(value), Hex.toHexString(result.getValue()));
        assertEquals(0, result.getLogInfoList().size());



        assertEquals(0, result.getDeleteAccounts().size());
        assertEquals(1, result.getInternalTransactions().size());



        assertNull(result.getException());
        assertNotNull(result.getStateRoot());
        assertFalse(result.getRevert());
        assertNull(result.getErrorMessage());

        assertEquals(ContractUtil.toBigInteger(new DataWord(3).getData()), blockRepository.getNonce(senderAddress));
        assertNotNull(blockRepository.getAccountState(receiverAddress));

        blockRepository.commit();
        Repository repository = blockRepository.getSnapshotTo(result.getStateRoot());
        assertEquals(ContractUtil.toBigInteger(new DataWord(3).getData()), repository.getNonce(senderAddress));
        assertNotNull(repository.getAccountState(receiverAddress));
    }

    private void deployCreateCreate() throws IOException {
        ContractTypeEnum contractType = ContractTypeEnum.CONTRACT_CREATION;
        byte[] transactionHash = Hex.decode("03e22f204d45f061a5b68847534b428a1277652677b6467f2d1f3381bbc4115c");
        byte[] nonce = new DataWord(0).getData();
        byte[] senderAddress = Hex.decode("05792f204d45f061a5b68847534b428a127ae583");
        byte[] receiverAddress = Hex.decode("00a615668486da40f31fd050854fb137b317e056");
        byte[] value = new DataWord(0).getData();
        byte[] data = compile("CreateContract011.sol", "Caller", "Caller()");
        byte[] parentHash = Hex.decode("098765467d45f061a5b68847534b428a1277652677b6467f2d1f3381ae683910");
        byte[] minerAddress = Hex.decode("5f061a5b68847534b428a1277652677b6467f2d1");
        long timestamp = 3785872384L;
        long number = 12;

        blockRepository.createAccount(senderAddress);
        blockRepository.addBalance(senderAddress, ContractUtil.toBigInteger(value));
        blockRepository.commit();
        assertEquals(Hex.toHexString(receiverAddress), Hex.toHexString(HashUtil.calcNewAddr(senderAddress, nonce)));


        ContractExecutionContext context = buildContractExecutionContext(contractType, transactionHash,
                nonce, senderAddress, receiverAddress, value, data, parentHash, minerAddress, timestamp, number);
        Executor<ContractExecutionResult> executor = factory.createExecutor(context);
        executor.execute();
    }

    private void call() {
        ContractTypeEnum contractType = ContractTypeEnum.CUSTOMER_CONTRACT_INVOCATION;
        byte[] transactionHash = Hex.decode("03e22f204d45f061a5b68847534b428a1277652677b6467f2d1f3381bbc4115c");
        byte[] nonce = new DataWord(1).getData();
        byte[] senderAddress = Hex.decode("05792f204d45f061a5b68847534b428a127ae583");
        byte[] receiverAddress = Hex.decode("00a615668486da40f31fd050854fb137b317e056");
        byte[] value = new DataWord(0).getData();
        byte[] data = Hex.decode("28b5e32b");
        byte[] parentHash = Hex.decode("098765467d45f061a5b68847534b428a1277652677b6467f2d1f3381ae683910");
        byte[] minerAddress = Hex.decode("5f061a5b68847534b428a1277652677b6467f2d1");
        long timestamp = 3785872384L;
        long number = 12;

        blockRepository.addBalance(senderAddress, ContractUtil.toBigInteger(value));
        blockRepository.commit();


        ContractExecutionContext context = buildContractExecutionContext(contractType, transactionHash,
                nonce, senderAddress, receiverAddress, value, data, parentHash, minerAddress, timestamp, number);
        Executor<ContractExecutionResult> executor = factory.createExecutor(context);
        executor.execute();
    }

    private byte[] getNewContract() {
        call();

        ContractTypeEnum contractType = ContractTypeEnum.CUSTOMER_CONTRACT_INVOCATION;
        byte[] transactionHash = Hex.decode("03e22f204d45f061a5b68847534b428a1277652677b6467f2d1f3381bbc4115c");
        byte[] nonce = new DataWord(2).getData();
        byte[] senderAddress = Hex.decode("05792f204d45f061a5b68847534b428a127ae583");
        byte[] receiverAddress = Hex.decode("00a615668486da40f31fd050854fb137b317e056");
        byte[] value = new DataWord(0).getData();
        Abi.Function func = Abi.Function.of("(address) getDsAddress()");
        byte[] data = Hex.decode("dec1e776");
        byte[] parentHash = Hex.decode("098765467d45f061a5b68847534b428a1277652677b6467f2d1f3381ae683910");
        byte[] minerAddress = Hex.decode("5f061a5b68847534b428a1277652677b6467f2d1");
        long timestamp = 3785872384L;
        long number = 12;

        blockRepository.addBalance(senderAddress, ContractUtil.toBigInteger(value));
        blockRepository.commit();


        ContractExecutionContext context = buildContractExecutionContext(contractType, transactionHash,
                nonce, senderAddress, receiverAddress, value, data, parentHash, minerAddress, timestamp, number);
        Executor<ContractExecutionResult> executor = factory.createExecutor(context);
        ContractExecutionResult result = executor.execute();

        List<?> list = func.decodeResult(result.getResult(), false);
        return  (byte[]) list.get(0);
    }

    @Test(timeout = 2000L)
    public void testExecute_CreateContract_011() throws IOException {
        deployCreateCreate();
        byte[] newContract = getNewContract();

        ContractTypeEnum contractType = ContractTypeEnum.CUSTOMER_CONTRACT_INVOCATION;
        byte[] transactionHash = Hex.decode("03e22f204d45f061a5b68847534b428a1277652677b6467f2d1f3381bbc4115c");
        byte[] nonce = new DataWord(3).getData();
        byte[] senderAddress = Hex.decode("05792f204d45f061a5b68847534b428a127ae583");
        byte[] receiverAddress = newContract;
        byte[] value = new DataWord(0).getData();
        Abi.Function func = Abi.Function.of("(uint256) get()");
        byte[] data = Hex.decode("6d4ce63c");
        byte[] parentHash = Hex.decode("098765467d45f061a5b68847534b428a1277652677b6467f2d1f3381ae683910");
        byte[] minerAddress = Hex.decode("5f061a5b68847534b428a1277652677b6467f2d1");
        long timestamp = 3785872384L;
        long number = 12;

        blockRepository.addBalance(senderAddress, ContractUtil.toBigInteger(value));
        blockRepository.commit();


        ContractExecutionContext context = buildContractExecutionContext(contractType, transactionHash,
                nonce, senderAddress, receiverAddress, value, data, parentHash, minerAddress, timestamp, number);
        Executor<ContractExecutionResult> executor = factory.createExecutor(context);
        ContractExecutionResult result = executor.execute();
        List<?> list = func.decodeResult(result.getResult(), false);

        assertNotNull(result);
        assertEquals(6, ((BigInteger)list.get(0)).intValue());
    }

//    @Test
//    public void testExecute_Suicide() {
////        pragma solidity ^0.4.12;
////
////        contract DataStorage {
////            constructor() public {
////                selfdestruct(msg.sender);
////            }
////        }

//        ExecutionResult executionResult = executor.execute();
//
//

//
//        Assert.assertEquals(1, executionResult.getDeleteAccounts().size());
//        DataWord account = new DataWord();
//        for (DataWord dataWord : executionResult.getDeleteAccounts()) {
//            account = dataWord;
//        }
//        Assert.assertEquals(Hex.toHexString(contractAddress), Hex.toHexString(contractAddress), Hex.toHexString(account.getLast20Bytes()));
//        System.out.println();
//
////        Assert.assertFalse(executor.getContractRepository().isExist(contractAddress));
//    }
}