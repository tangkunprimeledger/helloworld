package com.higgs.trust.evmcontract.demo;

import com.higgs.trust.evmcontract.config.SystemProperties;
import com.higgs.trust.evmcontract.core.Block;
import com.higgs.trust.evmcontract.core.Repository;
import com.higgs.trust.evmcontract.core.Transaction;
import com.higgs.trust.evmcontract.datasource.DbSource;
import com.higgs.trust.evmcontract.datasource.rocksdb.RocksDbDataSource;
import com.higgs.trust.evmcontract.db.BlockStore;
import com.higgs.trust.evmcontract.db.RepositoryRoot;
import com.higgs.trust.evmcontract.util.ByteUtil;
import com.higgs.trust.evmcontract.vm.VM;
import com.higgs.trust.evmcontract.vm.program.Program;
import com.higgs.trust.evmcontract.vm.program.ProgramResult;
import com.higgs.trust.evmcontract.vm.program.invoke.ProgramInvoke;
import com.higgs.trust.evmcontract.vm.program.invoke.ProgramInvokeImpl;
import org.apache.commons.io.IOUtils;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import static org.apache.commons.lang3.ArrayUtils.nullToEmpty;

/**
 * @author duhongming
 * @date 2018/11/15
 */
public class ContractService {

    private SystemProperties config;
    private DbSource<byte[]> db;
    private RepositoryRoot repository;

    //private boolean initialized;

    private final  static byte[] gasPrice = Hex.decode("e8d4a51000");  // 1000000000000
    private final  static byte[] gas = Hex.decode("27100000000000");            // 10000

    public ContractService(byte[] rootHash) {
        this.config = SystemProperties.getDefault();
        this.db = new RocksDbDataSource("trust");
//        this.db = new HashMapDB<>();
        db.init();
        if (rootHash == null || rootHash.length == 0) {
            rootHash = db.get("RootHash".getBytes());
        }

        if (rootHash != null) {
            System.out.println("Get RootHash:" + Hex.toHexString(rootHash));
        }

        this.repository = new RepositoryRoot(db, rootHash);

//        this.repository.addBalance(Hex.decode("13434c369b163A16F969C15F965618a724b3a634"), new BigInteger("10000000"));
//        BigInteger balance = this.repository.getBalance(Hex.decode("13434c369b163A16F969C15F965618a724b3a634"));
//        System.out.println("balance: " + balance);
    }

    public static void main(String[] args) {
        byte[] root = Hex.decode("6060604052341561000f57600080fd5b5b6101a18061001f6000396000f300606060405263ffffffff7c0100000000000000000000000000000000000000000000000000000000600035041663b21e51f18114610069578063b2ab515414610084578063c226d9ee14610099578063e1cb0e52146100ae578063f0ba8440146100d3575b600080fd5b341561007457600080fd5b6100826004356024356100fb565b005b341561008f57600080fd5b610082610111565b005b34156100a457600080fd5b61008261013b565b005b34156100b957600080fd5b6100c161015c565b60405190815260200160405180910390f35b34156100de57600080fd5b6100c1600435610163565b60405190815260200160405180910390f35b60008281526002602052604090208190555b5050565b60015b600a8110156101375760008181526002602052604090208190555b600101610114565b5b50565b60005b60648110156101375760008054820190555b60010161013e565b5b50565b6000545b90565b600260205260009081526040902054815600a165627a7a7230582076eb7abd88c40d3d271ff78493107db7edfc5e0b57d2fa7ec5baa8c92fa117170029");
        ContractService contractService = new ContractService(root);
        byte[] contractAddress = contractService.createContract();
        contractService.commit();

        contractService.invoke(contractAddress);
        contractService.commit();
    }

    public void destroy() {
        repository.close();
        db.close();
    }

    public void commit() {
        this.repository.commit();
//        String dumpStr = this.repository.dumpStateTrie();
//        System.out.println(dumpStr);
        System.out.printf("RootHash=%s\n", Hex.toHexString(repository.getRoot()));
        db.put("RootHash".getBytes(), repository.getRoot());
    }

    public byte[] createContract() {
        byte[] nonce = BigIntegers.asUnsignedByteArray(BigInteger.ZERO);
        byte[] code = loadCodeFromResourceFile("D:\\tmp\\demo.txt");
        byte[] receiveAddress = Hex.decode("");//13978aee95f38490e9769c39b2773ed763d9cd5f
        byte[] value = Hex.decode("");          //10000000000000000 2386f26fc10000"
        byte[] address = createContract(nonce, receiveAddress, value, code);
        System.out.printf("address=%s\n", Hex.toHexString(address));
        System.out.printf("RootHash=%s\n", Hex.toHexString(repository.getRoot()));
        return address;
    }

    public void invoke(byte[] contractAddress) {
        byte[] value = Hex.decode(""); //10000000000000000 2386f26fc10000"
        byte[] nonce = BigIntegers.asUnsignedByteArray(BigInteger.ZERO);

        //call contract
        byte[] data = Hex.decode("9e955b540000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000000a6475686f6e676d696e6700000000000000000000000000000000000000000000");
        System.out.println("thread id:" + Thread.currentThread().getName());
        invokeContract(nonce, contractAddress, value, data);
    }


    public byte[] createContract(byte[] nonce, byte[] receiveAddress, byte[] value, byte[] data) {
        Transaction tx = new Transaction(nonce, gasPrice, gas, receiveAddress, value, data);

        Block block = new Block();
        Repository txTrack = repository.startTracking();
        Repository contractTrack = txTrack.startTracking();

        ProgramInvoke programInvoke = createProgramInvoke(tx, block, contractTrack, null);
        Program program = new Program(tx.getData(), programInvoke, tx, SystemProperties.getDefault());

        VM vm = new VM(config);
        vm.play(program);

        ProgramResult result = program.getResult();
        if (!result.isRevert()) {
            contractTrack.saveCode(tx.getContractAddress(), result.getHReturn());
        }
        contractTrack.commit();
        txTrack.commit();
//        repository.commit();

//        System.out.println("合約result:" + Hex.toHexString(program.getResult().getHReturn()));
//        byte[] root = repository.getRoot();
//        System.out.println("RootHash:" + Hex.toHexString(root));
        return tx.getContractAddress();
    }

    public byte[] invokeContract(byte[] nonce, byte[] contractAddress, byte[] value, byte[] data) {
        Transaction tx = new Transaction(nonce, gasPrice, gas, contractAddress, value, data);

        Block block = new Block();
        Repository txTrack = repository.startTracking();
        Repository contractTrack = txTrack.startTracking();

        ProgramInvoke programInvoke = createProgramInvoke(tx, block, contractTrack, null);
        Program  program = new Program(contractTrack.getCode(contractAddress), programInvoke, tx, SystemProperties.getDefault());

        VM vm = new VM(config);
        vm.play(program);

        ProgramResult result = program.getResult();
        contractTrack.commit();
        txTrack.commit();
//        repository.commit();
        System.out.println("合約result:" + Hex.toHexString(result.getHReturn()));
//        byte[] root = repository.getRoot();
//        System.out.println("RootHash:" + Hex.toHexString(root));
        return result.getHReturn();
    }


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
        byte[] lastHash = block.getParentHash();

        /***   COINBASE  op ***/
        byte[] coinbase = block.getCoinbase();

        /*** TIMESTAMP  op  ***/
        long timestamp = block.getTimestamp();

        /*** NUMBER  op  ***/
        long number = block.getNumber();

        /*** DIFFICULTY  op  ***/
        byte[] difficulty = block.getDifficulty();

        /*** GASLIMIT op ***/
        byte[] gaslimit = block.getGasLimit();


        return new ProgramInvokeImpl(address, origin, caller, balance, gasPrice, gas, callValue, data,
                lastHash, coinbase, timestamp, number, difficulty, gaslimit,
                repository, blockStore);
    }

    public static byte[] loadCodeFromResourceFile(String filePath) {
        try {
            String code = IOUtils.toString(new File(filePath).toURI(), "UTF-8");
            return Hex.decode(code);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
